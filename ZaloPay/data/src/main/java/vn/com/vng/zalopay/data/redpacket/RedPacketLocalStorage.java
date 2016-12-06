package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
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
import vn.com.vng.zalopay.data.cache.model.RedPacketAppInfoGD;
import vn.com.vng.zalopay.data.cache.model.RedPacketAppInfoGDDao;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGDDao;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDBDao;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
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

        sentBundleGD.status = (status);
        getDaoSession().getSentBundleGDDao().insertOrReplace(sentBundleGD);
        Timber.d("SentBundle is set to be opened");
        return null;
    }

    @Override
    public Observable<List<PackageInBundle>> getPackageInBundle(long bundleID) {
        return ObservableHelper.makeObservable(() -> querySentPackage(bundleID))
                .doOnNext(sentPackage -> Timber.d("getPackageInBundle bundleID [%s] sentPackage [%s]", bundleID, sentPackage));
    }

    private RedPacketAppInfoGD queryRedPacketAppInfo() {
        List<RedPacketAppInfoGD> redPacketAppInfos = getDaoSession().getRedPacketAppInfoGDDao().queryBuilder()
                .orderDesc(RedPacketAppInfoGDDao.Properties.Id).limit(1).list();
        if (redPacketAppInfos == null || redPacketAppInfos.size() <= 0) {
            return null;
        }
        return redPacketAppInfos.get(0);
    }

    @Override
    public RedPacketAppInfo getRedPacketAppInfo() {
        return transform(queryRedPacketAppInfo());
    }

    @Override
    public void putRedPacketAppInfo(RedPacketAppInfo redPacketAppInfo) {
        if (redPacketAppInfo == null || redPacketAppInfo.appConfigEntity == null) {
            return;
        }
        RedPacketAppInfoGD data = new RedPacketAppInfoGD();
        data.id = null;
        data.checksum = redPacketAppInfo.checksum;
        data.expiredTime = redPacketAppInfo.expiredTime;
        data.minAmountEach = redPacketAppInfo.appConfigEntity.minAmountEach;
        data.maxTotalAmountPerBundle = redPacketAppInfo.appConfigEntity.maxTotalAmountPerBundle;
        data.maxPackageQuantity = redPacketAppInfo.appConfigEntity.maxPackageQuantity;
        data.maxCountHist = redPacketAppInfo.appConfigEntity.maxCountHist;
        data.maxMessageLength = redPacketAppInfo.appConfigEntity.maxMessageLength;
        data.bundleExpiredTime = redPacketAppInfo.appConfigEntity.bundleExpiredTime;
        data.minDivideAmount = redPacketAppInfo.appConfigEntity.minDivideAmount;
        data.maxAmountPerPackage = redPacketAppInfo.appConfigEntity.maxAmountPerPackage;
        getDaoSession().getRedPacketAppInfoGDDao().insertOrReplaceInTx(data);
    }

    private RedPacketAppInfo transform(RedPacketAppInfoGD redPacketAppInfoGD) {
        if (redPacketAppInfoGD == null) {
            return null;
        }

        AppConfigEntity entity = new AppConfigEntity();
        entity.bundleExpiredTime = ConvertHelper.unboxValue(redPacketAppInfoGD.bundleExpiredTime, 0L);
        entity.maxCountHist = ConvertHelper.unboxValue(redPacketAppInfoGD.maxCountHist, 0);
        entity.maxMessageLength = ConvertHelper.unboxValue(redPacketAppInfoGD.maxMessageLength, 0);
        entity.maxPackageQuantity = ConvertHelper.unboxValue(redPacketAppInfoGD.maxPackageQuantity, 0);
        entity.maxTotalAmountPerBundle = ConvertHelper.unboxValue(redPacketAppInfoGD.maxTotalAmountPerBundle, 0);
        entity.maxAmountPerPackage = ConvertHelper.unboxValue(redPacketAppInfoGD.maxAmountPerPackage, 0L);
        entity.minAmountEach = ConvertHelper.unboxValue(redPacketAppInfoGD.minAmountEach, 0);
        entity.minDivideAmount = ConvertHelper.unboxValue(redPacketAppInfoGD.minDivideAmount, 0L);

        RedPacketAppInfo item = new RedPacketAppInfo();
        item.isUpdateAppInfo = false;
        item.checksum = redPacketAppInfoGD.checksum;
        item.expiredTime = ConvertHelper.unboxValue(redPacketAppInfoGD.expiredTime, 0L);
        item.appConfigEntity = entity;
        return item;
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

        packageGD.status = (status);
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
            packageGD.status = (RedPacketStatus.CanOpen.getValue());
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
    public Observable<List<ReceivePackage>> getReceiveBundle(long openTime, int limit) {
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
        return mDataMapper.transformDBToSentBundles(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .orderDesc(SentBundleGDDao.Properties.CreateTime)
                        .limit(limit)
                        .list());
    }

    private List<SentBundle> querySentBundleList(long timeCreate, int limit) {
        if (timeCreate == 0) {
            return querySentBundleList(limit);
        } else {
            return mDataMapper.transformDBToSentBundles(
                    getDaoSession()
                            .getSentBundleGDDao()
                            .queryBuilder()
                            .where(SentBundleGDDao.Properties.CreateTime.lt(timeCreate))
                            .orderDesc(SentBundleGDDao.Properties.CreateTime)
                            .limit(limit)
                            .list());
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
        return mDataMapper.transformToPackageInBundle(
                getDaoSession()
                        .getPackageInBundleGDDao()
                        .queryBuilder()
                        .where(PackageInBundleGDDao.Properties.BundleID.eq(bundleID))
                        .list());
    }

    private List<ReceivePackage> queryReceivePackageList(int limit) {
        return mDataMapper.transformDBToRevPackets(
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .orderDesc(ReceivePackageGDDao.Properties.OpenedTime)
                        .limit(limit)
                        .list());
    }

    private List<ReceivePackage> queryReceivePackageList(long timeCreate, int limit) {
        if (timeCreate == 0) {
            return queryReceivePackageList(limit);
        } else {
            return mDataMapper.transformDBToRevPackets(
                    getDaoSession()
                            .getReceivePackageGDDao()
                            .queryBuilder()
                            .where(ReceivePackageGDDao.Properties.OpenedTime.lt(timeCreate))
                            .orderDesc(ReceivePackageGDDao.Properties.OpenedTime)
                            .limit(limit)
                            .list());
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
}
