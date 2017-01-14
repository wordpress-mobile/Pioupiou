package org.wordpress.pioupiou.postlist;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.postlist.DummyContent.PostItem;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

import timber.log.Timber;

public class PostListActivity extends AppCompatActivity implements OnListFragmentInteractionListener {
    // UI references
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // FluxC
    // TODO: Inject stores here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // TODO: show the "New message" UI
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
