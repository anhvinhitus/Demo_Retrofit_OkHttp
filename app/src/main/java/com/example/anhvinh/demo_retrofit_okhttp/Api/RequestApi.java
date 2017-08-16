package com.example.anhvinh.demo_retrofit_okhttp.Api;

import com.example.anhvinh.demo_retrofit_okhttp.Models.JSONObjectWorld;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public interface RequestApi {
    @GET("jsonparsetutorial.txt")
    Observable<JSONObjectWorld> getListCountry();
    //Call<JSONObjectWorld> getListCountry();
}
