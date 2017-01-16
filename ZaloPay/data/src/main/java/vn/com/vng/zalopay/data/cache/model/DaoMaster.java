package vn.com.vng.zalopay.data.cache.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import org.greenrobot.greendao.AbstractDaoMaster;
import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.identityscope.IdentityScopeType;


// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/**
 * Master of DAO (schema version 55): knows all DAOs.
 */
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 55;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(Database db, boolean ifNotExists) {
        AppResourceGDDao.createTable(db, ifNotExists);
        PaymentTransTypeGDDao.createTable(db, ifNotExists);
        TransactionLogDao.createTable(db, ifNotExists);
        TransactionLogBackupDao.createTable(db, ifNotExists);
        DataManifestDao.createTable(db, ifNotExists);
        BankCardGDDao.createTable(db, ifNotExists);
        ZaloFriendGDDao.createTable(db, ifNotExists);
        ZaloPayProfileGDDao.createTable(db, ifNotExists);
        ContactGDDao.createTable(db, ifNotExists);
        TransferRecentDao.createTable(db, ifNotExists);
        NotificationGDDao.createTable(db, ifNotExists);
        SentBundleSummaryDBDao.createTable(db, ifNotExists);
        ReceivePacketSummaryDBDao.createTable(db, ifNotExists);
        BundleGDDao.createTable(db, ifNotExists);
        PackageInBundleGDDao.createTable(db, ifNotExists);
        SentBundleGDDao.createTable(db, ifNotExists);
        ReceivePackageGDDao.createTable(db, ifNotExists);
        RedPacketAppInfoGDDao.createTable(db, ifNotExists);
        MerchantUserDao.createTable(db, ifNotExists);
    }

    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(Database db, boolean ifExists) {
        AppResourceGDDao.dropTable(db, ifExists);
        PaymentTransTypeGDDao.dropTable(db, ifExists);
        TransactionLogDao.dropTable(db, ifExists);
        TransactionLogBackupDao.dropTable(db, ifExists);
        DataManifestDao.dropTable(db, ifExists);
        BankCardGDDao.dropTable(db, ifExists);
        ZaloFriendGDDao.dropTable(db, ifExists);
        ZaloPayProfileGDDao.dropTable(db, ifExists);
        ContactGDDao.dropTable(db, ifExists);
        TransferRecentDao.dropTable(db, ifExists);
        NotificationGDDao.dropTable(db, ifExists);
        SentBundleSummaryDBDao.dropTable(db, ifExists);
        ReceivePacketSummaryDBDao.dropTable(db, ifExists);
        BundleGDDao.dropTable(db, ifExists);
        PackageInBundleGDDao.dropTable(db, ifExists);
        SentBundleGDDao.dropTable(db, ifExists);
        ReceivePackageGDDao.dropTable(db, ifExists);
        RedPacketAppInfoGDDao.dropTable(db, ifExists);
        MerchantUserDao.dropTable(db, ifExists);
    }

    /**
     * WARNING: Drops all table on Upgrade! Use only during development.
     * Convenience method using a {@link DevOpenHelper}.
     */
    public static DaoSession newDevSession(Context context, String name) {
        Database db = new DevOpenHelper(context, name).getWritableDb();
        DaoMaster daoMaster = new DaoMaster(db);
        return daoMaster.newSession();
    }

    public DaoMaster(SQLiteDatabase db) {
        this(new StandardDatabase(db));
    }

    public DaoMaster(Database db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(AppResourceGDDao.class);
        registerDaoClass(PaymentTransTypeGDDao.class);
        registerDaoClass(TransactionLogDao.class);
        registerDaoClass(TransactionLogBackupDao.class);
        registerDaoClass(DataManifestDao.class);
        registerDaoClass(BankCardGDDao.class);
        registerDaoClass(ZaloFriendGDDao.class);
        registerDaoClass(ZaloPayProfileGDDao.class);
        registerDaoClass(ContactGDDao.class);
        registerDaoClass(TransferRecentDao.class);
        registerDaoClass(NotificationGDDao.class);
        registerDaoClass(SentBundleSummaryDBDao.class);
        registerDaoClass(ReceivePacketSummaryDBDao.class);
        registerDaoClass(BundleGDDao.class);
        registerDaoClass(PackageInBundleGDDao.class);
        registerDaoClass(SentBundleGDDao.class);
        registerDaoClass(ReceivePackageGDDao.class);
        registerDaoClass(RedPacketAppInfoGDDao.class);
        registerDaoClass(MerchantUserDao.class);
    }

    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }

    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }

    /**
     * Calls {@link #createAllTables(Database, boolean)} in {@link #onCreate(Database)} -
     */
    public static abstract class OpenHelper extends DatabaseOpenHelper {
        public OpenHelper(Context context, String name) {
            super(context, name, SCHEMA_VERSION);
        }

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(Database db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }

    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name) {
            super(context, name);
        }

        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

}
