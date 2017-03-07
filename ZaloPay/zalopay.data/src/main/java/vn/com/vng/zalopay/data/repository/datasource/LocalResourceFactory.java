package vn.com.vng.zalopay.data.repository.datasource;

import java.util.Locale;

import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.DataManifest;
import vn.com.vng.zalopay.data.cache.model.DataManifestDao;

/**
 * Created by huuhoa on 5/29/16.
 * Local resource configuration
 */
public class LocalResourceFactory {
    private DaoSession mDaoSession;

    public LocalResourceFactory(DaoSession daoSession) {

        if (daoSession == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.mDaoSession = daoSession;
    }

    public String getInternalResourceVersion() {
        return getDataManifest(Constants.MANIFEST_RESOURCE_INTERNAL_VERSION);
    }

    public String getExternalResourceVersion(long appId) {
        String key = String.format(Locale.getDefault(), "%s_%d", Constants.MANIFEST_RESOURCE_EXTERNAL_VERSION, appId);
        return getDataManifest(key);
    }

    public void setInternalResourceVersion(String version) {
        insertDataManifest(Constants.MANIFEST_RESOURCE_INTERNAL_VERSION, version);
    }

    public void setExternalResourceVersion(long appId, String version) {
        String key = String.format(Locale.getDefault(), "%s_%d", Constants.MANIFEST_RESOURCE_EXTERNAL_VERSION, appId);
        insertDataManifest(key, version);
    }

    private void insertDataManifest(String key, String values) {
        DataManifest data = new DataManifest();
        data.key = key;
        data.value = values;
        mDaoSession.getDataManifestDao().insertOrReplace(data);
    }

    private String getDataManifest(String key) {
        DataManifest dataManifest = mDaoSession.getDataManifestDao().queryBuilder()
                .where(DataManifestDao.Properties.Key.eq(key)).unique();

        if (dataManifest != null) {
            return dataManifest.value;
        }

        return null;
    }
}
