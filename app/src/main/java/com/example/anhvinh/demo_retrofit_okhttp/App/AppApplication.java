package com.example.anhvinh.demo_retrofit_okhttp.App;

import android.app.Application;

import com.example.anhvinh.demo_retrofit_okhttp.App.Modules.AppModule;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class AppApplication extends Application{
    // Declare Variable:
    public static AppComponent appComponent;
    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static AppComponent getAppComponent() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .build();
        return appComponent;
    }
}
