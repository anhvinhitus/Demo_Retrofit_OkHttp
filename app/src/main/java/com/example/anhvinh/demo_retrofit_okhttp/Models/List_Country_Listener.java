package com.example.anhvinh.demo_retrofit_okhttp.Models;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;

import java.util.List;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public interface List_Country_Listener {
    public void LoadListCountrySuccess(List<Worldpopulation> ListCountry);
    public void LoadListCountryError();
}
