package vn.com.vng.zalopay.data.cache.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

@Singleton
public class AppDAOHelper {

    private final String DATABASE_NAME = this.getClass().getSimpleName();

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    private AppInfoHelper appInfoHelper;

    @Inject
    public AppDAOHelper(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DATABASE_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        appInfoHelper = new AppInfoHelper(db, daoSession.getAppInfoDao());
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public AppInfoHelper getAppInfoHelper() {
        return appInfoHelper;
    }

    public void clearUserRelatedData() {
        //clear data
    }
}
