package org.wordpress.pioupiou.misc;

import org.wordpress.android.fluxc.module.AppContextModule;
import org.wordpress.android.fluxc.module.ReleaseBaseModule;
import org.wordpress.android.fluxc.module.ReleaseNetworkModule;
import org.wordpress.android.fluxc.module.ReleaseStoreModule;
import org.wordpress.pioupiou.login.LoginActivity;
import org.wordpress.pioupiou.postlist.PostListActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppContextModule.class,
        AppSecretsModule.class,
        ReleaseBaseModule.class,
        ReleaseNetworkModule.class,
        ReleaseStoreModule.class
})
public interface AppComponent {
    void inject(PioupiouApp object);
    void inject(LoginActivity object);
    void inject(PostListActivity object);
    void inject(PostUploadService object);
}

