package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.ReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.RedPacketAppInfoResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.cache.model.BundleGD;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.BundleStatusEnum;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

import static java.util.Collections.emptyList;

/**
 * Created by longlv on 16/07/2016.
 * Transform RedPacket data entities
 */

@Singleton
public class RedPacketDataMapper {

    @Inject
    public RedPacketDataMapper() {

    }

    public List<PackageInBundleGD> transformToPackageInBundleGD(List<PackageInBundle> sentPackages) {
        List<PackageInBundleGD> sentPackageGDs = new ArrayList<>();
        if (sentPackages == null || sentPackages.size() <= 0) {
            return sentPackageGDs;
        }
        for (PackageInBundle sentPackage : sentPackages) {
            if (sentPackage == null || sentPackage.revZaloID <= 0) {
                continue;
            }
            PackageInBundleGD item = new PackageInBundleGD();
            item.id = sentPackage.packageID;
            item.bundleID = sentPackage.bundleID;
            item.revZaloPayID = sentPackage.revZaloPayID;
            item.revZaloID = sentPackage.revZaloID;
            item.revFullName = sentPackage.revFullName;
            item.revAvatarURL = sentPackage.revAvatarURL;
            item.openTime = sentPackage.openTime;
            item.amount = sentPackage.amount;
            item.sendMessage = sentPackage.sendMessage;
            item.isLuckiest = sentPackage.isLuckiest ? 1 : 0;
            sentPackageGDs.add(item);
        }
        return sentPackageGDs;
    }

    public List<PackageInBundle> transformToPackageInBundle(List<PackageInBundleGD> list) {
        List<PackageInBundle> sentPackages = new ArrayList<>();
        if (list == null || list.size() <= 0) {
            return sentPackages;
        }
        for (PackageInBundleGD packageInBundleGD : list) {
            if (packageInBundleGD.id <= 0) {
                continue;
            }
            PackageInBundle sentPackage = transform(packageInBundleGD);
            sentPackages.add(sentPackage);
        }
        return sentPackages;
    }

    private PackageInBundle transform(PackageInBundleGD packageInBundleGD) {
        if (packageInBundleGD == null) {
            return null;
        }
        return new PackageInBundle(packageInBundleGD.id, packageInBundleGD.bundleID, packageInBundleGD.revZaloPayID, packageInBundleGD.revZaloID, packageInBundleGD.revFullName, packageInBundleGD.revAvatarURL, packageInBundleGD.openTime, packageInBundleGD.amount, packageInBundleGD.sendMessage, packageInBundleGD.isLuckiest == 1);
    }

    public SentBundle transform(SentBundleGD sentBundleGD) {
        List<PackageInBundle> sentPackages = transformToPackageInBundle(sentBundleGD.getSentPackages());
        return new SentBundle(sentBundleGD.id, sentBundleGD.senderZaloPayID,
                sentBundleGD.type, sentBundleGD.createTime,
                sentBundleGD.lastOpenTime, sentBundleGD.totalLuck,
                sentBundleGD.numOfOpenedPakages, sentBundleGD.numOfPackages,
                sentBundleGD.sendMessage, sentBundleGD.status, sentPackages);
    }

    public List<PackageInBundle> transformToPackageInBundle(SentPackageInBundleResponse packageInBundlesResponse) {
        List<PackageInBundle> sentPackageList = new ArrayList<>();
        if (packageInBundlesResponse == null
                || packageInBundlesResponse.packageResponses == null
                || packageInBundlesResponse.packageResponses.size() <= 0) {
            return sentPackageList;
        }
        for (PackageInBundleResponse response : packageInBundlesResponse.packageResponses) {
            if (response == null) {
                continue;
            }
            sentPackageList.add(new PackageInBundle(response.packageid, response.bundleid, response.revzalopayid, response.revzaloid, response.revfullname, response.revavatarurl, response.opentime, response.amount, response.sendmessage, response.isluckiest));
        }
        return sentPackageList;
    }

    public List<SentBundle> transformDBToSentBundles(List<SentBundleGD> list) {
        if (Lists.isEmptyOrNull(list)) {
            return emptyList();
        }

        List<SentBundle> sentBundles = new ArrayList<>(list.size());
        for (SentBundleGD sentBundleGD : list) {
            SentBundle sentBundle = transform(sentBundleGD);
            if (sentBundle == null) {
                continue;
            }

            sentBundles.add(sentBundle);
        }

        return sentBundles;
    }

    public List<SentBundleGD> transformToSenBundleGD(List<SentBundle> list) {
        if (Lists.isEmptyOrNull(list)) {
            return emptyList();
        }

        List<SentBundleGD> sentBundleGDs = new ArrayList<>();
        for (SentBundle sentBundle : list) {
            SentBundleGD sentBundleGD = transform(sentBundle);
            if (sentBundleGD == null) {
                continue;
            }

            sentBundleGDs.add(sentBundleGD);
        }

        return sentBundleGDs;
    }

    private SentBundleGD transform(SentBundle sentBundle) {
        if (sentBundle == null) {
            return null;
        }

        SentBundleGD item = new SentBundleGD();
        item.id = sentBundle.bundleID;
        item.senderZaloPayID = sentBundle.sendZaloPayID;
        item.type = sentBundle.type;
        item.createTime = sentBundle.createTime;
        item.lastOpenTime = sentBundle.lastOpenTime;
        item.totalLuck = sentBundle.totalLuck;
        item.numOfOpenedPakages = sentBundle.numOfOpenedPakages;
        item.numOfOpenedPakages = sentBundle.numOfPackages;
        item.sendMessage = sentBundle.sendMessage;
        item.status = sentBundle.status;
        return item;
    }

    public GetSentBundle transformToSentBundleSummary(SentBundleListResponse response) {
        if (response == null) {
            return null;
        }
        List<SentBundle> sentBundles = transformToSentBundles(response.bundleResponseList);
        return new GetSentBundle(response.totalOfSentAmount,
                response.totalOfSentBundle,
                sentBundles);
    }

    public GetSentBundle transformToSentBundleSummary(List<SentBundleSummaryDB> list) {
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return null;
        }
        SentBundleSummaryDB sentBundleSummaryDB = list.get(0);
        return new GetSentBundle(sentBundleSummaryDB.totalOfSentAmount,
                sentBundleSummaryDB.totalOfSentBundle, null);
    }

    public List<SentBundle> transformToSentBundles(List<SentBundleResponse> bundleResponseList) {
        List<SentBundle> sentBundleList = new ArrayList<>();
        if (bundleResponseList == null || bundleResponseList.size() <= 0) {
            return sentBundleList;
        }
        for (SentBundleResponse bundleResponse : bundleResponseList) {
            if (bundleResponse == null) {
                continue;
            }
            sentBundleList.add(new SentBundle(bundleResponse.bundleid, bundleResponse.sendzalopayid,
                    bundleResponse.type, bundleResponse.createtime,
                    bundleResponse.lastopentime, bundleResponse.totalluck,
                    bundleResponse.numofopenedpakages, bundleResponse.numofpackages,
                    bundleResponse.sendmessage, BundleStatusEnum.AVAILABLE.getValue()));
        }
        return sentBundleList;
    }

    public ReceivePackage transform(ReceivePackageGD receivePackageGD) {
        if (receivePackageGD == null || receivePackageGD.id <= 0) {
            return null;
        }

        Long openTime = receivePackageGD.openedTime;
        Integer isLuckiest = receivePackageGD.isLuckiest;
        Long createTime = receivePackageGD.createTime;
        Long amount = receivePackageGD.amount;
        return new ReceivePackage(receivePackageGD.id, receivePackageGD.bundleID,
                receivePackageGD.receiverZaloPayID, receivePackageGD.senderZaloPayID,
                receivePackageGD.senderFullName,
                receivePackageGD.senderAvatar,
                receivePackageGD.message,
                amount == null ? 0 : amount,
                openTime == null ? 0 : openTime,
                isLuckiest == null ? 0 : isLuckiest,
                createTime == null ? 0 : createTime,
                receivePackageGD.status);
    }

    public List<ReceivePackage> transformDBToRevPackets(List<ReceivePackageGD> list) {
        if (Lists.isEmptyOrNull(list)) {
            return emptyList();
        }
        List<ReceivePackage> receivePackages = new ArrayList<>();
        for (ReceivePackageGD receivePackageGD : list) {
            if (receivePackageGD == null || receivePackageGD.id <= 0) {
                continue;
            }
            ReceivePackage receivePackage = transform(receivePackageGD);
            receivePackages.add(receivePackage);

        }
        return receivePackages;
    }

    public List<ReceivePackageGD> transformToRevPacketsDB(List<ReceivePackage> receivePackages) {
        if (Lists.isEmptyOrNull(receivePackages)) {
            return emptyList();
        }
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();
        for (ReceivePackage receivePackage : receivePackages) {
            if (receivePackage == null || receivePackage.packageID <= 0) {
                continue;
            }
            ReceivePackageGD receivePackageGD = transform(receivePackage);
            receivePackageGDs.add(receivePackageGD);

        }
        return receivePackageGDs;
    }

    private ReceivePackageGD transform(ReceivePackage receivePackage) {
        if (receivePackage == null || receivePackage.packageID <= 0) {
            return null;
        }

        ReceivePackageGD item = new ReceivePackageGD();
        item.id = receivePackage.packageID;
        item.bundleID = receivePackage.bundleID;
        item.receiverZaloPayID = receivePackage.revZaloPayID;
        item.senderZaloPayID = receivePackage.senderZaloPayID;
        item.senderFullName = receivePackage.senderFullName;
        item.senderAvatar = receivePackage.senderAvatar;
        item.amount = receivePackage.amount;
        item.openedTime = receivePackage.openedTime;
        item.status = receivePackage.status;
        item.message = receivePackage.message;
        item.messageStatus = null;
        item.isLuckiest = receivePackage.isLuckiest;
        item.createTime = receivePackage.createTime;

        return item;
    }

    public GetReceivePacket transformToGetRevPacket(GetReceivePackageResponse response) {
        if (response == null) {
            return null;
        }
        List<ReceivePackage> receivePackages = transformToRevPackets(response.receivePackageResponses);
        return new GetReceivePacket(response.totalOfRevAmount,
                response.totalOfRevPackage, response.numOfLuckiestDraw,
                receivePackages);
    }

    public List<ReceivePackage> transformToRevPackets(List<ReceivePackageResponse> revpackageList) {
        if (revpackageList == null || revpackageList.size() <= 0) {
            Timber.w("Empty packet list");
            return null;
        }
        Timber.d("Received packets size: %s", revpackageList.size());
        List<ReceivePackage> receivePackages = new ArrayList<>();
        for (ReceivePackageResponse response : revpackageList) {
            if (response == null) {
                continue;
            }
            receivePackages.add(
                    new ReceivePackage(response.packageid, response.bundleid,
                            response.revzalopayid, response.sendzalopayid,
                            response.sendfullname,
                            response.avatarofsender,
                            response.sendmessage,
                            response.amount,
                            response.openedtime,
                            response.isluckiest,
                            response.createtime,
                            RedPacketStatus.Opened.getValue()));
        }
        return receivePackages;
    }

    public GetReceivePacket transformToReceivePacketSummary(List<ReceivePacketSummaryDB> list) {
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return null;
        }
        ReceivePacketSummaryDB receivePacketSummarydb = list.get(0);
        return new GetReceivePacket(receivePacketSummarydb.totalOfRevamount,
                receivePacketSummarydb.totalOfRevPackage,
                receivePacketSummarydb.totalOfLuckiestDraw,
                null);
    }

    public RedPacketAppInfo transform(RedPacketAppInfoResponse response) {
        if (response == null) {
            return null;
        }

        return new RedPacketAppInfo(response.isUpdateAppInfo, response.checksum,
                response.expiredTime, transform(response.appConfigResponse));
    }

    private AppConfigEntity transform(RedPacketAppInfoResponse.AppConfigResponse appConfigResponse) {
        if (appConfigResponse == null) {
            return null;
        }
        return new AppConfigEntity(appConfigResponse.bundleExpiredTime, appConfigResponse.maxCountHist,
                appConfigResponse.maxMessageLength, appConfigResponse.maxPackageQuantity,
                appConfigResponse.maxTotalAmountPerBundle, appConfigResponse.minAmountEach,
                appConfigResponse.minDivideAmount, appConfigResponse.maxAmountPerPackage);
    }

    public List<BundleGD> transformToBundleGD(GetSentBundle getSentBundle) {
        List<BundleGD> bundleGDs = new ArrayList<>();
        if (Lists.isEmptyOrNull(getSentBundle.sentbundlelist)) {
            return bundleGDs;
        }
        for (int i = 0; i< getSentBundle.sentbundlelist.size(); i++) {
            SentBundle sentBundle = getSentBundle.sentbundlelist.get(i);
            if (sentBundle == null) {
                continue;
            }
            BundleGD bundleGD = new BundleGD();
            bundleGD.id = sentBundle.bundleID;
            bundleGD.createTime = sentBundle.createTime;
            bundleGD.lastTimeGetPackage = null;
            bundleGDs.add(bundleGD);
        }
        return bundleGDs;
    }

    public List<BundleGD> transformToBundleGD(GetReceivePacket getReceivePacket) {
        List<BundleGD> bundleGDs = new ArrayList<>();
        if (Lists.isEmptyOrNull(getReceivePacket.revpackageList)) {
            return bundleGDs;
        }
        for (int i = 0; i< getReceivePacket.revpackageList.size(); i++) {
            ReceivePackage receivePackage = getReceivePacket.revpackageList.get(i);
            if (receivePackage == null) {
                continue;
            }
            BundleGD bundleGD = new BundleGD();
            bundleGD.id = receivePackage.bundleID;
            bundleGD.createTime = receivePackage.openedTime;
            bundleGD.lastTimeGetPackage = null;
            bundleGDs.add(bundleGD);
        }
        return bundleGDs;
    }
}
