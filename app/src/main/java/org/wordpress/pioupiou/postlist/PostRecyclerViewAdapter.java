package org.wordpress.pioupiou.postlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.wordpress.android.fluxc.model.AccountModel;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.util.DateTimeUtils;
import org.wordpress.android.util.HtmlUtils;
import org.wordpress.android.util.ImageUtils;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.postlist.PostListFragment.OnListFragmentInteractionListener;

import java.text.BreakIterator;
import java.util.Date;
import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {
    private final List<PostModel> mPosts;
    private final OnListFragmentInteractionListener mListener;
    private final AccountModel mAccount;
    private final LayoutInflater mInflater;
    private final int mAvatarSz;

    public PostRecyclerViewAdapter(Context context,
                                   AccountModel account,
                                   List<PostModel> posts,
                                   OnListFragmentInteractionListener listener) {
        mInflater = LayoutInflater.from(context);
        mAccount = account;
        mPosts = posts;
        mListener = listener;
        mAvatarSz = context.getResources().getDimensionPixelSize(R.dimen.post_avatar);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PostModel post = mPosts.get(position);

        // TODO: we get the author from the account but it should be part of the post model
        holder.mAuthorView.setText(mAccount.getDisplayName());
        holder.mContentView.setText(makeExcerpt(post.getContent()));

        if (post.getDateCreated() != null) {
            Date date = DateTimeUtils.dateFromIso8601(post.getDateCreated());
            holder.mDateView.setText(DateUtils.getRelativeTimeSpanString(
                    date.getTime(),
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL));
        } else {
            holder.mDateView.setText("unknown");
        }

        Picasso.with(holder.itemView.getContext())
                .load(mAccount.getAvatarUrl())
                .placeholder(R.mipmap.ic_egg)
                .transform(mTransformation)
                .resize(mAvatarSz, mAvatarSz)
                .into(holder.mAvatarView);
    }

    private final Transformation mTransformation = new Transformation() {
        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap circular = ImageUtils.getCircularBitmap(source);
            source.recycle();
            return circular;
        }
        @Override
        public String key() {
            return "circular-avatar";
        }
    };

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    private static final int MAX_EXCERPT_LEN = 200;
    private static String makeExcerpt(final String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        String text = HtmlUtils.fastStripHtml(content);
        if (text.length() <= MAX_EXCERPT_LEN) {
            return text.trim();
        }

        StringBuilder result = new StringBuilder();
        BreakIterator wordIterator = BreakIterator.getWordInstance();
        wordIterator.setText(text);
        int start = wordIterator.first();
        int end = wordIterator.next();
        int totalLen = 0;
        while (end != BreakIterator.DONE) {
            String word = text.substring(start, end);
            result.append(word);
            totalLen += word.length();
            if (totalLen >= MAX_EXCERPT_LEN) {
                break;
            }
            start = end;
            end = wordIterator.next();
        }

        if (totalLen == 0) {
            return null;
        }

        return result.toString().trim() + "…";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mAvatarView;
        private final TextView mAuthorView;
        private final TextView mContentView;
        private final TextView mDateView;

        public ViewHolder(View view) {
            super(view);

            mAuthorView = (TextView) view.findViewById(R.id.author);
            mContentView = (TextView) view.findViewById(R.id.message);
            mAvatarView = (ImageView) view.findViewById(R.id.gravatar_view);
            mDateView = (TextView) view.findViewById(R.id.date);

            view.setOnClickListener(new View.OnClickListener() {
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
