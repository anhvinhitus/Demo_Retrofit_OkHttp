package vn.com.vng.zalopay.data.repository;

import java.util.Locale;

import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.DataManifest;
import vn.com.vng.zalopay.data.cache.model.DataManifestDao;
import vn.com.vng.zalopay.domain.repository.LocalResourceRepository;

/**
 * Created by huuhoa on 5/29/16.
 * Implementation for @ref LocalResourceRepository
 */
public class LocalResourceRepositoryImpl implements LocalResourceRepository {
    private DaoSession mDaoSession;
    public LocalResourceRepositoryImpl(DaoSession daoSession) {
        if (daoSession == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.mDaoSession = daoSession;
    }

    @Override
    public String getInternalResourceVersion() {
        return getDataManifest(Constants.MANIFEST_RESOURCE_INTERNAL_VERSION);
    }

    @Override
    public String getExternalResourceVersion(long appId) {
        String key = String.format(Locale.getDefault(), "%s_%d", Constants.MANIFEST_RESOURCE_EXTERNAL_VERSION, appId);
        return getDataManifest(key);
    }

    @Override
    public void setInternalResourceVersion(String version) {
        insertDataManifest(Constants.MANIFEST_RESOURCE_INTERNAL_VERSION, version);
    }

    @Override
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
