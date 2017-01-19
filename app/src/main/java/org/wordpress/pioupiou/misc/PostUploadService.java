package org.wordpress.pioupiou.misc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.PostActionBuilder;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.post.PostStatus;
import org.wordpress.android.fluxc.store.PostStore.OnPostUploaded;
import org.wordpress.android.fluxc.store.PostStore.PostError;
import org.wordpress.android.fluxc.store.PostStore.RemotePostPayload;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;
import org.wordpress.pioupiou.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class PostUploadService extends Service {
    private static final ArrayList<PostModel> mPostsList = new ArrayList<>();
    private static PostModel mCurrentUploadingPost = null;
    private static boolean mUseLegacyMode;
    private UploadPostTask mCurrentTask = null;

    private static final Set<Integer> mFirstPublishPosts = new HashSet<>();

    private Context mContext;

    @Inject
    Dispatcher mDispatcher;
    @Inject
    SiteStore mSiteStore;

    /**
     * Adds a post to the queue.
     */
    public static void addPostToUpload(PostModel post) {
        synchronized (mPostsList) {
            mPostsList.add(post);
        }
    }

    /**
     * Adds a post to the queue and tracks post analytics.
     * To be used only the first time a post is uploaded, i.e. when its status changes from local draft or remote draft
     * to published.
     */
    public static void addPostToUploadAndTrackAnalytics(PostModel post) {
        synchronized (mFirstPublishPosts) {
            mFirstPublishPosts.add(post.getId());
        }
        synchronized (mPostsList) {
            mPostsList.add(post);
        }
    }

    public static void setLegacyMode(boolean enabled) {
        mUseLegacyMode = enabled;
    }

    /**
     * Returns true if the passed post is either uploading or waiting to be uploaded.
     */
    public static boolean isPostUploading(PostModel post) {
        // first check the currently uploading post
        if (mCurrentUploadingPost != null && mCurrentUploadingPost.getId() == post.getId()) {
            return true;
        }
        // then check the list of posts waiting to be uploaded
        if (mPostsList.size() > 0) {
            synchronized (mPostsList) {
                for (PostModel queuedPost : mPostsList) {
                    if (queuedPost.getId() == post.getId()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((PioupiouApp) getApplication()).component().inject(this);
        mDispatcher.register(this);
        mContext = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel current task, it will reset post from "uploading" to "local draft"
        if (mCurrentTask != null) {
            AppLog.d(T.POSTS, "cancelling current upload task");
            mCurrentTask.cancel(true);
        }
        mDispatcher.unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (mPostsList) {
            if (mPostsList.size() == 0 || mContext == null) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        uploadNextPost();
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

    private void uploadNextPost() {
        synchronized (mPostsList) {
            if (mCurrentTask == null) { //make sure nothing is running
                mCurrentUploadingPost = null;
                if (mPostsList.size() > 0) {
                    mCurrentUploadingPost = mPostsList.remove(0);
                    mCurrentTask = new UploadPostTask();
                    mCurrentTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mCurrentUploadingPost);
                } else {
                    stopSelf();
                }
            }
        }
    }

    private void finishUpload() {
        synchronized (mPostsList) {
            mCurrentTask = null;
            mCurrentUploadingPost = null;
        }
        uploadNextPost();
    }

    private class UploadPostTask extends AsyncTask<PostModel, Boolean, Boolean> {
        private PostModel mPost;
        private SiteModel mSite;

        private String mErrorMessage = "";
        private boolean mIsMediaError = false;
        private int featuredImageID = -1;

        // Used for analytics
        private boolean mHasImage, mHasVideo, mHasCategory;

        @Override
        protected Boolean doInBackground(PostModel... posts) {
            mPost = posts[0];

            String postTitle = ""; //TextUtils.isEmpty(mPost.getTitle()) ? getString(R.string.untitled) : mPost.getTitle();
          /*  String uploadingPostTitle = String.format(getString(R.string.posting_post), postTitle);
            String uploadingPostMessage = String.format(
                    getString(R.string.sending_content),
                    mPost.isPage() ? getString(R.string.page).toLowerCase() : getString(R.string.post).toLowerCase()
            );
*/

            mSite = mSiteStore.getSiteByLocalId(mPost.getLocalSiteId());
            if (mSite == null) {
                mErrorMessage = mContext.getString(R.string.blog_not_found);
                return false;
            }

            if (TextUtils.isEmpty(mPost.getStatus())) {
                mPost.setStatus(PostStatus.PUBLISHED.toString());
            }


            // If media file upload failed, let's stop here and prompt the user
            if (mIsMediaError) {
                return false;
            }

            // Support for legacy editor - images are identified as featured as they're being uploaded with the post
            if (mUseLegacyMode && featuredImageID != -1) {
                mPost.setFeaturedImageId(featuredImageID);
            }


            RemotePostPayload payload = new RemotePostPayload(mPost, mSite);
            mDispatcher.dispatch(PostActionBuilder.newPushPostAction(payload));

            // Track analytics only if the post is newly published
            if (mFirstPublishPosts.contains(mPost.getId())) {
                trackUploadAnalytics();
            }

            return true;
        }

        private boolean hasGallery() {
            Pattern galleryTester = Pattern.compile("\\[.*?gallery.*?\\]");
            Matcher matcher = galleryTester.matcher(mPost.getContent());
            return matcher.find();
        }

        private void trackUploadAnalytics() {
        }


        private void setUploadPostErrorMessage(Exception e) {
            mErrorMessage = mContext.getResources().getText(R.string.error_upload).toString()
                    + " - " + e.getMessage();
            mIsMediaError = false;
            AppLog.e(T.EDITOR, mErrorMessage, e);
        }

    }

    private File createTempUploadFile(String fileExtension) throws IOException {
        return File.createTempFile("wp-", fileExtension, mContext.getCacheDir());
    }

    /**
     * Returns an error message string for a failed post upload.
     */
    private String buildErrorMessage(PostModel post, PostError error) {
        // TODO: We should interpret event.error.type and pass our own string rather than use event.error.message
        String postType = mContext.getResources().getText(post.isPage() ? R.string.page : R.string.post).toString().toLowerCase();
        String errorMessage = String.format(mContext.getResources().getText(R.string.error_upload).toString(), postType);
        errorMessage += ": " + error.message;
        return errorMessage;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostUploaded(OnPostUploaded event) {
        SiteModel site = mSiteStore.getSiteByLocalId(event.post.getLocalSiteId());

        if (event.isError()) {
            AppLog.e(T.EDITOR, "Post upload failed. " + event.error.type + ": " + event.error.message);
            mFirstPublishPosts.remove(event.post.getId());
        } else {
            // TODO: MediaStore?
            // WordPress.wpDB.deleteMediaFilesForPost(mPost);
            boolean isFirstTimePublish = mFirstPublishPosts.remove(event.post.getId());
        }

        finishUpload();
    }
}
