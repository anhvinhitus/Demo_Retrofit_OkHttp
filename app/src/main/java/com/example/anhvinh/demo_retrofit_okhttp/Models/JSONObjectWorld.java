package com.example.anhvinh.demo_retrofit_okhttp.Models;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class JSONObjectWorld {
    @SerializedName("worldpopulation")
    @Expose
    private List<Worldpopulation> worldpopulation = null;

    public List<Worldpopulation> getWorldpopulation() {
        return worldpopulation;
    }

    public void setWorldpopulation(List<Worldpopulation> worldpopulation) {
        this.worldpopulation = worldpopulation;
    }
}
