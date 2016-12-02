package vn.com.vng.zalopay.data.cache.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SENT_BUNDLE_GD".
*/
public class SentBundleGDDao extends AbstractDao<SentBundleGD, Long> {

    public static final String TABLENAME = "SENT_BUNDLE_GD";

    /**
     * Properties of entity SentBundleGD.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property SenderZaloPayID = new Property(1, String.class, "senderZaloPayID", false, "SENDER_ZALO_PAY_ID");
        public final static Property Type = new Property(2, Integer.class, "type", false, "TYPE");
        public final static Property CreateTime = new Property(3, Long.class, "createTime", false, "CREATE_TIME");
        public final static Property LastOpenTime = new Property(4, Long.class, "lastOpenTime", false, "LAST_OPEN_TIME");
        public final static Property TotalLuck = new Property(5, Integer.class, "totalLuck", false, "TOTAL_LUCK");
        public final static Property NumOfOpenedPakages = new Property(6, Integer.class, "numOfOpenedPakages", false, "NUM_OF_OPENED_PAKAGES");
        public final static Property NumOfPackages = new Property(7, Integer.class, "numOfPackages", false, "NUM_OF_PACKAGES");
        public final static Property SendMessage = new Property(8, String.class, "sendMessage", false, "SEND_MESSAGE");
        public final static Property Status = new Property(9, Integer.class, "status", false, "STATUS");
    }

    private DaoSession daoSession;


    public SentBundleGDDao(DaoConfig config) {
        super(config);
    }
    
    public SentBundleGDDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SENT_BUNDLE_GD\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                "\"SENDER_ZALO_PAY_ID\" TEXT NOT NULL ," + // 1: senderZaloPayID
                "\"TYPE\" INTEGER," + // 2: type
                "\"CREATE_TIME\" INTEGER," + // 3: createTime
                "\"LAST_OPEN_TIME\" INTEGER," + // 4: lastOpenTime
                "\"TOTAL_LUCK\" INTEGER," + // 5: totalLuck
                "\"NUM_OF_OPENED_PAKAGES\" INTEGER," + // 6: numOfOpenedPakages
                "\"NUM_OF_PACKAGES\" INTEGER," + // 7: numOfPackages
                "\"SEND_MESSAGE\" TEXT," + // 8: sendMessage
                "\"STATUS\" INTEGER);"); // 9: status
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SENT_BUNDLE_GD\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SentBundleGD entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindString(2, entity.getSenderZaloPayID());
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(3, type);
        }
 
        Long createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(4, createTime);
        }
 
        Long lastOpenTime = entity.getLastOpenTime();
        if (lastOpenTime != null) {
            stmt.bindLong(5, lastOpenTime);
        }
 
        Integer totalLuck = entity.getTotalLuck();
        if (totalLuck != null) {
            stmt.bindLong(6, totalLuck);
        }
 
        Integer numOfOpenedPakages = entity.getNumOfOpenedPakages();
        if (numOfOpenedPakages != null) {
            stmt.bindLong(7, numOfOpenedPakages);
        }
 
        Integer numOfPackages = entity.getNumOfPackages();
        if (numOfPackages != null) {
            stmt.bindLong(8, numOfPackages);
        }
 
        String sendMessage = entity.getSendMessage();
        if (sendMessage != null) {
            stmt.bindString(9, sendMessage);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(10, status);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SentBundleGD entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindString(2, entity.getSenderZaloPayID());
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(3, type);
        }
 
        Long createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(4, createTime);
        }
 
        Long lastOpenTime = entity.getLastOpenTime();
        if (lastOpenTime != null) {
            stmt.bindLong(5, lastOpenTime);
        }
 
        Integer totalLuck = entity.getTotalLuck();
        if (totalLuck != null) {
            stmt.bindLong(6, totalLuck);
        }
 
        Integer numOfOpenedPakages = entity.getNumOfOpenedPakages();
        if (numOfOpenedPakages != null) {
            stmt.bindLong(7, numOfOpenedPakages);
        }
 
        Integer numOfPackages = entity.getNumOfPackages();
        if (numOfPackages != null) {
            stmt.bindLong(8, numOfPackages);
        }
 
        String sendMessage = entity.getSendMessage();
        if (sendMessage != null) {
            stmt.bindString(9, sendMessage);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(10, status);
        }
    }

    @Override
    protected final void attachEntity(SentBundleGD entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public SentBundleGD readEntity(Cursor cursor, int offset) {
        SentBundleGD entity = new SentBundleGD( //
            cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // senderZaloPayID
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // type
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // createTime
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // lastOpenTime
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // totalLuck
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // numOfOpenedPakages
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // numOfPackages
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // sendMessage
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9) // status
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SentBundleGD entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setSenderZaloPayID(cursor.getString(offset + 1));
        entity.setType(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setCreateTime(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setLastOpenTime(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setTotalLuck(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setNumOfOpenedPakages(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setNumOfPackages(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setSendMessage(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setStatus(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SentBundleGD entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SentBundleGD entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SentBundleGD entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
