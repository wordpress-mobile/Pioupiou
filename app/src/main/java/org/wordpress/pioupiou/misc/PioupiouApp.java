package org.wordpress.pioupiou.misc;

import android.app.Application;

import com.yarolegovich.wellsql.WellSql;

import org.wordpress.android.fluxc.module.AppContextModule;
import org.wordpress.android.fluxc.persistence.WellSqlConfig;

import timber.log.Timber;

public class PioupiouApp extends Application {
    private AppComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        // Init Dagger
        mComponent = DaggerAppComponent.builder()
                .appContextModule(new AppContextModule(getApplicationContext()))
                .build();
        component().inject(this);

        // Init WellSql
        WellSql.init(new WellSqlConfig(getApplicationContext()));

        // Init Timber
        Timber.plant(new Timber.DebugTree());
    }

    public AppComponent component() {
        return mComponent;
    }
}
