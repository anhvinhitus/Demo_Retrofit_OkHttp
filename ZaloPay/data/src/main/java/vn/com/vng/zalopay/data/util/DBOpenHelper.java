package vn.com.vng.zalopay.data.util;

import android.content.Context;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class DBOpenHelper extends DaoMaster.OpenHelper {

    public DBOpenHelper(Context context, String name) {
        super(context, name, null);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        //Todo: apply db upgrade code here
        Timber.i("Upgrading schema from version %s to %s by dropping all tables", oldVersion, newVersion);
        DaoMaster.dropAllTables(db, true);
        onCreate(db);
    }

}
