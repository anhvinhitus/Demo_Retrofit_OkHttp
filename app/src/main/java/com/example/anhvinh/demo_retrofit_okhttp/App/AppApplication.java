package com.example.anhvinh.demo_retrofit_okhttp.App;

import android.app.Application;

import com.example.anhvinh.demo_retrofit_okhttp.App.Modules.AppModule;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class AppApplication extends Application {
    // Declare Variable:
    public static AppComponent appComponent;
    public static AppApplication application;
    public static AppApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        application = this;
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }
}
