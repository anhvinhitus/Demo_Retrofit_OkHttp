package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PaymentTransTypeEntity;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.AppResourceGD;
import vn.com.vng.zalopay.data.cache.model.AppResourceGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by AnhHieu on 4/28/16.
 */
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
        return makeObservable(() -> listCardEntity());
    }

    private List<CardEntity> listCardEntity() {
        return platformDaoMapper.transformCardEntity(getBankCardDao().queryBuilder().list());
    }

    @Override
    public void write(List<AppResourceEntity> listApp) {
        getAppInfoDao().insertOrReplaceInTx(platformDaoMapper.transformAppResourceEntity(listApp));
    }

    @Override
    public Observable<List<AppResourceEntity>> listApp() {
        return makeObservable(() -> listAppResourceEntity());
    }

    public List<AppResourceEntity> listAppResourceEntity() {
        return platformDaoMapper.transformAppResourceDao(getAppInfoDao().queryBuilder().list());
    }

    @Override
    public void updateAppId(List<Integer> list) {
        getAppInfoDao().queryBuilder()
                .where(AppResourceGDDao.Properties.Appid.notIn(list))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities(); // TOdo: chú ý có khi trong session/cache van ton tai. (get App !=null)
    }

    @Override
    public void writePaymentTransType(List<PaymentTransTypeEntity> list) {
        getPaymentTransDao().insertOrReplaceInTx(platformDaoMapper.transformPaymentTransTypeEntity(list));
    }
}
