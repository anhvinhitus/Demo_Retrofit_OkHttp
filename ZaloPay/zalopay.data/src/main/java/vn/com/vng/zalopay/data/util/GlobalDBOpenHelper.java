package vn.com.vng.zalopay.data.util;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.DaoMaster;

/**
 * Created by khattn on 3/24/17.
 */

public class GlobalDBOpenHelper extends DaoMaster.OpenHelper {

    public GlobalDBOpenHelper(Context context, String name) {
        super(context, name, null);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        //Todo: apply db upgrade code here
        Timber.i("Upgrading schema from version %s to %s by dropping all tables", oldVersion, newVersion);
        vn.com.vng.zalopay.data.cache.global.DaoMaster.dropAllTables(db, true);
        onCreate(db);
    }

}