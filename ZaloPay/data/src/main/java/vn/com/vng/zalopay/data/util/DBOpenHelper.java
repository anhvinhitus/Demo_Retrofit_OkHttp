package vn.com.vng.zalopay.data.util;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

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
        // Debug
        DaoMaster.dropAllTables(db, true);
        onCreate(db);
    }

}
