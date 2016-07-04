package vn.com.vng.zalopay.data.cache.model;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import de.greenrobot.dao.DaoLog;

/* Copy this code snippet into your AndroidManifest.xml inside the
<application> element:

    <provider
            android:name="vn.com.vng.zalopay.data.cache.model.ZaloFriendContentProvider"
            android:authorities="vn.com.vng.zalopay.data.cache.model.zalofriendprovider"/>
    */

public abstract class ZaloFriendContentProvider extends ContentProvider {

    public static final String AUTHORITY = "vn.com.vng.zalopay.data.cache.model.zalofriendprovider";
    public static final String BASE_PATH = "zalofriend";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + BASE_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/" + BASE_PATH;

    private static final String TABLENAME = ZaloFriendGDDao.TABLENAME;
    private static final String PK = ZaloFriendGDDao.Properties.Id
            .columnName;

    private static final int ZALOFRIEND_DIR = 0;
    private static final int ZALOFRIEND_ID = 1;

    private static final UriMatcher sURIMatcher;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ZALOFRIEND_DIR);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ZALOFRIEND_ID);
    }

    /**
     * This must be set from outside, it's recommended to do this inside your Application object.
     * Subject to change (static isn't nice).
     */
//    public static DaoSession daoSession;
    public abstract DaoSession getDaoSession();

    public abstract void injection();

    @Override
    public boolean onCreate() {
        // if(daoSession == null) {
        // throw new IllegalStateException("DaoSession must be set before content provider is created");
        // }
        DaoLog.d("Content Provider started: " + CONTENT_URI);
        return true;
    }

    protected SQLiteDatabase getDatabase() {
        injection();
        if (getDaoSession() == null) {
            throw new IllegalStateException("DaoSession must be set during content provider is active");
        }
        return getDaoSession().getDatabase();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;
        String path = "";
        switch (uriType) {
            case ZALOFRIEND_DIR:
                id = getDatabase().insert(TABLENAME, null, values);
                path = BASE_PATH + "/" + id;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(path);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = getDatabase();
        int rowsDeleted = 0;
        String id;
        switch (uriType) {
            case ZALOFRIEND_DIR:
                rowsDeleted = db.delete(TABLENAME, selection, selectionArgs);
                break;
            case ZALOFRIEND_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(TABLENAME, PK + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(TABLENAME, PK + "=" + id + " and "
                            + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = getDatabase();
        int rowsUpdated = 0;
        String id;
        switch (uriType) {
            case ZALOFRIEND_DIR:
                rowsUpdated = db.update(TABLENAME, values, selection, selectionArgs);
                break;
            case ZALOFRIEND_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(TABLENAME, values, PK + "=" + id, null);
                } else {
                    rowsUpdated = db.update(TABLENAME, values, PK + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ZALOFRIEND_DIR:
                queryBuilder.setTables(TABLENAME);
                break;
            case ZALOFRIEND_ID:
                queryBuilder.setTables(TABLENAME);
                queryBuilder.appendWhere(PK + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = getDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public final String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case ZALOFRIEND_DIR:
                return CONTENT_TYPE;
            case ZALOFRIEND_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
