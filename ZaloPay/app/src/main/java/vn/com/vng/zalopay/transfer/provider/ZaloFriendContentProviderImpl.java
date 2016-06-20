package vn.com.vng.zalopay.transfer.provider;

import javax.inject.Inject;
import javax.inject.Named;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendContentProvider;

/**
 * Created by longlv on 14/06/2016.
 */
public class ZaloFriendContentProviderImpl extends ZaloFriendContentProvider {

    @Inject
    @Named("daosession")
    DaoSession daoSession;

    @Override
    public DaoSession getDaoSession() {
        Timber.d("getDaoSession daoSession:%s", daoSession);
        return daoSession;
    }

    @Override
    public void injection() {
        if (daoSession == null) {
            AndroidApplication.instance().getUserComponent().inject(this);
        }
    }
}
