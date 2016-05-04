package vn.com.vng.zalopay.data.cache.helper;

import android.database.sqlite.SQLiteDatabase;

import vn.com.vng.zalopay.data.cache.model.AppInfoDao;

/**
 * Created by AnhHieu on 5/3/16.
 */
public class AppInfoHelper {
    private SQLiteDatabase db;
    private AppInfoDao appInfoDao;

    public AppInfoHelper(SQLiteDatabase db, AppInfoDao songTableDao) {
        this.db = db;
        this.appInfoDao = songTableDao;
    }

    public AppInfoDao getAppInfoDao() {
        return appInfoDao;
    }

}
