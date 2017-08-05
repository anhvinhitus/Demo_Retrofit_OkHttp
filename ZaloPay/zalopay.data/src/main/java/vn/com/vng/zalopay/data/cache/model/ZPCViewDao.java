package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.SqlUtils;

import vn.com.vng.zalopay.data.zfriend.ZPCAlias.TableAlias;
import vn.com.vng.zalopay.data.zfriend.ZPCAlias.ColumnAlias;

/**
 * Created by hieuvm on 7/20/17.
 * *
 */

public class ZPCViewDao {
    public static void createView(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE VIEW " + constraint + "\"ZPC_VIEW\" AS SELECT " + getColumns() + " FROM " + ZFLDao.TABLENAME + " AS " + TableAlias.ZFL_TABLE_ALIAS +
                " LEFT JOIN " + ZPCDao.TABLENAME + " AS " + TableAlias.ZPC_TABLE_ALIAS + " ON " + appendColumn(TableAlias.ZFL_TABLE_ALIAS, ZFLDao.Properties.ZaloId.columnName) + " = " + appendColumn(TableAlias.ZPC_TABLE_ALIAS, ZPCDao.Properties.ZaloId.columnName) +
                " LEFT JOIN " + UCBDao.TABLENAME + " AS " + TableAlias.UCB_TABLE_ALIAS + " ON " + appendColumn(TableAlias.ZPC_TABLE_ALIAS, ZPCDao.Properties.PhoneNumber.columnName) + " = " + appendColumn(TableAlias.UCB_TABLE_ALIAS, UCBDao.Properties.PhoneNumber.columnName));
    }

    public static void dropView(Database db, boolean ifExists) {
        String sql = "DROP VIEW " + (ifExists ? "IF EXISTS " : "") + "\"ZPC_VIEW\"";
        db.execSQL(sql);
    }

    public static String getColumns() {
       /* if (false) {
            return "*";
        }*/

        String[] columnsT = new String[]{
                ZFLDao.Properties.ZaloId.columnName,
                //  ZFLDao.Properties.UserName.columnName,
                ZFLDao.Properties.Avatar.columnName,
                //  ZFLDao.Properties.Birthday.columnName,
                ZFLDao.Properties.DisplayName.columnName,
                ZFLDao.Properties.NormalizeDisplayName.columnName,
                //  ZFLDao.Properties.Gender.columnName,
                ZFLDao.Properties.UsingApp.columnName,
        };

        String[] columnsT1 = new String[]{
                //ZPCDao.Properties.Id.columnName,
                ZPCDao.Properties.ZaloId.columnName,
                ZPCDao.Properties.ZalopayId.columnName,
                ZPCDao.Properties.PhoneNumber.columnName,
                ZPCDao.Properties.ZalopayName.columnName,
                // ZPCDao.Properties.Avatar.columnName,
                // ZPCDao.Properties.DisplayName.columnName,
                // ZPCDao.Properties.NormalizeDisplayName.columnName,
                ZPCDao.Properties.Status.columnName,
        };

        String[] columnsT2 = new String[]{
                //UCBDao.Properties.Id.columnName,
                UCBDao.Properties.PhoneNumber.columnName,
                UCBDao.Properties.DisplayName.columnName,
                UCBDao.Properties.NormalizeDisplayName.columnName,
                UCBDao.Properties.PhotoUri.columnName
        };

        StringBuilder builder = new StringBuilder();
        SqlUtils.appendColumns(builder, TableAlias.ZFL_TABLE_ALIAS, columnsT);
        builder.append(',');
        SqlUtils.appendColumns(builder, TableAlias.ZPC_TABLE_ALIAS, columnsT1);
        builder.append(',');
        SqlUtils.appendColumns(builder, TableAlias.UCB_TABLE_ALIAS, columnsT2);
        builder.append(',');
        builder.append(" IFNULL( ");
        SqlUtils.appendColumn(builder, TableAlias.UCB_TABLE_ALIAS, UCBDao.Properties.DisplayName.columnName);
        builder.append(',');
        SqlUtils.appendColumn(builder, TableAlias.ZFL_TABLE_ALIAS, ZFLDao.Properties.DisplayName.columnName);
        builder.append(" ) AS ");
        builder.append(ColumnAlias.DISPLAY_NAME);
        builder.append(',');

        builder.append(" IFNULL( ");
        SqlUtils.appendColumn(builder, TableAlias.UCB_TABLE_ALIAS, UCBDao.Properties.NormalizeDisplayName.columnName);
        builder.append(',');
        SqlUtils.appendColumn(builder, TableAlias.ZFL_TABLE_ALIAS, ZFLDao.Properties.NormalizeDisplayName.columnName);
        builder.append(" ) AS ");
        builder.append(ColumnAlias.NORMALIZE_DISPLAY_NAME);

        return builder.toString();
    }

    private static String appendColumn(String tableAlias, String column) {
        StringBuilder builder = new StringBuilder();
        SqlUtils.appendColumn(builder, tableAlias, column);
        return builder.toString();
    }
}
