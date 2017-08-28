package com.example.anhvinh.demo_retrofit_okhttp.App;

import com.example.anhvinh.demo_retrofit_okhttp.App.Modules.AppModule;
import com.example.anhvinh.demo_retrofit_okhttp.View.DetailItem.DetailItemFragment;
import com.example.anhvinh.demo_retrofit_okhttp.View.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by AnhVinh on 09/08/2017.
 */
@Singleton
@Component (modules = {AppModule.class})
public interface AppComponent {
    void inject (MainActivity mainActivity);
    void inject (DetailItemFragment detailItemFragment);
}
