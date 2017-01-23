package vn.com.vng.zalopay.data.cache.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

/**
 * Created by khattn on 1/23/17.
 */

public class LogGDDao extends AbstractDao<LogGD, Void> {

    public static final String TABLENAME = "LOG_APP_TRANS_ID";

    public static class Properties {
        public final static Property Apptransid = new Property(0, String.class, "apptransid", false, "APPTRANSID");
        public final static Property Appid = new Property(1, int.class, "appid", false, "APPID");
        public final static Property Step = new Property(2, int.class, "step", false, "STEP");
        public final static Property Step_result = new Property(3, int.class, "step_result", false, "STEPRESULT");
        public final static Property Pcmid = new Property(4, int.class, "pcmid", false, "PCMID");
        public final static Property Transtype = new Property(5, int.class, "transtype", false, "TRANSTYPE");
        public final static Property Transid = new Property(9, long.class, "transid", false, "TRANSID");
        public final static Property Sdk_result = new Property(6, int.class, "sdk_result", false, "SDKRESULT");
        public final static Property Server_result = new Property(7, int.class, "server_result", false, "SERVERRESULT");
        public final static Property Source = new Property(8, String.class, "source", false, "SOURCE");
    }

    public LogGDDao(DaoConfig config) {
        super(config);
    }

    public LogGDDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"LOG_APP_TRANS_ID\" (" + //
                "\"APPTRANSID\" TEXT NOT NULL UNIQUE ," + // 0: apptransid
                "\"APPID\" INTEGER," + // 1: appid
                "\"STEP\" INTEGER," + // 2: step
                "\"STEPRESULT\" INTEGER," + // 3: step_result
                "\"PCMID\" INTEGER," + // 4: pcmid
                "\"TRANSTYPE\" INTEGER," + // 5: transtype
                "\"TRANSID\" INTEGER," + // 6: transid
                "\"SDKRESULT\" INTEGER," + // 7: sdk_result
                "\"SERVERRESULT\" INTEGER," + // 8: server_result
                "\"SOURCE\" TEXT);"); // 9: source
    }

    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LOG_APP_TRANS_ID\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, LogGD entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.apptransid);

        int appid = entity.appid;
        if (appid != 0) {
            stmt.bindLong(2, appid);
        }

        int step = entity.step;
        if (step != 0) {
            stmt.bindLong(3, step);
        }

        int step_result = entity.step_result;
        if (step_result != 0) {
            stmt.bindLong(4, step_result);
        }

        int pcmid = entity.pcmid;
        if (pcmid != 0) {
            stmt.bindLong(5, pcmid);
        }

        int transtype = entity.transtype;
        if (transtype != 0) {
            stmt.bindLong(6, transtype);
        }

        long transid = entity.transid;
        if (transid != 0) {
            stmt.bindLong(7, transid);
        }

        int sdk_result = entity.sdk_result;
        if (sdk_result != 0) {
            stmt.bindLong(8, sdk_result);
        }

        int server_result = entity.server_result;
        if (server_result != 0) {
            stmt.bindLong(9, server_result);
        }

        String source = entity.source;
        if (source != null) {
            stmt.bindString(10, source);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, LogGD entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.apptransid);

        int appid = entity.appid;
        if (appid != 0) {
            stmt.bindLong(2, appid);
        }

        int step = entity.step;
        if (step != 0) {
            stmt.bindLong(3, step);
        }

        int step_result = entity.step_result;
        if (step_result != 0) {
            stmt.bindLong(4, step_result);
        }

        int pcmid = entity.pcmid;
        if (pcmid != 0) {
            stmt.bindLong(5, pcmid);
        }

        int transtype = entity.transtype;
        if (transtype != 0) {
            stmt.bindLong(6, transtype);
        }

        long transid = entity.transid;
        if (transid != 0) {
            stmt.bindLong(7, transid);
        }

        int sdk_result = entity.sdk_result;
        if (sdk_result != 0) {
            stmt.bindLong(8, sdk_result);
        }

        int server_result = entity.server_result;
        if (server_result != 0) {
            stmt.bindLong(9, server_result);
        }

        String source = entity.source;
        if (source != null) {
            stmt.bindString(10, source);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }

    @Override
    public LogGD readEntity(Cursor cursor, int offset) {
        LogGD entity = new LogGD();
        readEntity(cursor, entity, offset);
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, LogGD entity, int offset) {
        entity.apptransid = cursor.getString(offset + 0);
        entity.appid = cursor.isNull(offset + 1) ? 0 : cursor.getInt(offset + 1);
        entity.step = cursor.isNull(offset + 2) ? 0 : cursor.getInt(offset + 2);
        entity.step_result = cursor.isNull(offset + 3) ? 0 : cursor.getInt(offset + 3);
        entity.pcmid = cursor.isNull(offset + 4) ? 0 : cursor.getInt(offset + 4);
        entity.transtype = cursor.isNull(offset + 5) ? 0 : cursor.getInt(offset + 5);
        entity.transid = cursor.isNull(offset + 6) ? 0 : cursor.getLong(offset + 6);
        entity.sdk_result = cursor.isNull(offset + 7) ? 0 : cursor.getInt(offset + 7);
        entity.server_result = cursor.isNull(offset + 8) ? 0 : cursor.getInt(offset + 8);
        entity.source = cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9);
    }

    @Override
    protected final Void updateKeyAfterInsert(LogGD entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }

    @Override
    public Void getKey(LogGD entity) {
        return null;
    }

    @Override
    public boolean hasKey(LogGD entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
}
