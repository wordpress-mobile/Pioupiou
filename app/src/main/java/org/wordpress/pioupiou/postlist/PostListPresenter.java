package org.wordpress.pioupiou.postlist;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.action.PostAction;
import org.wordpress.android.fluxc.generated.PostActionBuilder;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.PostStore.FetchPostsPayload;
import org.wordpress.android.fluxc.store.PostStore.OnPostChanged;
import org.wordpress.android.fluxc.store.PostStore.OnPostUploaded;
import org.wordpress.android.fluxc.store.PostStore.RemotePostPayload;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.util.UrlUtils;
import org.wordpress.pioupiou.BuildConfig;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class PostListPresenter implements PostListContract.Presenter {
    private Dispatcher mDispatcher;
    private AccountStore mAccountStore;
    private PostStore mPostStore;
    private SiteStore mSiteStore;

    private PostListContract.View mPostListView;
    private boolean mIsFetchingPosts;

    @Inject
    PostListPresenter(Dispatcher dispatcher, AccountStore accountStore, PostStore postStore, SiteStore siteStore) {
        mDispatcher = dispatcher;
        mAccountStore = accountStore;
        mPostStore = postStore;
        mSiteStore = siteStore;
    }

    private void openTask() {
        // immediately show existing posts then fetch the latest from the server
        SiteModel site = getSite();
        if (site == null) {
            Timber.w("Can't show posts for null site");
            mPostListView.showError("No site found");
        } else {
            mPostListView.showPosts(mAccountStore.getAccount(), mPostStore.getPostsForSite(getSite()));
            fetchPosts();
        }
    }

    @Override
    public void fetchPosts() {
        if (!mIsFetchingPosts) {
            Timber.i("Fetch posts started");
            mIsFetchingPosts = true;
            mPostListView.setLoadingIndicator(true);
            mDispatcher.dispatch(PostActionBuilder.newFetchPostsAction(new FetchPostsPayload(getSite())));
        }
    }

    @Override
    public void publishPost(@NonNull String content) {
        // instantiate a new post with the content and publish it
        mPostListView.showProgress(true);
        PostModel post = mPostStore.instantiatePostModel(getSite(), false);
        post.setContent(content);
        RemotePostPayload payload = new RemotePostPayload(post, getSite());
        mDispatcher.dispatch(PostActionBuilder.newPushPostAction(payload));
    }

    private SiteModel getSite() {
        List<SiteModel> sites = mSiteStore.getSitesByNameOrUrlMatching(UrlUtils.removeScheme(BuildConfig.SITE_DOMAIN));
        if (sites.size() != 0) {
            return sites.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void takeView(PostListContract.View postListView) {
        mPostListView = postListView;
        mDispatcher.register(this);
        openTask();
    }

    @Override
    public void dropView() {
        mPostListView = null;
        mDispatcher.unregister(this);
    }


    /*
     * called whenever the post list changes, such as when a fetch posts has completed
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostChanged(OnPostChanged event) {
        if (event.causeOfChange == PostAction.FETCH_POSTS) {
            mIsFetchingPosts = false;
        }

        if (!event.isError()) {
            mPostListView.showPosts(mAccountStore.getAccount(), mPostStore.getPostsForSite(getSite()));
        } else {
            mPostListView.showError("OnPostChanged error - "
                    + event.error.message
                    + " (" + event.causeOfChange.toString() + ")");
        }

        mPostListView.setLoadingIndicator(false);
    }

    /*
     * called when a new post has been uploaded
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostUploaded(OnPostUploaded event) {
        mPostListView.showProgress(false);
        if (!event.isError()) {
            mPostListView.showPosts(mAccountStore.getAccount(), mPostStore.getPostsForSite(getSite()));
        } else {
            mPostListView.showError("OnPostUploaded error - " + event.error.message);
        }
    }
}
