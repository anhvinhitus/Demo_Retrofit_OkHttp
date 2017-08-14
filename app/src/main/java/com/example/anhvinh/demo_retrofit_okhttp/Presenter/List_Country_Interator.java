package com.example.anhvinh.demo_retrofit_okhttp.Presenter;

import android.util.Log;

import com.example.anhvinh.demo_retrofit_okhttp.Api.RequestApi;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.example.anhvinh.demo_retrofit_okhttp.Models.JSONObjectWorld;
import com.example.anhvinh.demo_retrofit_okhttp.Models.List_Country_Listener;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class List_Country_Interator {
    // Declare variable:
    private List_Country_Listener listener;
    private Retrofit retrofit;
    private List<Worldpopulation> listCountry;

    // Contructor:
    @Inject
    public List_Country_Interator(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public void setListCountry(List<Worldpopulation> listCountry) {
        this.listCountry = listCountry;
    }

    public void setPresenter(List_Country_Listener listener) {
        this.listener = listener;
    }

    public void getListCountry(boolean userCache) {
        if (listCountry == null || !userCache) {
            Log.d("Interator", "Load data");
            loadDatafromJSON();
        } else {
            Log.d("Interator", "Exist Data");
            listener.LoadListCountrySuccess(listCountry);
        }
    }

    private void loadDatafromJSON() {
        RequestApi requestApi = retrofit.create(RequestApi.class);
        Call<JSONObjectWorld> call = requestApi.getListCountry();
        call.enqueue(new Callback<JSONObjectWorld>() {
            @Override
            public void onResponse(Call<JSONObjectWorld> call, Response<JSONObjectWorld> response) {
                listCountry = response.body().getWorldpopulation();
                if (listener != null) {
                    if (listCountry != null) {
                        listener.LoadListCountrySuccess(listCountry);
                    } else listener.LoadListCountryError();
                } else {
                    throw new RuntimeException("Please set callback for Load Data from JSON");
                }
            }

            @Override
            public void onFailure(Call<JSONObjectWorld> call, Throwable t) {
                listener.LoadListCountryError();
                Log.d("Error Interator", t.getMessage());
            }
        });
    }
}
