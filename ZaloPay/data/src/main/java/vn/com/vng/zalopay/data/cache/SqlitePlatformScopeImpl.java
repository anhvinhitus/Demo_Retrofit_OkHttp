package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class SqlitePlatformScopeImpl implements SqlitePlatformScope {

    @Override
    public void writeCards(List<CardEntity> listCard) {
    }

    @Override
    public void write(CardEntity card) {

    }

    @Override
    public void writeApplications(List<AppEntity> list) {
    }

    @Override
    public void write(AppEntity appEntity) {
    }

    @Override
    public List<AppEntity> getAllAppEntity() {
        return null;
    }

    @Override
    public List<List<CardEntity>> getAllCardEntity() {
        return null;
    }
}
