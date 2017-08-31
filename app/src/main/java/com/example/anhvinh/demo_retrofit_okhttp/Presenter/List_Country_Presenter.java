package com.example.anhvinh.demo_retrofit_okhttp.Presenter;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.example.anhvinh.demo_retrofit_okhttp.Models.List_Country_Listener;
import com.example.anhvinh.demo_retrofit_okhttp.View.Base.List_Country_View;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class List_Country_Presenter implements List_Country_Listener {
    // Declare Var:
    private List_Country_Interator model;
    private List_Country_View view;

    @Inject
    public List_Country_Presenter(List_Country_Interator model) {
        this.model = model;
        this.model.setPresenter(this);
    }

    public void setView(List_Country_View view) {
        this.view = view;
    }

    public void getData(boolean useCache, boolean isInternet) {
        view.reLoad();
        model.getListCountry(useCache, isInternet);
    }

    public void saveData(){
        model.saveData();
    }

    public void reLoad(boolean isInternet) {
        getData(false, isInternet);
    }

    @Override
    public void LoadListCountrySuccess(List<Worldpopulation> ListCountry) {
        if (ListCountry.size() > 0)
            view.showData(ListCountry);
        else
            view.showNoData();
    }

    @Override
    public void LoadListCountryError() {
        view.showError();
    }
}
