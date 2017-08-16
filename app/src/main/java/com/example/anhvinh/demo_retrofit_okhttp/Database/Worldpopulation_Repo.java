package com.example.anhvinh.demo_retrofit_okhttp.Database;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.DaoSession;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;

import java.util.List;

/**
 * Created by AnhVinh on 16/08/2017.
 */

public class Worldpopulation_Repo {
    private DaoSession daoSession;

    public Worldpopulation_Repo (DaoSession daoSession){
        this.daoSession = daoSession;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
    public void deleteAll() {
        this.daoSession.getWorldpopulationDao().deleteAll();
    };

    public List<Worldpopulation> loadAll(){
        return this.daoSession.getWorldpopulationDao().loadAll();
    };

    public void saveAll(List<Worldpopulation> worldpopulationList){
        //this.daoSession.getWorldpopulationDao().deleteAll();
        this.daoSession.getWorldpopulationDao().insertOrReplaceInTx(worldpopulationList);
    }
}
