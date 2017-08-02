package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.database.Database;

/**
 * Created by hieuvm on 7/23/17.
 * *
 */

public class UBCViewDao {

    public static void createView(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE VIEW " + constraint + "\"UBC_VIEW\" AS SELECT * FROM " + UCBDao.TABLENAME + " AS T " +
                "LEFT JOIN " + ZPCDao.TABLENAME + " AS T1 ON T." + UCBDao.Properties.PhoneNumber.columnName + " = T1." + ZPCDao.Properties.PhoneNumber.columnName
                + " WHERE T1." + ZPCDao.Properties.PhoneNumber.columnName + " IS NULL");
    }

    public static void dropView(Database db, boolean ifExists) {
        String sql = "DROP VIEW " + (ifExists ? "IF EXISTS " : "") + "\"UBC_VIEW\"";
        db.execSQL(sql);
    }
}
