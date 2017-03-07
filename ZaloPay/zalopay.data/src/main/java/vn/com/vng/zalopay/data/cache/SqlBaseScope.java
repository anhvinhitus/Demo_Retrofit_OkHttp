package vn.com.vng.zalopay.data.cache;

/**
 * Created by AnhHieu on 5/15/16.
 * *
 */
public interface SqlBaseScope {

    void insertDataManifest(String key, String values);

    long getDataManifest(String key, long defValue);
}
