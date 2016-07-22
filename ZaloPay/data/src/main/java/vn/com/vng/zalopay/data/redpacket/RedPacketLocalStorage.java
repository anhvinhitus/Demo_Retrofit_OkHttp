package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGDDao;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGDDao;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

import static java.util.Collections.emptyList;

/**
 * Created by longlv on 13/07/2016.
 * Implementation of RedPacketStore.LocalStorage
 */
public class RedPacketLocalStorage extends SqlBaseScopeImpl implements RedPacketStore.LocalStorage {

    private RedPacketDataMapper mDataMapper;

    public RedPacketLocalStorage(DaoSession daoSession, RedPacketDataMapper dataMapper) {
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
            //save PackageInBundle of SentBundle to DB
            putPackageInBundle(sentBundleGD.getSentPackages());
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
    public void putPackageInBundle(List<PackageInBundleGD> packageInBundleGDs) {
        if (packageInBundleGDs == null || packageInBundleGDs.size() <= 0) {
            return;
        }
        try {
            getDaoSession().getPackageInBundleGDDao().insertOrReplaceInTx(packageInBundleGDs);

            Timber.d("putPackageInBundle sentPackage %s", packageInBundleGDs);
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
    public Observable<List<PackageInBundle>> getPackageInBundle() {
        return ObservableHelper.makeObservable(this::querySentPackageList)
                .doOnNext(sentPackageList -> Timber.d("get %s", sentPackageList.size()));
    }

    @Override
    public Observable<List<PackageInBundle>> getPackageInBundle(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> querySentPackageList(pageIndex, limit))
                .doOnNext(sentPackageList -> Timber.d("get %s", sentPackageList.size()));
    }

    @Override
    public Observable<PackageInBundle> getPackageInBundle(long bundleID) {
        return ObservableHelper.makeObservable(() -> querySentPackage(bundleID))
                .doOnNext(sentPackage -> Timber.d("get %s", sentPackage));
    }

    @Override
    public Boolean isPacketOpen(long packetId) {
        List<ReceivePackageGD> all =
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .where(ReceivePackageGDDao.Properties.Id.eq(packetId))
                        .limit(1)
                        .list();
        if (Lists.isEmptyOrNull(all)) {
            return Boolean.FALSE;
        } else {
            return all.get(0).getIsOpen();
        }
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
    public Observable<List<ReceivePackage>> getReceiveBundle() {
        return ObservableHelper.makeObservable(this::queryReceivePackageList)
                .doOnNext(receivePackageList -> Timber.d("get %s", receivePackageList.size()));
    }

    @Override
    public Observable<List<ReceivePackage>> getReceiveBundle(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(()-> queryReceivePackageList(pageIndex, limit))
                .doOnNext(receivePackageList -> Timber.d("get %s", receivePackageList.size()));
    }

    @Override
    public Observable<ReceivePackage> getReceiveBundle(long bundleID) {
        return ObservableHelper.makeObservable(() -> queryReceivePackage(bundleID))
                .doOnNext(receivePackage -> Timber.d("get %s", receivePackage));
    }

    private List<SentBundle> querySentBundleList() {
        return mDataMapper.transformDBToSentBundle(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .orderDesc(SentBundleGDDao.Properties.CreateTime)
                        .list());
    }

    private List<SentBundle> querySentBundleList(int pageIndex, int limit) {
        return mDataMapper.transformDBToSentBundle(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(SentBundleGDDao.Properties.CreateTime)
                        .list());
    }

    private SentBundle querySentBundle(long bundleID) {
        List<SentBundle> sentBundles = mDataMapper.transformDBToSentBundle(
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

    private List<PackageInBundle> querySentPackageList() {
        return mDataMapper.transformToPackageInBundle(
                getDaoSession()
                        .getPackageInBundleGDDao()
                        .queryBuilder()
                        .orderDesc(PackageInBundleGDDao.Properties.OpenTime)
                        .list());
    }

    private List<PackageInBundle> querySentPackageList(int pageIndex, int limit) {
        return mDataMapper.transformToPackageInBundle(
                getDaoSession()
                        .getPackageInBundleGDDao()
                        .queryBuilder()
                        .offset(pageIndex * limit)
                        .orderDesc(PackageInBundleGDDao.Properties.OpenTime)
                        .list());
    }

    private PackageInBundle querySentPackage(long bundleID) {
        List<PackageInBundle> sentPackages = mDataMapper.transformToPackageInBundle(
                getDaoSession()
                        .getPackageInBundleGDDao()
                        .queryBuilder()
                        .where(PackageInBundleGDDao.Properties.Id.eq(bundleID))
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
                        .orderDesc(ReceivePackageGDDao.Properties.OpenedTime)
                        .list());
    }

    private List<ReceivePackage> queryReceivePackageList(int pageIndex, int limit) {
        return mDataMapper.transformToReceivePackage(
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .offset(pageIndex*limit)
                        .orderDesc(ReceivePackageGDDao.Properties.OpenedTime)
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
