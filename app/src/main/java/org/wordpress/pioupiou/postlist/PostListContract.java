package org.wordpress.pioupiou.postlist;

import android.support.annotation.NonNull;

import org.wordpress.android.fluxc.model.AccountModel;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.pioupiou.BasePresenter;
import org.wordpress.pioupiou.BaseView;

import java.util.List;

public interface PostListContract {
    interface View extends BaseView<Presenter> {
        void showPosts(AccountModel accountModel, List<PostModel> posts);
        void showProgress(boolean show);
        void showError(String message);
        void setLoadingIndicator(boolean active);
    }

    interface Presenter extends BasePresenter<View> {
        void fetchPosts();
        void publishPost(@NonNull String content);
    }
}
