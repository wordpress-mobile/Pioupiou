package org.wordpress.pioupiou.postlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.wordpress.android.fluxc.model.AccountModel;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.util.HtmlUtils;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {
    private final List<PostModel> mPosts;
    private final OnListFragmentInteractionListener mListener;
    private final AccountModel mAccount;

    public PostRecyclerViewAdapter(@NonNull AccountModel account,
                                   @NonNull List<PostModel> posts,
                                   OnListFragmentInteractionListener listener) {
        mAccount = account;
        mPosts = posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PostModel post = mPosts.get(position);
        String content = HtmlUtils.fastStripHtml(post.getContent());

        holder.mItem = post;
        holder.mIdView.setText(mAccount.getDisplayName());
        holder.mContentView.setText(content);
        holder.mDateView.setText(post.getDateCreated());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        // TODO: avatar should be rounded
        Picasso.with(holder.itemView.getContext()).load(mAccount.getAvatarUrl()).placeholder(R.mipmap.ic_egg)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mImageView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDateView;
        public PostModel mItem;

        public ViewHolder(View view) {
            super(view);
            mIdView = (TextView) view.findViewById(R.id.author);
            mContentView = (TextView) view.findViewById(R.id.message);
            mImageView = (ImageView) view.findViewById(R.id.gravatar_view);
            mDateView = (TextView) view.findViewById(R.id.date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
