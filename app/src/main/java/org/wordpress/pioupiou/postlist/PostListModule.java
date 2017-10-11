package org.wordpress.pioupiou.postlist;

import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.PostStore;
import org.wordpress.android.fluxc.store.SiteStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PostListModule {
    @Singleton
    @Provides
    public PostListContract.Presenter providePostListPresenter(Dispatcher dispatcher, AccountStore accountStore,
                                                               PostStore postStore, SiteStore siteStore) {
        return new PostListPresenter(dispatcher, accountStore, postStore, siteStore);
    }
}
