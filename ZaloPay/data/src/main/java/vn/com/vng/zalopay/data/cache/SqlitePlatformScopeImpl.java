package vn.com.vng.zalopay.data.cache;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PaymentTransTypeEntity;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.AppResourceGD;
import vn.com.vng.zalopay.data.cache.model.AppResourceGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

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

        List<AppResourceGD> list = platformDaoMapper.transformAppResourceEntity(listApp);
        if (Lists.isEmptyOrNull(list)) return;

        for (AppResourceGD appResource : list) {
            appResource.setStateDownload(0);
            appResource.setNumRetry(0);
            appResource.setTimeDownload(0l);
        }

        getAppInfoDao().insertOrReplaceInTx(list);
    }

    @Override
    public Observable<List<AppResourceEntity>> listApp() {
        return makeObservable(() -> listAppResourceEntity());
    }

    public List<AppResourceEntity> listAppResourceEntity() {
        try {
            return platformDaoMapper.transformAppResourceDao(getAppInfoDao().queryBuilder().list());
        } catch (android.database.sqlite.SQLiteException e) {
            Timber.e(e, "Exception");
            return new ArrayList<>();
        }
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
        if (Lists.isEmptyOrNull(list)) return;

        getPaymentTransDao().insertOrReplaceInTx(platformDaoMapper.transformPaymentTransTypeEntity(list));
    }

    @Override
    public void increaseStateDownload(int appId) {
        Timber.e("setDownloadInfo appResourceId %s", appId);
        List<AppResourceGD> appResourceGD = getAppInfoDao().queryBuilder().where(AppResourceGDDao.Properties.Appid.eq(appId)).list();
        if (Lists.isEmptyOrNull(appResourceGD)) return;

        for (AppResourceGD app : appResourceGD) {
            app.setStateDownload(app.getStateDownload() + 1);
        }

        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }

    @Override
    public void increaseRetryDownload(long appId) {
        List<AppResourceGD> appResourceGD = getAppInfoDao().queryBuilder().where(AppResourceGDDao.Properties.Appid.eq(appId)).list();
        if (Lists.isEmptyOrNull(appResourceGD)) return;

        long currentTime = System.currentTimeMillis() / 1000;
        for (AppResourceGD app : appResourceGD) {
            app.setNumRetry(app.getNumRetry() + 1);
            app.setTimeDownload(currentTime);
        }

        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }
}
