package vn.com.vng.zalopay.data.merchant;

import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
public class MerchantLocalStorage extends SqlBaseScopeImpl implements MerchantStore.LocalStorage {
    public MerchantLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }
}
