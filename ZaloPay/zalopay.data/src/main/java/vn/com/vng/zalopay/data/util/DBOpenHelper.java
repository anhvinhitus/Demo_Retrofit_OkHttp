package vn.com.vng.zalopay.data.util;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.UBCViewDao;
import vn.com.vng.zalopay.data.cache.model.ZPCViewDao;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class DBOpenHelper extends DaoMaster.OpenHelper {

    public DBOpenHelper(Context context, String name) {
        super(context, name, null);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        ZPCViewDao.createView(db, false);
        UBCViewDao.createView(db, false);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        //Todo: apply db upgrade code here
        Timber.i("Upgrading schema from version %s to %s by dropping all tables", oldVersion, newVersion);
        DaoMaster.dropAllTables(db, true);
        ZPCViewDao.dropView(db, true);
        UBCViewDao.dropView(db, true);
        onCreate(db);
    }

}
