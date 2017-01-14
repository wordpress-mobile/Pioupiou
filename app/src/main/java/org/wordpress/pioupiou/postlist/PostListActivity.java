package org.wordpress.pioupiou.postlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.postlist.DummyContent.PostItem;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

public class PostListActivity extends AppCompatActivity implements OnListFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
    }

    @Override
    public void onListFragmentInteraction(PostItem item) {
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
                // TODO: show the "New message" UI
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
