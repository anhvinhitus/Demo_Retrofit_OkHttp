package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.ReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.RedPacketAppInfoResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
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
            PackageInBundleGD packageInBundleGD = new PackageInBundleGD(sentPackage.packageID,
                    sentPackage.bundleID, sentPackage.revZaloPayID,
                    sentPackage.revZaloID, sentPackage.revFullName,
                    sentPackage.revAvatarURL, sentPackage.openTime,
                    sentPackage.amount, sentPackage.sendMessage,
                    sentPackage.isLuckiest ? 1 : 0);
            sentPackageGDs.add(packageInBundleGD);
        }
        return sentPackageGDs;
    }

    public List<PackageInBundle> transformToPackageInBundle(List<PackageInBundleGD> list) {
        List<PackageInBundle> sentPackages = new ArrayList<>();
        if (list == null || list.size() <= 0) {
            return sentPackages;
        }
        for (PackageInBundleGD packageInBundleGD : list) {
            if (packageInBundleGD.getId() <= 0) {
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
        return new PackageInBundle(packageInBundleGD.getId(), packageInBundleGD.getBundleID(), packageInBundleGD.getRevZaloPayID(), packageInBundleGD.getRevZaloID(), packageInBundleGD.getRevFullName(), packageInBundleGD.getRevAvatarURL(), packageInBundleGD.getOpenTime(), packageInBundleGD.getAmount(), packageInBundleGD.getSendMessage(), packageInBundleGD.getIsLuckiest() == 1);
    }

    public SentBundle transform(SentBundleGD sentBundleGD) {
        List<PackageInBundle> sentPackages = transformToPackageInBundle(sentBundleGD.getSentPackages());
        return new SentBundle(sentBundleGD.getId(), sentBundleGD.getSenderZaloPayID(),
                sentBundleGD.getType(), sentBundleGD.getCreateTime(),
                sentBundleGD.getLastOpenTime(), sentBundleGD.getTotalLuck(),
                sentBundleGD.getNumOfOpenedPakages(), sentBundleGD.getNumOfPackages(),
                sentBundleGD.getSendMessage(), sentBundleGD.getStatus(), sentPackages);
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
        return new SentBundleGD(sentBundle.bundleID, sentBundle.sendZaloPayID,
                sentBundle.type, sentBundle.createTime,
                sentBundle.lastOpenTime, sentBundle.totalLuck,
                sentBundle.numOfOpenedPakages, sentBundle.numOfPackages,
                sentBundle.sendMessage, sentBundle.status);
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
        return new GetSentBundle(sentBundleSummaryDB.getTotalOfSentAmount(),
                sentBundleSummaryDB.getTotalOfSentBundle(), null);
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
                    bundleResponse.sendmessage, BundleStatusResponse.BundleStatusEnum.AVAILABLE.getValue()));
        }
        return sentBundleList;
    }

    public ReceivePackage transform(ReceivePackageGD receivePackageGD) {
        if (receivePackageGD == null || receivePackageGD.getId() <= 0) {
            return null;
        }

        Long openTime = receivePackageGD.getOpenedTime();
        Integer isLuckiest = receivePackageGD.getIsLuckiest();
        Long createTime = receivePackageGD.getCreateTime();
        Long amount = receivePackageGD.getAmount();
        return new ReceivePackage(receivePackageGD.getId(), receivePackageGD.getBundleID(),
                receivePackageGD.getReceiverZaloPayID(), receivePackageGD.getSenderZaloPayID(),
                receivePackageGD.getSenderFullName(),
                receivePackageGD.getSenderAvatar(),
                receivePackageGD.getMessage(),
                amount == null ? 0 : amount,
                openTime == null ? 0 : openTime,
                isLuckiest == null ? 0 : isLuckiest,
                createTime == null ? 0 : createTime,
                receivePackageGD.getStatus());
    }

    public List<ReceivePackage> transformDBToRevPackets(List<ReceivePackageGD> list) {
        if (Lists.isEmptyOrNull(list)) {
            return emptyList();
        }
        List<ReceivePackage> receivePackages = new ArrayList<>();
        for (ReceivePackageGD receivePackageGD : list) {
            if (receivePackageGD == null || receivePackageGD.getId() <= 0) {
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
        return new ReceivePackageGD(receivePackage.packageID, receivePackage.bundleID,
                receivePackage.revZaloPayID, receivePackage.senderZaloPayID,
                receivePackage.senderFullName, receivePackage.senderAvatar,
                receivePackage.amount,
                receivePackage.openedTime,
                receivePackage.status,
                receivePackage.message,
                receivePackage.isLuckiest,
                receivePackage.createTime
        );
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
        return new GetReceivePacket(receivePacketSummarydb.getTotalOfRevamount(),
                receivePacketSummarydb.getTotalOfRevPackage(),
                receivePacketSummarydb.getTotalOfLuckiestDraw(),
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
                appConfigResponse.maxTotalAmountPerBundle, appConfigResponse.minAmounTeach);
    }
}
