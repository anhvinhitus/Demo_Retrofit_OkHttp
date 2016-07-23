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
    public Observable<List<PackageInBundle>> getPackageInBundle(long bundleID, int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> querySentPackageList(bundleID, pageIndex, limit))
                .doOnNext(sentPackageList -> Timber.d("get %s", sentPackageList.size()));
    }

    @Override
    public Observable<List<PackageInBundle>> getPackageInBundle(long bundleID) {
        return ObservableHelper.makeObservable(() -> querySentPackage(bundleID))
                .doOnNext(sentPackage -> Timber.d("get %s", sentPackage));
    }

    @Override
    public Boolean isHavePackagesInDb(long bundleID) {
        return getDaoSession().getPackageInBundleGDDao().queryBuilder().where(PackageInBundleGDDao.Properties.BundleID.eq(bundleID)).count() > 0;
    }

    @Override
    public Boolean isPacketOpen(long packetId) {
        Timber.d("query status for packet: %s", packetId);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            Timber.d("Packet not found");
            return Boolean.FALSE;
        }

        Timber.d("query status for packet: %s, result: %s", packetId, packageGD.getIsOpen());
        return packageGD.getIsOpen();
    }

    @Override
    public Void setPacketIsOpen(long packetId, long amount) {
        Timber.d("set open status for packet: %s", packetId);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            Timber.d("Packet not found");
            return null;
        }

        packageGD.setIsOpen(true);
        packageGD.setAmount(amount);
        packageGD.setOpenedTime(System.currentTimeMillis());
        getDaoSession().getReceivePackageGDDao().insertOrReplace(packageGD);
        Timber.d("Packet is set to be opened");
        return null;
    }

    @Override
    public Void addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message) {
        Timber.d("Add received red packet: [packetId: %s, bundleId: %s, sender: %s, avatar: %s, message: %s",
                packetId, bundleId, senderName, senderAvatar, message);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            packageGD = new ReceivePackageGD();
            packageGD.setId(packetId);
            packageGD.setIsOpen(false);
        }
        packageGD.setBundleID(bundleId);
        packageGD.setSenderFullName(senderName);
        packageGD.setSenderAvatar(senderAvatar);
        packageGD.setMessage(message);

        getDaoSession().getReceivePackageGDDao().insertOrReplace(packageGD);
        return null;
    }

    @Override
    public ReceivePackage getReceivedPacket(long packetId) {
        return queryReceivePackage(packetId);
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

    private List<PackageInBundle> querySentPackageList(long bundleID, int pageIndex, int limit) {
        return mDataMapper.transformToPackageInBundle(
                getDaoSession()
                        .getPackageInBundleGDDao()
                        .queryBuilder()
                        .where(PackageInBundleGDDao.Properties.BundleID.eq(bundleID))
                        .offset(pageIndex * limit)
                        .orderDesc(PackageInBundleGDDao.Properties.OpenTime)
                        .list());
    }

    private List<PackageInBundle> querySentPackage(long bundleID) {
        return mDataMapper.transformToPackageInBundle(
                getDaoSession()
                        .getPackageInBundleGDDao()
                        .queryBuilder()
                        .where(PackageInBundleGDDao.Properties.Id.eq(bundleID))
                        .list());
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

    private ReceivePackage queryReceivePackage(long packetId) {
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            return null;
        }

        return mDataMapper.transform(packageGD);
    }

    private ReceivePackageGD getReceivePackageGD(long packetId) {
        List<ReceivePackageGD> receivePackages = getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .where(ReceivePackageGDDao.Properties.Id.eq(packetId))
                        .limit(1)
                        .list();
        if (Lists.isEmptyOrNull(receivePackages)) {
            return null;
        } else {
            return receivePackages.get(0);
        }
    }
}
