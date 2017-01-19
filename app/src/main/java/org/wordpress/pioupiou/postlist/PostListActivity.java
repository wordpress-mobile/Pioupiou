package org.wordpress.pioupiou.postlist;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.fenchtose.tooltip.Tooltip;
import com.fenchtose.tooltip.Tooltip.Listener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.action.PostAction;
import org.wordpress.android.fluxc.generated.PostActionBuilder;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.persistentedittext.PersistentEditText;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.misc.PioupiouApp;
import org.wordpress.pioupiou.postlist.PostListFragment.OnListFragmentInteractionListener;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class PostListActivity extends AppCompatActivity implements OnListFragmentInteractionListener {
    // UI references
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // State
    private boolean mNewPostVisible;
    private boolean mIsFetchingPosts;

    private String mNewPostContent;

    // FluxC
    @Inject Dispatcher mDispatcher;
    @Inject PostStore mPostStore;
    @Inject SiteStore mSiteStore;
    @Inject AccountStore mAccountStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PioupiouApp) getApplication()).component().inject(this);

        setContentView(R.layout.activity_post_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (!mIsFetchingPosts) {
                            fetchPosts();
                        }
                    }
                }
        );

        // immediately show existing posts then fetch the latest from the server
        showPosts();
        fetchPosts();
    }

    @Override
    public void onStart() {
        super.onStart();
        mDispatcher.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDispatcher.unregister(this);
    }

    private void showPosts() {
        Timber.i("Show posts started");
        SiteModel site = getSite();
        if (site == null) {
            Timber.w("Can't show posts for null site");
            return;
        }

        if (hasPostFragment()) {
            getPostFragment().setPosts(mAccountStore.getAccount(), mPostStore.getPostsForSite(site));
        }
    }

    private void fetchPosts() {
        Timber.i("Fetch posts started");
        SiteModel site = getSite();
        if (site == null) {
            Timber.w("Can't fetch posts for null site");
            return;
        }

        mIsFetchingPosts = true;
        mDispatcher.dispatch(PostActionBuilder.newFetchPostsAction(
                new PostStore.FetchPostsPayload(site)));
        mSwipeRefreshLayout.setRefreshing(true);
    }

    /*
     * returns the current site to use for the post list
     */
    private SiteModel getSite() {
        List<SiteModel> sites = mSiteStore.getSitesByNameOrUrlMatching("nbradbury.wordpress.com");
        if (sites.size() != 0) {
            return sites.get(0);
        } else {
            return null;
        }
    }

    /*
     * called when the user taps a post in the list
     */
    @Override
    public void onListFragmentInteraction(PostModel post) {
        Timber.i("Post tapped");
        // uncomment to delete a post when tapped - useful to remove test posts
        // PostStore.RemotePostPayload payload = new PostStore.RemotePostPayload(post, getSite());
        // mDispatcher.dispatch(PostActionBuilder.newDeletePostAction(payload));
        // showProgress(true);
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Timber.i("New post");
                createNewPost();
                // TODO: show the "New message" UI
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createNewPost() {
        if (mNewPostVisible) {
            return;
        }
        mNewPostVisible = true;

        View anchor = findViewById(R.id.action_add);
        View newPostView = getLayoutInflater().inflate(R.layout.new_post, null);
        final PersistentEditText editText = (PersistentEditText) newPostView.findViewById(R.id.new_post_edit_text);
        ImageView sendButton = (ImageView) newPostView.findViewById(R.id.send_post);
        int tipSizeSmall = getResources().getDimensionPixelSize(R.dimen.text_margin);
        int tooltipColor = ContextCompat.getColor(this, R.color.nux_background);

        final Tooltip tooltip = new Tooltip.Builder(this)
                .anchor(anchor, Tooltip.BOTTOM)
                // Can't use animation because the tooltip library starts the animation when onLayout() is called
                // .animate(new TooltipAnimation(TooltipAnimation.REVEAL, 400))
                .autoAdjust(true)
                .content(newPostView)
                .cancelable(true)
                .withPadding(getResources().getDimensionPixelOffset(R.dimen.tooltip_padding))
                .withTip(new Tooltip.Tip(tipSizeSmall, tipSizeSmall / 2, tooltipColor))
                .into((ViewGroup) findViewById(R.id.root))
                .debug(true)
                .withListener(new Listener() {
                    @Override
                    public void onDismissed() {
                        mNewPostVisible = false;
                        // Hide keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                })
                .show();

        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewPostContent = editText.getText().toString();
                // Clear the saved text
                editText.setText("");
                tooltip.dismiss(true);
                publishPost();
            }
        });
    }

    // Publish post
    private void publishPost() {
        if (TextUtils.isEmpty(mNewPostContent)) {
            ToastUtils.showToast(this, "Can't publish an empty post");
            return;
        }

        // instantiate a new post, event handler will take care of setting the content and uploading it
        PostStore.InstantiatePostPayload payload = new PostStore.InstantiatePostPayload(getSite(), false);
        mDispatcher.dispatch(PostActionBuilder.newInstantiatePostAction(payload));
    }

    private boolean hasPostFragment() {
        return getPostFragment() != null;
    }

    private PostListFragment getPostFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.list);
        if (fragment instanceof PostListFragment) {
            return (PostListFragment) fragment;
        } else {
            return null;
        }
    }

    private void showProgress(boolean show) {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        ToastUtils.showToast(this, message, ToastUtils.Duration.LONG);
    }

    /*
     * called whenever the post list changes, such as when a fetch posts has completed
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostChanged(PostStore.OnPostChanged event) {
        if (event.causeOfChange == PostAction.FETCH_POSTS
                || event.causeOfChange == PostAction.FETCHED_POSTS) {
            mIsFetchingPosts = false;
        }

        if (!event.isError()) {
            showPosts();
        } else {
            showError("OnPostChanged error - "
                    + event.error.message
                    + " (" + event.causeOfChange.toString() + ")");
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    /*
     * called when a new post has been instantiated - we use this to set the new post's content
     * and actually publish it
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostInstantiated(PostStore.OnPostInstantiated event) {
        if (!event.isError()) {
            showProgress(true);
            PostModel post = event.post;
            post.setContent(mNewPostContent);
            PostStore.RemotePostPayload payload = new PostStore.RemotePostPayload(post, getSite());
            mDispatcher.dispatch(PostActionBuilder.newPushPostAction(payload));
        } else {
            showError("onPostInstantiated error - " + event.error.message);
        }
    }

    /*
     * called when a new post has been uploaded
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostUploaded(PostStore.OnPostUploaded event) {
        showProgress(false);
        if (!event.isError()) {
            showPosts();
        } else {
            showError("OnPostUploaded error - " + event.error.message);
        }
    }
}
