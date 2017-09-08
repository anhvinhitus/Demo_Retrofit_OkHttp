package com.example.anhvinh.demo_retrofit_okhttp.Database;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.DaoSession;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;


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

    public Observable<List<Worldpopulation>> loadAll() {
        return Observable.fromCallable(new Callable<List<Worldpopulation>>() {
            @Override
            public List<Worldpopulation> call() throws Exception {
                return daoSession.getWorldpopulationDao().loadAll();
            }
        });
    }

    public Observable<Boolean> saveAll(final List<Worldpopulation> worldpopulationList) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                deleteAll();
                daoSession.getWorldpopulationDao().insertOrReplaceInTx(worldpopulationList);
                return true;
            }
        });
    }

}
