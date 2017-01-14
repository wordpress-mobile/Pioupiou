package org.wordpress.pioupiou.misc;

import android.text.TextUtils;

import org.wordpress.android.fluxc.network.rest.wpcom.auth.AppSecrets;

import java.lang.reflect.Field;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class AppSecretsModule {
    public String getStringBuildConfigValue(String fieldName) {
        try {
            String packageName = getClass().getPackage().getName();
            Class<?> clazz = Class.forName(packageName + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return (String) field.get(null);
        } catch (Exception e) {
            return "";
        }
    }

    @Provides
    public AppSecrets provideAppSecrets() {
        String appId = getStringBuildConfigValue("OAUTH_APP_ID");
        String appSecret = getStringBuildConfigValue("OAUTH_APP_SECRET");
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appSecret)) {
            Timber.e("OAUTH_APP_ID or OAUTH_APP_SECRET is empty, check your gradle.properties");
        }
        return new AppSecrets(appId, appSecret);
    }
}
