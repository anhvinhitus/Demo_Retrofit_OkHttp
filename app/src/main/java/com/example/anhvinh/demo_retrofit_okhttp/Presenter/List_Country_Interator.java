package com.example.anhvinh.demo_retrofit_okhttp.Presenter;

import android.util.Log;

import com.example.anhvinh.demo_retrofit_okhttp.Api.RequestApi;
import com.example.anhvinh.demo_retrofit_okhttp.Database.Worldpopulation_Repo;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.example.anhvinh.demo_retrofit_okhttp.Models.JSONObjectWorld;
import com.example.anhvinh.demo_retrofit_okhttp.Models.List_Country_Listener;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class List_Country_Interator {
    // Declare variable:
    private List_Country_Listener listener;
    private Retrofit retrofit;
    private List<Worldpopulation> listCountry;
    private Worldpopulation_Repo worldpopulation_repo;

    // Contructor:
    @Inject
    public List_Country_Interator(Retrofit retrofit, Worldpopulation_Repo worldpopulation_repo) {
        this.retrofit = retrofit;
        this.worldpopulation_repo = worldpopulation_repo;
    }

    public void setListCountry(List<Worldpopulation> listCountry) {
        this.listCountry = listCountry;
    }

    public void setPresenter(List_Country_Listener listener) {
        this.listener = listener;
    }

    public void getListCountry(boolean userCache, boolean isInternet) {
        if (listCountry == null || !userCache) {
            Log.d("Interator", "Load data");
            if (isInternet) {
                RequestApi requestApi = retrofit.create(RequestApi.class);
                requestApi.getListCountry()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<JSONObjectWorld>() {
                            @Override
                            public void accept(JSONObjectWorld jsonObjectWorld) throws Exception {
                                listCountry = jsonObjectWorld.getWorldpopulation();
                                saveData();
                            }
                        })
                        .subscribe();
            } else {
                worldpopulation_repo.loadAll()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<List<Worldpopulation>>() {
                            @Override
                            public void accept(List<Worldpopulation> worldpopulationList) throws Exception {
                                if (worldpopulationList != null) {
                                    listener.LoadListCountrySuccess(worldpopulationList);
                                } else
                                    listener.LoadListCountryError();
                            }
                        })
                        .subscribe();
            }
        } else {
            Log.d("Interator", "Exist Data");
            listener.LoadListCountrySuccess(listCountry);
        }
    }

    public void saveData() {
        if (listCountry != null) {
            worldpopulation_repo.saveAll(listCountry)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            listener.LoadListCountrySuccess(listCountry);
                        }
                    })
                    .subscribe();
            //listCountry = worldpopulation_repo.loadAll2();
        }
    }
}
