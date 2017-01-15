package org.wordpress.pioupiou.misc;

import org.wordpress.android.fluxc.network.rest.wpcom.auth.AppSecrets;
import org.wordpress.pioupiou.BuildConfig;

import dagger.Module;
import dagger.Provides;

@Module
public class AppSecretsModule {
    @Provides
    public AppSecrets provideAppSecrets() {
        return new AppSecrets(BuildConfig.OAUTH_APP_ID, BuildConfig.OAUTH_APP_SECRET);
    }
}
