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
        for (AppResourceGD appResource : list) {
            appResource.setDownload(false);
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
        getPaymentTransDao().insertOrReplaceInTx(platformDaoMapper.transformPaymentTransTypeEntity(list));
    }

    @Override
    public void setDownloadInfo(int appResourceId, boolean download) {
        Timber.e("setDownloadInfo appResourceId %s", appResourceId);
        List<AppResourceGD> appResourceGD = getAppInfoDao().queryBuilder().where(AppResourceGDDao.Properties.Appid.eq(appResourceId)).list();
        for (AppResourceGD app : appResourceGD) {
            app.setDownload(download);
        }
        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }
}
