package vn.com.vng.zalopay.data.repository;

import vn.com.vng.zalopay.data.repository.datasource.LocalResourceFactory;
import vn.com.vng.zalopay.domain.repository.LocalResourceRepository;

/**
 * Created by huuhoa on 5/29/16.
 * Implementation for @ref LocalResourceRepository
 */
public class LocalResourceRepositoryImpl extends BaseRepository implements LocalResourceRepository {
    LocalResourceFactory mFactory;
    public LocalResourceRepositoryImpl(LocalResourceFactory factory) {
        mFactory = factory;
    }

    @Override
    public String getInternalResourceVersion() {
        return mFactory.getInternalResourceVersion();
    }

    @Override
    public String getExternalResourceVersion(int appId) {
        return mFactory.getExternalResourceVersion(appId);
    }

    @Override
    public void setInternalResourceVersion(String version) {
        mFactory.setInternalResourceVersion(version);
    }

    @Override
    public void setExternalResourceVersion(int appId, String version) {
        mFactory.setExternalResourceVersion(appId, version);
    }
}
