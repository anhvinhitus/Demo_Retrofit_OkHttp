package vn.com.vng.zalopay.data.zfriend;

import android.text.TextUtils;

import org.greenrobot.greendao.internal.SqlUtils;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.cache.model.UCBDao;
import vn.com.vng.zalopay.data.cache.model.ZFLDao;
import vn.com.vng.zalopay.data.cache.model.ZPCDao;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.ZPCAlias.ColumnAlias;

/**
 * Created by hieuvm on 7/25/17.
 * *
 */

class ZPCSqlHelper {

    private static String createWhereCondition(String condition) {
        if (!TextUtils.isEmpty(condition)) {
            return "WHERE " + condition;
        }
        return "";
    }

    static String createSqlContactQuery(String keySearch, boolean isSyncContact, boolean isWithPhone) {
        String condition = createWhereCondition(buildContactCondition(keySearch, isWithPhone));
        return createSqlSelectContact(isSyncContact, condition);
    }

    static String createSqlContactQuery(String phoneNumbers, String zaloIds, boolean isSyncContact, boolean isWithPhone) {
        String condition = createWhereCondition(buildFavoriteCondition(phoneNumbers, zaloIds, isWithPhone));
        return createSqlSelectContact(isSyncContact, condition);
    }

    private static String buildFavoriteCondition(String phoneNumbers, String zaloIds) {

        List<String> condition = new ArrayList<>();
        if (!TextUtils.isEmpty(phoneNumbers)) {
            condition.add(ColumnAlias.PHONE_NUMBER + " IN (" + phoneNumbers + ")");
        }

        if (!TextUtils.isEmpty(zaloIds)) {
            condition.add(ColumnAlias.ZALO_ID + " IN (" + zaloIds + ")");
        }

        return Strings.joinWithDelimiter(" OR ", condition);

    }

    private static String buildFavoriteCondition(String phoneNumbers, String zaloIds, boolean isWithPhone) {

        List<String> condition = new ArrayList<>();
        if (isWithPhone) {
            condition.add(ColumnAlias.PHONE_NUMBER + " IS NOT NULL");
        }

        String phoneAndZaloIds = buildFavoriteCondition(phoneNumbers, zaloIds);
        if (!TextUtils.isEmpty(phoneAndZaloIds)) {
            condition.add(phoneAndZaloIds);
        }

        return Strings.joinWithDelimiter(" AND ", condition);
    }

    private static String buildContactCondition(String keySearch, boolean isWithPhone) {

        List<String> condition = new ArrayList<>();

        if (isWithPhone) {
            condition.add(ColumnAlias.PHONE_NUMBER + " IS NOT NULL");
        }

        if (!TextUtils.isEmpty(keySearch)) {
            String likeCondition = "("
                    + ColumnAlias.NORMALIZE_DISPLAY_NAME + " LIKE '%" + keySearch + "%' OR "
                    + ColumnAlias.DISPLAY_NAME + " LIKE '%" + keySearch + "%' OR "
                    + ColumnAlias.PHONE_NUMBER + " LIKE '%" + keySearch + "%')";
            condition.add(likeCondition);
        }

        return Strings.joinWithDelimiter(" AND ", condition);
    }

    private static String createSqlSelectContact(boolean isSyncContact, String condition) {
        String ubcSql = createSqlSelectUBCDao(isSyncContact ? "UBC_VIEW" : UCBDao.TABLENAME);
        String zpcSql = createSqlSelectZPCDAO(isSyncContact);

        String sql = "SELECT * FROM (" + ubcSql + " UNION ALL " + zpcSql + ") "
                + condition + " ORDER BY " + ColumnAlias.NORMALIZE_DISPLAY_NAME;

        //  Timber.d("Create Sql contact : %s", sql);

        return sql;
    }

    private static String createSqlSelectZPCDAO(boolean isSyncContact) {
        String displayName = isSyncContact ? ColumnAlias.DISPLAY_NAME : ZFLDao.Properties.DisplayName.columnName;
        String normalizeDisplayName = isSyncContact ? ColumnAlias.NORMALIZE_DISPLAY_NAME : ZFLDao.Properties.NormalizeDisplayName.columnName;

        return "SELECT " + ZFLDao.Properties.ZaloId.columnName + ", "
                + ZFLDao.Properties.Avatar.columnName + ", "
                + displayName + ", "
                + normalizeDisplayName + ", "
                + ZPCDao.Properties.PhoneNumber.columnName + ", "
                + ZPCDao.Properties.Status.columnName + ", "
                + ZPCDao.Properties.ZalopayId.columnName + ", "
                + ZPCDao.Properties.ZalopayName.columnName + ", "
                + ZFLDao.Properties.UsingApp.columnName
                + " FROM ZPC_VIEW";
    }

    private static String createSqlSelectUBCDao(String table) {
        return "SELECT 0 AS " + ColumnAlias.ZALO_ID + ", "
                + UCBDao.Properties.PhotoUri.columnName + " AS " + ColumnAlias.AVATAR + ", "
                + UCBDao.Properties.DisplayName.columnName + " AS " + ColumnAlias.DISPLAY_NAME + ", "
                + UCBDao.Properties.NormalizeDisplayName.columnName + " AS " + ColumnAlias.NORMALIZE_DISPLAY_NAME + ", "
                + UCBDao.Properties.PhoneNumber.columnName + " AS " + ColumnAlias.PHONE_NUMBER + ", "
                + "0 AS " + ColumnAlias.STATUS + ", "
                + "NULL AS " + ColumnAlias.ZALOPAY_ID + ", "
                + "NULL AS " + ColumnAlias.ZALOPAY_NAME + ", "
                + "0 AS " + ColumnAlias.USING_APP
                + " FROM " + table;
    }
}
