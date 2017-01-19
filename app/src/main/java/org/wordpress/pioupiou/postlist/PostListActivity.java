package org.wordpress.pioupiou.postlist;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import org.wordpress.android.fluxc.generated.PostActionBuilder;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.persistentedittext.PersistentEditText;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.misc.PioupiouApp;
import org.wordpress.pioupiou.misc.PostUploadService;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

public class PostListActivity extends AppCompatActivity implements OnListFragmentInteractionListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    boolean mNewPostVisible;
    boolean mIsFetchingPosts = false;
    private SiteModel firstWPCOMSite;
    private PostFragment mPostList;
    private CountDownLatch mNewPostLatch;
    private PostModel mPost;
    private Handler mHandler;

    // FluxC
    @Inject Dispatcher mDispatcher;
    @Inject AccountStore mAccountStore;
    @Inject SiteStore mSiteStore;
    @Inject PostStore mPostStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Injection
        ((PioupiouApp) getApplication()).component().inject(this);

        setContentView(R.layout.activity_post_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchPosts();
                    }
                }
        );

        firstWPCOMSite = mSiteStore.getSiteBySiteId(
                PioupiouApp.blog_id
        );

        if (firstWPCOMSite == null) {
            ToastUtils.showToast(this, "Error while accessing eritreocazzulati.wordpress.com", ToastUtils.Duration.SHORT);
            finish();
            return;
        }

        FragmentManager fm = getFragmentManager();
        mPostList = (PostFragment) fm.findFragmentById(R.id.postList);
        mNewPostLatch = new CountDownLatch(1);
    }


    @Override
    public void onResume() {
        super.onResume();
        List<PostModel> posts = mPostStore.getPostsForSite(firstWPCOMSite);
        if (posts == null || posts.size() == 0) {
            fetchPosts();
        } else {
            mPostList.updatePostsList(mPostStore, firstWPCOMSite);
        }

        mHandler = new Handler();
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


    private void fetchPosts() {
        Timber.i("Fetch posts started");
        if (mIsFetchingPosts) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            //show (EmptyViewMessageType.NETWORK_ERROR);
            setRefreshing(false);
            return;
        }
        mIsFetchingPosts = true;


        PostStore.FetchPostsPayload payload = new PostStore.FetchPostsPayload(firstWPCOMSite, false);
        mDispatcher.dispatch(PostActionBuilder.newFetchPostsAction(payload));
        setRefreshing(true);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostChanged(PostStore.OnPostChanged event) {
        switch (event.causeOfChange) {
            case FETCH_POSTS:
            case FETCH_PAGES:
                mIsFetchingPosts = false;
                setRefreshing(false);
                if (!event.isError()) {
                    mPostList.updatePostsList(mPostStore, firstWPCOMSite);
                } else {
                    PostStore.PostError error = event.error;
                    switch (error.type) {
                        case UNAUTHORIZED:
                            ToastUtils.showToast(this, "Not Authorized on eritreocazzulati.wordpress.com", ToastUtils.Duration.SHORT);
                            break;
                        default:
                            ToastUtils.showToast(this, error.toString(), ToastUtils.Duration.SHORT);
                            break;
                    }
                }
                break;
            case DELETE_POST:
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(PostModel item) {
        Timber.i("Post tapped");
        ToastUtils.showToast(this, "Nope", ToastUtils.Duration.SHORT);
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

    // New post

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
                // Clear the saved text
                tooltip.dismiss(true);
                publishPost(editText.getText().toString());
                editText.setText("");
            }
        });
    }

    // Publish post
    private void publishPost(String text) {
        Timber.i("Publishing post: " + text);

        // Create a new post
        List<Long> categories = new ArrayList<>();
        PostStore.InstantiatePostPayload payload = new PostStore.InstantiatePostPayload(firstWPCOMSite, false, categories, "default");
        mDispatcher.dispatch(PostActionBuilder.newInstantiatePostAction(payload));

        // Wait for the OnPostInstantiated event to initialize the post
        try {
            mNewPostLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mPost == null) {
            throw new RuntimeException("No callback received from INSTANTIATE_POST action");
        }

        mPost.setContent(text);

        // If the post is empty, don't publish
       /* if (TextUtils.isEmpty(mPost.getContent())) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PostListActivity.this, "Empty", ToastUtils.Duration.SHORT);
                }
            });
            return;
        }*/

        PostUploadService.addPostToUpload(mPost);
        PostUploadService.setLegacyMode(true);
        startService(new Intent(PostListActivity.this, PostUploadService.class));
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPostInstantiated(PostStore.OnPostInstantiated event) {
        mPost = event.post;
        mNewPostLatch.countDown();
    }

    private void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }
}
