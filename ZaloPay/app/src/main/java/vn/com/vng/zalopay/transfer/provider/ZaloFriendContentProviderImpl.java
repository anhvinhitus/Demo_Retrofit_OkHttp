package vn.com.vng.zalopay.transfer.provider;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendContentProvider;

/**
 * Created by longlv on 14/06/2016.
 */
public class ZaloFriendContentProviderImpl extends ZaloFriendContentProvider {

    @Inject
    DaoSession daoSession;

    @Override
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
