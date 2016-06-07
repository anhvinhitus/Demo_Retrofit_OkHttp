package vn.com.vng.zalopay.data.cache.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import vn.com.vng.zalopay.data.cache.model.AppResourceGD;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "APP_RESOURCE_GD".
*/
public class AppResourceGDDao extends AbstractDao<AppResourceGD, Void> {

    public static final String TABLENAME = "APP_RESOURCE_GD";

    /**
     * Properties of entity AppResourceGD.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Appid = new Property(0, int.class, "appid", false, "APPID");
        public final static Property Appname = new Property(1, String.class, "appname", false, "APPNAME");
        public final static Property Needdownloadrs = new Property(2, Integer.class, "needdownloadrs", false, "NEEDDOWNLOADRS");
        public final static Property Imageurl = new Property(3, String.class, "imageurl", false, "IMAGEURL");
        public final static Property Jsurl = new Property(4, String.class, "jsurl", false, "JSURL");
        public final static Property Status = new Property(5, Integer.class, "status", false, "STATUS");
        public final static Property Checksum = new Property(6, String.class, "checksum", false, "CHECKSUM");
        public final static Property StateDownload = new Property(7, Integer.class, "stateDownload", false, "STATE_DOWNLOAD");
        public final static Property TimeDownload = new Property(8, Long.class, "timeDownload", false, "TIME_DOWNLOAD");
        public final static Property NumRetry = new Property(9, Integer.class, "numRetry", false, "NUM_RETRY");
    };


    public AppResourceGDDao(DaoConfig config) {
        super(config);
    }
    
    public AppResourceGDDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"APP_RESOURCE_GD\" (" + //
                "\"APPID\" INTEGER NOT NULL UNIQUE ," + // 0: appid
                "\"APPNAME\" TEXT," + // 1: appname
                "\"NEEDDOWNLOADRS\" INTEGER," + // 2: needdownloadrs
                "\"IMAGEURL\" TEXT," + // 3: imageurl
                "\"JSURL\" TEXT," + // 4: jsurl
                "\"STATUS\" INTEGER," + // 5: status
                "\"CHECKSUM\" TEXT," + // 6: checksum
                "\"STATE_DOWNLOAD\" INTEGER," + // 7: stateDownload
                "\"TIME_DOWNLOAD\" INTEGER," + // 8: timeDownload
                "\"NUM_RETRY\" INTEGER);"); // 9: numRetry
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"APP_RESOURCE_GD\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, AppResourceGD entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getAppid());
 
        String appname = entity.getAppname();
        if (appname != null) {
            stmt.bindString(2, appname);
        }
 
        Integer needdownloadrs = entity.getNeeddownloadrs();
        if (needdownloadrs != null) {
            stmt.bindLong(3, needdownloadrs);
        }
 
        String imageurl = entity.getImageurl();
        if (imageurl != null) {
            stmt.bindString(4, imageurl);
        }
 
        String jsurl = entity.getJsurl();
        if (jsurl != null) {
            stmt.bindString(5, jsurl);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(6, status);
        }
 
        String checksum = entity.getChecksum();
        if (checksum != null) {
            stmt.bindString(7, checksum);
        }
 
        Integer stateDownload = entity.getStateDownload();
        if (stateDownload != null) {
            stmt.bindLong(8, stateDownload);
        }
 
        Long timeDownload = entity.getTimeDownload();
        if (timeDownload != null) {
            stmt.bindLong(9, timeDownload);
        }
 
        Integer numRetry = entity.getNumRetry();
        if (numRetry != null) {
            stmt.bindLong(10, numRetry);
        }
    }

    /** @inheritdoc */
    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    /** @inheritdoc */
    @Override
    public AppResourceGD readEntity(Cursor cursor, int offset) {
        AppResourceGD entity = new AppResourceGD( //
            cursor.getInt(offset + 0), // appid
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // appname
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // needdownloadrs
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // imageurl
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // jsurl
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // status
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // checksum
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // stateDownload
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // timeDownload
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9) // numRetry
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, AppResourceGD entity, int offset) {
        entity.setAppid(cursor.getInt(offset + 0));
        entity.setAppname(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setNeeddownloadrs(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setImageurl(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setJsurl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setStatus(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setChecksum(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setStateDownload(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setTimeDownload(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setNumRetry(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
     }
    
    /** @inheritdoc */
    @Override
    protected Void updateKeyAfterInsert(AppResourceGD entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    /** @inheritdoc */
    @Override
    public Void getKey(AppResourceGD entity) {
        return null;
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
