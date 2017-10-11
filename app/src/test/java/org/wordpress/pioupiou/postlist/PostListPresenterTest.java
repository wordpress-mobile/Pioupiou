package org.wordpress.pioupiou.postlist;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.model.AccountModel;
import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.PostStore.OnPostUploaded;
import org.wordpress.android.fluxc.store.SiteStore;

import java.util.ArrayList;
import java.util.List;

public class PostListPresenterTest {
    @Mock private PostListContract.View mPostListView;

    @Mock private Dispatcher mDispatcher;
    @Mock private AccountStore mAccountStore;
    @Mock private PostStore mPostStore;
    @Mock private SiteStore mSiteStore;

    private AccountModel mMockAccountModel;
    private SiteModel mMockSiteModel;

    private PostListPresenter mPostListPresenter;

    @Before
    public void setup() {
        // Inject the annotated mocks
        MockitoAnnotations.initMocks(this);

        // Set up a dummy AccountModel
        mMockAccountModel = new AccountModel();
        Mockito.doReturn(mMockAccountModel).when(mAccountStore).getAccount();
    }

    @Test
    public void fetchPostsAndLoadIntoView() {
        // Set up the Presenter and attach a mocked View
        mPostListPresenter = Mockito.spy(new PostListPresenter(mDispatcher, mAccountStore, mPostStore, mSiteStore));
        mPostListPresenter.takeView(mPostListView);

        // Set up dummy SiteModel
        mMockSiteModel = new SiteModel();
        mMockSiteModel.setId(1);
        Mockito.doReturn(mMockSiteModel).when(mPostListPresenter).getSite();

        mPostListPresenter.fetchPosts();

        // The loading indicator should have been started
        Mockito.verify(mPostListView).setLoadingIndicator(true);

        // Mock the return of mPostStore.getPostsForSite()
        PostModel postModel = new PostModel();
        List<PostModel> postList = new ArrayList<>();
        postList.add(postModel);
        Mockito.doReturn(postList).when(mPostStore).getPostsForSite(Mockito.any(SiteModel.class));

        // Simulate a successful OnPostUploaded event
        OnPostUploaded onPostUploaded = new OnPostUploaded(postModel);
        mPostListPresenter.onPostUploaded(onPostUploaded);

        // The progress view should have been disabled, and showPosts called with the latest post list
        Mockito.verify(mPostListView).showProgress(false);
        Mockito.verify(mPostListView).showPosts(mMockAccountModel, postList);
    }
}
