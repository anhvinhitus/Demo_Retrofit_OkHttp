package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppInfoEntity;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class SqlAppListScopeImpl implements SqlAppListScope {

    private final DaoSession daoSession;

    public SqlAppListScopeImpl(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public void write(AppInfoEntity entity) {
        //Todo write db
    }

    @Override
    public void write(List<AppInfoEntity> entityList) {
        //Todo write db
    }
}
