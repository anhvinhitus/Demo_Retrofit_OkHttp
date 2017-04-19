package vn.com.vng.zalopay.data.redpacket;

import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.RedPacketStatusEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.BundleGD;
import vn.com.vng.zalopay.data.cache.model.BundleGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGDDao;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGDDao;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDBDao;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGDDao;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDBDao;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

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
    public void putBundle(List<BundleGD> bundleGDs) {
        getDaoSession().getBundleGDDao().insertInTx(bundleGDs);
    }

    @Override
    public void updateLastTimeGetPackage(long bundleId) {
        BundleGD bundleGD = getBundle(bundleId);
        if (bundleGD == null) {
            bundleGD = new BundleGD();
        }
        bundleGD.id = (bundleId);
        bundleGD.lastTimeGetPackage = (System.currentTimeMillis());
        getDaoSession().getBundleGDDao().insertOrReplaceInTx(bundleGD);
    }

    @Override
    public BundleGD getBundle(long bundleId) {
        List<BundleGD> bundleGDs = getDaoSession().getBundleGDDao().
                queryBuilder().
                where(BundleGDDao.Properties.Id.eq(bundleId))
                .list();
        if (bundleGDs == null || bundleGDs.isEmpty()) {
            return null;
        }
        return bundleGDs.get(0);
    }

    @Override
    public void putSentBundleSummary(SentBundleSummaryDB sentBundleSummaryDB) {
        if (sentBundleSummaryDB == null ||
                sentBundleSummaryDB.totalOfSentAmount < 0 ||
                sentBundleSummaryDB.totalOfSentBundle < 0) {
            return;
        }
        try {
            //Delete all SentBundleSummary
            getDaoSession().getSentBundleSummaryDBDao().deleteAll();
            //save SentBundle to DB
            getDaoSession().getSentBundleSummaryDBDao().insertOrReplaceInTx(sentBundleSummaryDB);
            Timber.d("putSentBundleSummary data %s", sentBundleSummaryDB);
        } catch (Exception e) {
            Timber.w("Exception while trying to put SentBundleSummary to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<GetSentBundle> getSentBundleSummary() {
        return ObservableHelper.makeObservable(this::querySentBundleSummary)
                .doOnNext(sentBundleSummary -> Timber.d("getSentBundleSummary %s", sentBundleSummary));
    }

    private GetSentBundle querySentBundleSummary() {
        return mDataMapper.transformToSentBundleSummary(
                getDaoSession()
                        .getSentBundleSummaryDBDao()
                        .queryBuilder()
                        .orderDesc(SentBundleSummaryDBDao.Properties.TimeCreate)
                        .limit(1)
                        .list());
    }

    @Override
    public void putReceivePacketSummary(ReceivePacketSummaryDB receivePacketSummaryDB) {
        if (receivePacketSummaryDB == null ||
                receivePacketSummaryDB.totalOfLuckiestDraw < 0 ||
                receivePacketSummaryDB.totalOfRevamount < 0 ||
                receivePacketSummaryDB.totalOfRevPackage < 0) {
            return;
        }
        try {
            //Delete all ReceivePacketSummary
            getDaoSession().getReceivePacketSummaryDBDao().deleteAll();
            //save ReceivePacketSummary to DB
            getDaoSession().getReceivePacketSummaryDBDao().insertOrReplaceInTx(receivePacketSummaryDB);
            Timber.d("putReceivePacketSummary data %s", receivePacketSummaryDB);
        } catch (Exception e) {
            Timber.w("Exception while trying to put ReceivePacketSummary to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<GetReceivePacket> getReceivePacketSummary() {
        return ObservableHelper.makeObservable(this::queryReceivePacketSummary)
                .doOnNext(receivePacketSummary -> Timber.d("getReceivePacketSummary %s", receivePacketSummary));
    }

    private GetReceivePacket queryReceivePacketSummary() {
        return mDataMapper.transformToReceivePacketSummary(
                getDaoSession()
                        .getReceivePacketSummaryDBDao()
                        .queryBuilder()
                        .orderDesc(ReceivePacketSummaryDBDao.Properties.TimeCreate)
                        .limit(1)
                        .list());
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
    public Observable<List<SentBundle>> getSentBundle(long timeCreate, int limit) {
        if (limit <= 0) {
            return Observable.just(Collections.emptyList());
        }
        return ObservableHelper.makeObservable(() -> querySentBundleList(timeCreate, limit))
                .doOnNext(redPackageList -> Timber.d("getSentBundle timeCreate [%s] limit [%s] size [%s]",
                        timeCreate, limit, redPackageList.size()));
    }

    @Override
    public Boolean isHaveSentBundleInDb(long createTime, int count) {
        return getDaoSession().getSentBundleGDDao().queryBuilder()
                .where(SentBundleGDDao.Properties.CreateTime.lt(createTime))
                .count() >= count;
    }

    @Override
    public Void setBundleStatus(long bundleId, int status) {
        Timber.d("set status for SentBundle: %s", bundleId);
        SentBundleGD sentBundleGD = querySentBundleGD(bundleId);
        if (sentBundleGD == null) {
            Timber.d("SentBundle not found");
            return null;
        }

        sentBundleGD.status = (long) (status);
        getDaoSession().getSentBundleGDDao().insertOrReplace(sentBundleGD);
        Timber.d("SentBundle is set to be opened");
        return null;
    }

    @Override
    public Observable<List<PackageInBundle>> getPackageInBundle(long bundleID) {
        return ObservableHelper.makeObservable(() -> querySentPackage(bundleID))
                .doOnNext(sentPackage -> Timber.d("getPackageInBundle bundleID [%s] sentPackage [%s]", bundleID, sentPackage));
    }

    @Override
    public ReceivePackageGD getPacketStatus(long packetId) {
        Timber.d("query status for packet: %s", packetId);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            Timber.d("Packet not found");
            return null;
        }

        Timber.d("query status for packet: %s, status: %s", packetId, packageGD.status);
        return packageGD;
    }

    @Override
    public Void setPacketStatus(long packetId, long amount, int status, String messageStatus) {
        Timber.d("set open status for packet: %s", packetId);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            Timber.d("Packet not found");
            return null;
        }

        packageGD.status = (long) (status);
        packageGD.messageStatus = (messageStatus);
        packageGD.amount = (amount);
        packageGD.openedTime = (System.currentTimeMillis());
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
            packageGD.id = (packetId);
            packageGD.status = (long) (RedPacketStatus.CanOpen.getValue());
        }
        packageGD.bundleID = (bundleId);
        packageGD.senderFullName = (senderName);
        packageGD.senderAvatar = (senderAvatar);
        packageGD.message = (message);

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
            return;
        }

        try {
            getDaoSession().getReceivePackageGDDao().insertOrReplaceInTx(receivePackageGDs);

            Timber.d("putReceivePackages receivePackages %s", receivePackageGDs);
        } catch (Exception e) {
            Timber.w("Exception while trying to put receivePackages to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<ReceivePackage>> getReceiveBundle(long openTime, int limit) {
        if (openTime < 0 || limit <= 0) {
            return Observable.just(Collections.emptyList());
        }

        return ObservableHelper.makeObservable(() -> queryReceivePackageList(openTime, limit))
                .doOnNext(receivePackageList -> Timber.d("getReceiveBundle openTime [%s] limit [%s] size [%s]",
                        openTime, limit, receivePackageList.size()));
    }

    @Override
    public Boolean isHaveReceivePacketInDb(long createTime, int count) {
        return getDaoSession().getReceivePackageGDDao().queryBuilder()
                .where(ReceivePackageGDDao.Properties.CreateTime.lt(createTime))
                .count() >= count;
    }

    private List<SentBundle> querySentBundleList(int limit) {
        List<SentBundleGD> list = getDaoSession()
                .getSentBundleGDDao()
                .queryBuilder()
                .orderDesc(SentBundleGDDao.Properties.CreateTime)
                .limit(limit)
                .list();
        return Lists.transform(list, mDataMapper::transform);
    }

    private List<SentBundle> querySentBundleList(long timeCreate, int limit) {
        if (timeCreate == 0) {
            return querySentBundleList(limit);
        } else {
            List<SentBundleGD> list = getDaoSession()
                    .getSentBundleGDDao()
                    .queryBuilder()
                    .where(SentBundleGDDao.Properties.CreateTime.lt(timeCreate))
                    .orderDesc(SentBundleGDDao.Properties.CreateTime)
                    .limit(limit)
                    .list();
            return Lists.transform(list, mDataMapper::transform);
        }
    }

    private SentBundleGD querySentBundleGD(long bundleID) {
        List<SentBundleGD> sentBundleGDs = getDaoSession()
                .getSentBundleGDDao()
                .queryBuilder()
                .where(SentBundleGDDao.Properties.Id.eq(bundleID))
                .limit(1)
                .list();
        if (Lists.isEmptyOrNull(sentBundleGDs)) {
            return null;
        } else {
            return sentBundleGDs.get(0);
        }
    }

    private List<PackageInBundle> querySentPackage(long bundleID) {
        List<PackageInBundleGD> list = getDaoSession()
                .getPackageInBundleGDDao()
                .queryBuilder()
                .where(PackageInBundleGDDao.Properties.BundleID.eq(bundleID))
                .list();
        return Lists.transform(list, mDataMapper::transform);
    }

    private List<ReceivePackage> queryReceivePackageList(int limit) {
        List<ReceivePackageGD> list = getDaoSession()
                .getReceivePackageGDDao()
                .queryBuilder()
                .orderDesc(ReceivePackageGDDao.Properties.OpenedTime)
                .limit(limit)
                .list();
        return Lists.transform(list, mDataMapper::transform);
    }

    private List<ReceivePackage> queryReceivePackageList(long timeCreate, int limit) {
        if (timeCreate == 0) {
            return queryReceivePackageList(limit);
        } else {
            List<ReceivePackageGD> list = getDaoSession()
                    .getReceivePackageGDDao()
                    .queryBuilder()
                    .where(ReceivePackageGDDao.Properties.OpenedTime.lt(timeCreate))
                    .orderDesc(ReceivePackageGDDao.Properties.OpenedTime)
                    .limit(limit)
                    .list();
            return Lists.transform(list, mDataMapper::transform);
        }
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

    @Override
    public void updateListPackageStatus(@Nullable List<RedPacketStatusEntity> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return;
        }

        Timber.d("updateListPackageStatus: %s", entities.size());

        for (RedPacketStatusEntity entity : entities) {
            ReceivePackageGD receivePackageGD = getPacketStatus(entity.packageID);
            if (receivePackageGD == null) {
                continue;
            }
            receivePackageGD.status = entity.status;
            getDaoSession().insertOrReplace(receivePackageGD);
        }
    }
}
