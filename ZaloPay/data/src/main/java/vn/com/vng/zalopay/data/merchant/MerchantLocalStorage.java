package vn.com.vng.zalopay.data.merchant;

import java.util.Collection;
import java.util.List;

import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.MerchantUser;
import vn.com.vng.zalopay.data.cache.model.MerchantUserDao;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
public class MerchantLocalStorage extends SqlBaseScopeImpl implements MerchantStore.LocalStorage {

    public MerchantLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    private MerchantUserDao getMerchantUserDao() {
        return getDaoSession().getMerchantUserDao();
    }


    @Override
    public void put(MerchantUser entity) {
        getMerchantUserDao().insertOrReplace(entity);
    }

    @Override
    public void put(List<MerchantUser> entities) {
        getMerchantUserDao().insertOrReplaceInTx(entities);
    }

    @Override
    public MerchantUser get(long appId) {
        return getMerchantUserDao().load(appId);
    }

    @Override
    public void removeAll() {
        getMerchantUserDao().deleteAll();
    }

    @Override
    public boolean existIn(long[] appIds) {
        long count = getMerchantUserDao().queryBuilder()
                .where(MerchantUserDao.Properties.Appid.in(appIds))
                .count();
        return count >= appIds.length;
    }

    public boolean existIn(Collection<Long> appIds) {
        long count = getMerchantUserDao().queryBuilder()
                .where(MerchantUserDao.Properties.Appid.in(appIds))
                .count();
        return count >= appIds.size();
    }
}
