package org.wordpress.pioupiou.postlist;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.postlist.DummyContent.PostItem;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {
    private final List<PostItem> mValues; // Use a List<PostModel> instead
    private final OnListFragmentInteractionListener mListener;

    public PostRecyclerViewAdapter(List<PostItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
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
        PostItem item = mValues.get(position);
        holder.mItem = item;
        holder.mIdView.setText(item.authorName);
        holder.mContentView.setText(item.message);
        holder.mDateView.setText(DateUtils.getRelativeTimeSpanString(item.date, System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        Picasso.with(holder.mView.getContext()).load(item.gravatarUrl).placeholder(R.mipmap.ic_egg)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDateView;
        public PostItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
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
