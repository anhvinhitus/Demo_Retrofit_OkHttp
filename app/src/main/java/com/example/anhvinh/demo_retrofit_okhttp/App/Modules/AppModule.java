package com.example.anhvinh.demo_retrofit_okhttp.App.Modules;

/**
 * Created by AnhVinh on 09/08/2017.
 */

import android.app.Application;
import android.content.Context;

import com.example.anhvinh.demo_retrofit_okhttp.Database.Worldpopulation_Repo;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.DaoMaster;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.DaoSession;
import com.example.anhvinh.demo_retrofit_okhttp.Presenter.List_Country_Interator;
import com.example.anhvinh.demo_retrofit_okhttp.Presenter.List_Country_Presenter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;

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
    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Singleton
    @Provides
    Context provide_Context() {
        return application.getApplicationContext();
    }

    @Singleton
    @Provides
    Gson provide_Gson() {
        return new GsonBuilder().create();
    }

    @Singleton
    @Provides
    DaoSession provide_DaoSession(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "worldpopulation-db");
        Database db = helper.getWritableDb();
        return new DaoMaster(db).newSession();
    }

    @Singleton
    @Provides
    Worldpopulation_Repo provide_DaoSession_Repo(DaoSession daoSession){
        return new Worldpopulation_Repo(daoSession);
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
    List_Country_Interator provide_Interator(Retrofit retrofit, Worldpopulation_Repo worldpopulation_repo) {
        return new List_Country_Interator(retrofit, worldpopulation_repo);
    }
}