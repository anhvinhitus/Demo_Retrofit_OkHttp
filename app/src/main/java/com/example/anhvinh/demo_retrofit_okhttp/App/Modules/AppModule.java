package com.example.anhvinh.demo_retrofit_okhttp.App.Modules;

/**
 * Created by AnhVinh on 09/08/2017.
 */

import com.example.anhvinh.demo_retrofit_okhttp.Presenter.List_Country_Interator;
import com.example.anhvinh.demo_retrofit_okhttp.Presenter.List_Country_Presenter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.anhvinh.demo_retrofit_okhttp.BuildConfig.BASE_URL;

/**
 * Created by AnhVinh on 09/08/2017.
 */
@Module
public class AppModule {
    @Singleton
    @Provides
    Gson provide_Gson() {
        return new GsonBuilder().create();
    }

    @Singleton
    @Provides
    List_Country_Presenter provide_ListCountry_Presenter(List_Country_Interator interator) {
        return new List_Country_Presenter(interator);
    }

    @Singleton
    @Provides
    Retrofit provide_Retrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Singleton
    @Provides
    List_Country_Interator provide_Interator(Retrofit retrofit) {
        return new List_Country_Interator(retrofit);
    }
}