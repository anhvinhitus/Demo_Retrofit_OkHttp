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
 * DAO for table "PAYMENT_TRANS_TYPE_GD".
*/
public class PaymentTransTypeGDDao extends AbstractDao<PaymentTransTypeGD, Long> {

    public static final String TABLENAME = "PAYMENT_TRANS_TYPE_GD";

    /**
     * Properties of entity PaymentTransTypeGD.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Transtype = new Property(0, long.class, "transtype", true, "TRANSTYPE");
        public final static Property Pmcid = new Property(1, long.class, "pmcid", false, "PMCID");
        public final static Property Pmcname = new Property(2, String.class, "pmcname", false, "PMCNAME");
        public final static Property Status = new Property(3, Integer.class, "status", false, "STATUS");
        public final static Property Minvalue = new Property(4, Long.class, "minvalue", false, "MINVALUE");
        public final static Property Maxvalue = new Property(5, Long.class, "maxvalue", false, "MAXVALUE");
        public final static Property Feerate = new Property(6, Float.class, "feerate", false, "FEERATE");
        public final static Property Minfee = new Property(7, Long.class, "minfee", false, "MINFEE");
        public final static Property Feecaltype = new Property(8, String.class, "feecaltype", false, "FEECALTYPE");
    }


    public PaymentTransTypeGDDao(DaoConfig config) {
        super(config);
    }
    
    public PaymentTransTypeGDDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PAYMENT_TRANS_TYPE_GD\" (" + //
                "\"TRANSTYPE\" INTEGER PRIMARY KEY NOT NULL ," + // 0: transtype
                "\"PMCID\" INTEGER NOT NULL ," + // 1: pmcid
                "\"PMCNAME\" TEXT," + // 2: pmcname
                "\"STATUS\" INTEGER," + // 3: status
                "\"MINVALUE\" INTEGER," + // 4: minvalue
                "\"MAXVALUE\" INTEGER," + // 5: maxvalue
                "\"FEERATE\" REAL," + // 6: feerate
                "\"MINFEE\" INTEGER," + // 7: minfee
                "\"FEECALTYPE\" TEXT);"); // 8: feecaltype
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_PAYMENT_TRANS_TYPE_GD_TRANSTYPE_PMCID ON PAYMENT_TRANS_TYPE_GD" +
                " (\"TRANSTYPE\",\"PMCID\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PAYMENT_TRANS_TYPE_GD\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, PaymentTransTypeGD entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getTranstype());
        stmt.bindLong(2, entity.getPmcid());
 
        String pmcname = entity.getPmcname();
        if (pmcname != null) {
            stmt.bindString(3, pmcname);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(4, status);
        }
 
        Long minvalue = entity.getMinvalue();
        if (minvalue != null) {
            stmt.bindLong(5, minvalue);
        }
 
        Long maxvalue = entity.getMaxvalue();
        if (maxvalue != null) {
            stmt.bindLong(6, maxvalue);
        }
 
        Float feerate = entity.getFeerate();
        if (feerate != null) {
            stmt.bindDouble(7, feerate);
        }
 
        Long minfee = entity.getMinfee();
        if (minfee != null) {
            stmt.bindLong(8, minfee);
        }
 
        String feecaltype = entity.getFeecaltype();
        if (feecaltype != null) {
            stmt.bindString(9, feecaltype);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, PaymentTransTypeGD entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getTranstype());
        stmt.bindLong(2, entity.getPmcid());
 
        String pmcname = entity.getPmcname();
        if (pmcname != null) {
            stmt.bindString(3, pmcname);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(4, status);
        }
 
        Long minvalue = entity.getMinvalue();
        if (minvalue != null) {
            stmt.bindLong(5, minvalue);
        }
 
        Long maxvalue = entity.getMaxvalue();
        if (maxvalue != null) {
            stmt.bindLong(6, maxvalue);
        }
 
        Float feerate = entity.getFeerate();
        if (feerate != null) {
            stmt.bindDouble(7, feerate);
        }
 
        Long minfee = entity.getMinfee();
        if (minfee != null) {
            stmt.bindLong(8, minfee);
        }
 
        String feecaltype = entity.getFeecaltype();
        if (feecaltype != null) {
            stmt.bindString(9, feecaltype);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public PaymentTransTypeGD readEntity(Cursor cursor, int offset) {
        PaymentTransTypeGD entity = new PaymentTransTypeGD( //
            cursor.getLong(offset + 0), // transtype
            cursor.getLong(offset + 1), // pmcid
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // pmcname
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // status
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // minvalue
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // maxvalue
            cursor.isNull(offset + 6) ? null : cursor.getFloat(offset + 6), // feerate
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // minfee
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // feecaltype
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, PaymentTransTypeGD entity, int offset) {
        entity.setTranstype(cursor.getLong(offset + 0));
        entity.setPmcid(cursor.getLong(offset + 1));
        entity.setPmcname(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setStatus(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setMinvalue(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setMaxvalue(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setFeerate(cursor.isNull(offset + 6) ? null : cursor.getFloat(offset + 6));
        entity.setMinfee(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setFeecaltype(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(PaymentTransTypeGD entity, long rowId) {
        entity.setTranstype(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(PaymentTransTypeGD entity) {
        if(entity != null) {
            return entity.getTranstype();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(PaymentTransTypeGD entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
