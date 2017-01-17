package org.wordpress.pioupiou.postlist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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

import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.persistentedittext.PersistentEditText;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.misc.PioupiouApp;
import org.wordpress.pioupiou.postlist.DummyContent.PostItem;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

import javax.inject.Inject;

import timber.log.Timber;

public class PostListActivity extends AppCompatActivity implements OnListFragmentInteractionListener {
    // UI references
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // State
    boolean mNewPostVisible;

    // FluxC
    @Inject PostStore mPostStore;
    @Inject SiteStore mSiteStore;

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
                        fetchPosts();
                    }
                }
        );
    }

    private void fetchPosts() {
        Timber.i("Fetch posts started");
        // TODO: fetch posts
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onListFragmentInteraction(PostItem item) {
        Timber.i("Post tapped");
        // TODO: edit?
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
                editText.setText("");
                tooltip.dismiss(true);
                publishPost(editText.getText().toString());
            }
        });
    }

    // Publish post
    private void publishPost(String text) {
        Timber.i("Publishing post: " + text);
        // TODO: Prepare the post and dispatch the action
    }
}
