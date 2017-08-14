package com.example.anhvinh.demo_retrofit_okhttp.View.Base;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;

import java.util.List;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public interface List_Country_View {
    void showError();
    void showData(List<Worldpopulation> listCountry);
    void showNoData();
    void reLoad();
}
