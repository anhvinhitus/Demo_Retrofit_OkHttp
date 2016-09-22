/*
package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PaymentTransTypeEntity;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

*/
/**
 * Created by AnhHieu on 4/28/16.
 *//*

public class SqlitePlatformScopeImpl extends SqlBaseScopeImpl implements SqlitePlatformScope {

    private PlatformDaoMapper platformDaoMapper;

    public SqlitePlatformScopeImpl(DaoSession daoSession, PlatformDaoMapper mapper) {
        super(daoSession);
        this.platformDaoMapper = mapper;
    }

    @Override
    public void writeCards(List<CardEntity> listCard) {
        getBankCardDao().insertOrReplaceInTx(platformDaoMapper.transformCardGreenDao(listCard));
    }

    @Override
    public void write(CardEntity card) {

    }

    @Override
    public Observable<List<CardEntity>> listCard() {
        return ObservableHelper.makeObservable(() -> listCardEntity());
    }

    private List<CardEntity> listCardEntity() {
        return platformDaoMapper.transformCardEntity(getBankCardDao().queryBuilder().list());
    }

    @Override
    public void writePaymentTransType(List<PaymentTransTypeEntity> list) {
        if (Lists.isEmptyOrNull(list)) return;

        getPaymentTransDao().insertOrReplaceInTx(platformDaoMapper.transformPaymentTransTypeEntity(list));
    }

}
*/
