package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPackageDataMapper;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGDDao;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGDDao;
import vn.com.vng.zalopay.data.cache.model.SentPackageGD;
import vn.com.vng.zalopay.data.cache.model.SentPackageGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.redpackage.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpackage.SentBundle;
import vn.com.vng.zalopay.domain.model.redpackage.SentPackage;

import static java.util.Collections.emptyList;

/**
 * Created by longlv on 13/07/2016.
 * Implementation of RedPackageStore.LocalStorage
 */
public class RedPackageLocalStorage extends SqlBaseScopeImpl implements RedPackageStore.LocalStorage {

    private RedPackageDataMapper mDataMapper;

    public RedPackageLocalStorage(DaoSession daoSession, RedPackageDataMapper dataMapper) {
        super(daoSession);
        this.mDataMapper = dataMapper;
    }

    @Override
    public void putSentBundle(SentBundleGD sentBundleGD) {
        if (sentBundleGD == null || sentBundleGD.getId() <= 0) {
            return;
        }
        try {
            //save SentBundle to DB
            getDaoSession().getSentBundleGDDao().insertOrReplaceInTx(sentBundleGD);
            //save SentPackage of SentBundle to DB
            putSentPackage(sentBundleGD.getSentPackages());
            Timber.d("putSentBundle sentBundleGD %s", sentBundleGD);
        } catch (Exception e) {
            Timber.w("Exception while trying to put SentBundle to local storage: %s", e.getMessage());
        }
    }

    @Override
    public void putSentBundle(List<SentBundleGD> sentBundleGDs) {
        if (Lists.isEmptyOrNull(sentBundleGDs)) {
            return;
        }
        try {
            getDaoSession().getSentBundleGDDao().insertOrReplaceInTx(sentBundleGDs);

            Timber.d("putSentBundle sentBundleGDs %s", sentBundleGDs);
        } catch (Exception e) {
            Timber.w("Exception while trying to put SentBundle to local storage: %s", e.getMessage());
        }
    }

    @Override
    public void putSentPackage(List<SentPackageGD> sentPackageGDs) {
        if (sentPackageGDs == null || sentPackageGDs.size() <= 0) {
            return;
        }
        try {
            getDaoSession().getSentPackageGDDao().insertOrReplaceInTx(sentPackageGDs);

            Timber.d("putSentPackage sentPackage %s", sentPackageGDs);
        } catch (Exception e) {
            Timber.w("Exception while trying to put sentPackage to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<SentBundle>> getAllSentBundle() {
        return ObservableHelper.makeObservable(this::querySentBundleList)
                .doOnNext(redPackageList -> Timber.d("get %s", redPackageList.size()));
    }

    @Override
    public Observable<List<SentBundle>> getSentBundle(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> querySentBundleList(pageIndex, limit))
                .doOnNext(redPackageList -> Timber.d("get %s", redPackageList.size()));
    }

    @Override
    public Observable<SentBundle> getSentBundle(long bundleID) {
        return ObservableHelper.makeObservable(() -> querySentBundle(bundleID))
                .doOnNext(sentBundle -> Timber.d("get %s", sentBundle));
    }

    @Override
    public Observable<List<SentPackage>> getAllSentPackage() {
        return ObservableHelper.makeObservable(this::querySentPackageList)
                .doOnNext(sentPackageList -> Timber.d("get %s", sentPackageList.size()));
    }

    @Override
    public Observable<List<SentPackage>> getSentPackage(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> querySentPackageList(pageIndex, limit))
                .doOnNext(sentPackageList -> Timber.d("get %s", sentPackageList.size()));
    }

    @Override
    public Observable<SentPackage> getSentPackage(long bundleID) {
        return ObservableHelper.makeObservable(() -> querySentPackage(bundleID))
                .doOnNext(sentPackage -> Timber.d("get %s", sentPackage));
    }

    @Override
    public void putReceivePackages(List<ReceivePackageGD> receivePackageGDs) {
        if (Lists.isEmptyOrNull(receivePackageGDs)) {
            emptyList();
        }
        try {
            getDaoSession().getReceivePackageGDDao().insertOrReplaceInTx(receivePackageGDs);

            Timber.d("putReceivePackages receivePackages %s", receivePackageGDs);
        } catch (Exception e) {
            Timber.w("Exception while trying to put receivePackages to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<ReceivePackage>> getAllReceivePackage() {
        return ObservableHelper.makeObservable(this::queryReceivePackageList)
                .doOnNext(receivePackageList -> Timber.d("get %s", receivePackageList.size()));
    }

    @Override
    public Observable<List<ReceivePackage>> getReceivePackage(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(()-> queryReceivePackageList(pageIndex, limit))
                .doOnNext(receivePackageList -> Timber.d("get %s", receivePackageList.size()));
    }

    @Override
    public Observable<ReceivePackage> getReceivePackage(long bundleID) {
        return ObservableHelper.makeObservable(() -> queryReceivePackage(bundleID))
                .doOnNext(receivePackage -> Timber.d("get %s", receivePackage));
    }

    private List<SentBundle> querySentBundleList() {
        return mDataMapper.transformToSentBundle(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .orderDesc(SentBundleGDDao.Properties.CreateTime)
                        .list());
    }

    private List<SentBundle> querySentBundleList(int pageIndex, int limit) {
        return mDataMapper.transformToSentBundle(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(SentBundleGDDao.Properties.CreateTime)
                        .list());
    }

    private SentBundle querySentBundle(long bundleID) {
        List<SentBundle> sentBundles = mDataMapper.transformToSentBundle(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .where(SentBundleGDDao.Properties.Id.eq(bundleID))
                        .limit(1)
                        .list());
        if (Lists.isEmptyOrNull(sentBundles)) {
            return null;
        } else {
            return sentBundles.get(0);
        }
    }

    private List<SentPackage> querySentPackageList() {
        return mDataMapper.transformToSentPackage(
                getDaoSession()
                        .getSentPackageGDDao()
                        .queryBuilder()
                        .orderDesc(SentPackageGDDao.Properties.OpenTime)
                        .list());
    }

    private List<SentPackage> querySentPackageList(int pageIndex, int limit) {
        return mDataMapper.transformToSentPackage(
                getDaoSession()
                        .getSentPackageGDDao()
                        .queryBuilder()
                        .offset(pageIndex * limit)
                        .orderDesc(SentPackageGDDao.Properties.OpenTime)
                        .list());
    }

    private SentPackage querySentPackage(long bundleID) {
        List<SentPackage> sentPackages = mDataMapper.transformToSentPackage(
                getDaoSession()
                        .getSentPackageGDDao()
                        .queryBuilder()
                        .where(SentPackageGDDao.Properties.Id.eq(bundleID))
                        .limit(1)
                        .list());
        if (Lists.isEmptyOrNull(sentPackages)) {
            return null;
        } else {
            return sentPackages.get(0);
        }
    }

    private List<ReceivePackage> queryReceivePackageList() {
        return mDataMapper.transformToReceivePackage(
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .orderDesc(ReceivePackageGDDao.Properties.PackageId)
                        .list());
    }

    private List<ReceivePackage> queryReceivePackageList(int pageIndex, int limit) {
        return mDataMapper.transformToReceivePackage(
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .offset(pageIndex*limit)
                        .orderDesc(ReceivePackageGDDao.Properties.PackageId)
                        .list());
    }

    private ReceivePackage queryReceivePackage(long bundleID) {
        List<ReceivePackage> receivePackages = mDataMapper.transformToReceivePackage(
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .where(ReceivePackageGDDao.Properties.Id.eq(bundleID))
                        .limit(1)
                        .list());
        if (Lists.isEmptyOrNull(receivePackages)) {
            return null;
        } else {
            return receivePackages.get(0);
        }
    }
}
