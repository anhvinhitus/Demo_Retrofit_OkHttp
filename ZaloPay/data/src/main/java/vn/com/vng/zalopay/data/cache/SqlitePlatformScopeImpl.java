package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class SqlitePlatformScopeImpl implements SqlitePlatformScope {

    private final DaoSession daoSession;

    public SqlitePlatformScopeImpl(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public void writeCards(List<CardEntity> listCard) {
    }

    @Override
    public void write(CardEntity card) {

    }


    @Override
    public List<List<CardEntity>> getAllCardEntity() {
        return null;
    }
}
