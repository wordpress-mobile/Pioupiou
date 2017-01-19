package org.wordpress.pioupiou.postlist;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.postlist.PostFragment.OnListFragmentInteractionListener;

import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {
    private final List<PostModel> mValues; // Use a List<PostModel> instead
    private final OnListFragmentInteractionListener mListener;

    public PostRecyclerViewAdapter(List<PostModel> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    void newItems(List<PostModel> values) {
        mValues.clear();
        mValues.addAll(values);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PostModel item = mValues.get(position);
        holder.mItem = item;
        holder.mIdView.setText(String.valueOf(item.getId()));
        holder.mContentView.setText(item.getContent());
        holder.mDateView.setText(item.getDateCreated());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        if (!TextUtils.isEmpty(item.getLink())) {
            Picasso.with(holder.mView.getContext()).load(item.getLink()).placeholder(R.mipmap.ic_egg)
                    .into(holder.mImageView);
        }
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
        public PostModel mItem;

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
