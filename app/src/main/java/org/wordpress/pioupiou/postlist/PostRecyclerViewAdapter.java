package org.wordpress.pioupiou.postlist;

import android.content.Context;
import android.support.v7.widget.CardView;
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
import org.wordpress.pioupiou.postlist.PostListFragment.OnListFragmentInteractionListener;

import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {
    private final List<PostModel> mPosts;
    private final OnListFragmentInteractionListener mListener;
    private final AccountModel mAccount;
    private final LayoutInflater mInflater;

    public PostRecyclerViewAdapter(Context context,
                                   AccountModel account,
                                   List<PostModel> posts,
                                   OnListFragmentInteractionListener listener) {
        mInflater = LayoutInflater.from(context);
        mAccount = account;
        mPosts = posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PostModel post = mPosts.get(position);
        String content = HtmlUtils.fastStripHtml(post.getContent());

        holder.mIdView.setText(mAccount.getDisplayName());
        holder.mContentView.setText(content);
        holder.mDateView.setText(post.getDateCreated());

        // TODO: avatar should be rounded
        Picasso.with(holder.itemView.getContext()).load(mAccount.getAvatarUrl()).placeholder(R.mipmap.ic_egg)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView mCardView;
        private final ImageView mImageView;
        private final TextView mIdView;
        private final TextView mContentView;
        private final TextView mDateView;

        public ViewHolder(View view) {
            super(view);
            mCardView = (CardView) view.findViewById(R.id.card_view);
            mIdView = (TextView) view.findViewById(R.id.author);
            mContentView = (TextView) view.findViewById(R.id.message);
            mImageView = (ImageView) view.findViewById(R.id.gravatar_view);
            mDateView = (TextView) view.findViewById(R.id.date);

            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        PostModel post = mPosts.get(position);
                        mListener.onListFragmentInteraction(post);
                    }
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
