package vn.com.vng.zalopay.data.util;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.AppResourceGDDao;
import vn.com.vng.zalopay.data.cache.model.ContactGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DataManifestDao;
import vn.com.vng.zalopay.data.cache.model.MerchantUserDao;
import vn.com.vng.zalopay.data.cache.model.NotificationGDDao;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGDDao;
import vn.com.vng.zalopay.data.cache.model.TransactionFragmentGDDao;
import vn.com.vng.zalopay.data.cache.model.TransactionLogDao;
import vn.com.vng.zalopay.data.cache.model.ZaloPayProfileGDDao;
import vn.com.vng.zalopay.data.cache.model.ZaloProfileGDDao;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class DBOpenHelper extends DaoMaster.OpenHelper {

    public static final int SCHEMA_VERSION_RELEASE_2_14 = 61;
    public static final int SCHEMA_VERSION_RELEASE_2_15 = DaoMaster.SCHEMA_VERSION;

    public DBOpenHelper(Context context, String name) {
        super(context, name, null);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        int version = oldVersion;

        if (version == SCHEMA_VERSION_RELEASE_2_14) {
            upgradeVersion214ToVersion215(db);
            version = SCHEMA_VERSION_RELEASE_2_15;
        }

        if (version != newVersion) {
            DaoMaster.dropAllTables(db, true);
            onCreate(db);
            version = newVersion;
        }
    }

    private void upgradeVersion214ToVersion215(Database db) {
        Timber.i("Upgrade version 214 to version 215");

        //drop all table, except TransferRecentDao
        AppResourceGDDao.dropTable(db, true);
        TransactionLogDao.dropTable(db, true);
        DataManifestDao.dropTable(db, true);
        ZaloProfileGDDao.dropTable(db, true);
        ZaloPayProfileGDDao.dropTable(db, true);
        ContactGDDao.dropTable(db, true);
        NotificationGDDao.dropTable(db, true);
        ReceivePackageGDDao.dropTable(db, true);
        MerchantUserDao.dropTable(db, true);
        TransactionFragmentGDDao.dropTable(db, true);


        //Create all table, except TransferRecentDao
        AppResourceGDDao.createTable(db, false);
        TransactionLogDao.createTable(db, false);
        DataManifestDao.createTable(db, false);
        ZaloProfileGDDao.createTable(db, false);
        ZaloPayProfileGDDao.createTable(db, false);
        ContactGDDao.createTable(db, false);
        NotificationGDDao.createTable(db, false);
        ReceivePackageGDDao.createTable(db, false);
        MerchantUserDao.createTable(db, false);
        TransactionFragmentGDDao.createTable(db, false);
    }

}
