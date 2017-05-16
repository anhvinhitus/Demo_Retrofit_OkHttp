package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.ReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleResponse;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.redpacket.BundleStatusEnum;
import vn.com.vng.zalopay.domain.model.redpacket.GetReceivePacket;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

/**
 * Created by longlv on 16/07/2016.
 * Transform RedPacket data entities
 */

@Singleton
public class RedPacketDataMapper {

    @Inject
    public RedPacketDataMapper() {

    }

//    public PackageInBundleGD transform(PackageInBundle sentPackage) {
//        if (sentPackage == null || sentPackage.revZaloID <= 0) {
//            return null;
//        }
//
//        PackageInBundleGD item = new PackageInBundleGD();
//        item.id = sentPackage.packageID;
//        item.bundleID = sentPackage.bundleID;
//        item.revZaloPayID = sentPackage.revZaloPayID;
//        item.revZaloID = sentPackage.revZaloID;
//        item.revFullName = sentPackage.revFullName;
//        item.revAvatarURL = sentPackage.revAvatarURL;
//        item.openTime = sentPackage.openTime;
//        item.amount = sentPackage.amount;
//        item.sendMessage = sentPackage.sendMessage;
//        item.isLuckiest = sentPackage.isLuckiest ? 1L : 0L;
//        return item;
//    }
//
//    public PackageInBundle transform(PackageInBundleGD packageInBundleGD) {
//        if (packageInBundleGD == null || packageInBundleGD.id <= 0) {
//            return null;
//        }
//
//        PackageInBundle item = new PackageInBundle();
//        item.packageID = ConvertHelper.unboxValue(packageInBundleGD.id, 0L);
//        item.bundleID = ConvertHelper.unboxValue(packageInBundleGD.bundleID, 0L);
//        item.revZaloPayID = packageInBundleGD.revZaloPayID;
//        item.revZaloID = packageInBundleGD.revZaloID;
//        item.revFullName = packageInBundleGD.revFullName;
//        item.revAvatarURL = packageInBundleGD.revAvatarURL;
//        item.openTime = ConvertHelper.unboxValue(packageInBundleGD.openTime, 0L);
//        item.amount = ConvertHelper.unboxValue(packageInBundleGD.amount, 0L);
//        item.sendMessage = packageInBundleGD.sendMessage;
//        item.isLuckiest = (ConvertHelper.unboxValue(packageInBundleGD.isLuckiest, 0L) == 1L);
//        return item;
//    }
//
//    public SentBundle transform(SentBundleGD sentBundleGD) {
//        List<PackageInBundle> sentPackages = Lists.transform(sentBundleGD.getSentPackages(), this::transform);
//        SentBundle item = new SentBundle();
//        item.bundleID = sentBundleGD.id;
//        item.sendZaloPayID = sentBundleGD.senderZaloPayID;
//        item.type = ConvertHelper.unboxValue(sentBundleGD.type, 0L);
//        item.createTime = ConvertHelper.unboxValue(sentBundleGD.createTime, 0L);
//        item.lastOpenTime = ConvertHelper.unboxValue(sentBundleGD.lastOpenTime, 0L);
//        item.totalLuck = ConvertHelper.unboxValue(sentBundleGD.totalLuck, 0L);
//        item.numOfOpenedPakages = ConvertHelper.unboxValue(sentBundleGD.numOfOpenedPakages, 0L);
//        item.numOfPackages = ConvertHelper.unboxValue(sentBundleGD.numOfPackages, 0L);
//        item.sendMessage = sentBundleGD.sendMessage;
//        item.status = ConvertHelper.unboxValue(sentBundleGD.status, 0L);
//        item.packages = sentPackages;
//        return item;
//    }
//
//    public List<PackageInBundle> transformToPackageInBundle(SentPackageInBundleResponse packageInBundlesResponse) {
//        List<PackageInBundle> sentPackageList = new ArrayList<>();
//        if (packageInBundlesResponse == null
//                || packageInBundlesResponse.packageResponses == null
//                || packageInBundlesResponse.packageResponses.size() <= 0) {
//            return sentPackageList;
//        }
//        for (PackageInBundleResponse response : packageInBundlesResponse.packageResponses) {
//            if (response == null) {
//                continue;
//            }
//            PackageInBundle item = new PackageInBundle();
//            item.packageID = response.packageid;
//            item.bundleID = response.bundleid;
//            item.revZaloPayID = response.revzalopayid;
//            item.revZaloID = response.revzaloid;
//            item.revFullName = response.revfullname;
//            item.revAvatarURL = response.revavatarurl;
//            item.openTime = response.opentime;
//            item.amount = response.amount;
//            item.sendMessage = response.message;
//            item.isLuckiest = response.isluckiest;
//
//            sentPackageList.add(item);
//        }
//        return sentPackageList;
//    }

//    public List<SentBundle> transformDBToSentBundles(List<SentBundleGD> list) {
//        if (Lists.isEmptyOrNull(list)) {
//            return emptyList();
//        }
//
//        List<SentBundle> sentBundles = new ArrayList<>(list.size());
//        for (SentBundleGD sentBundleGD : list) {
//            SentBundle sentBundle = transform(sentBundleGD);
//            if (sentBundle == null) {
//                continue;
//            }
//
//            sentBundles.add(sentBundle);
//        }
//
//        return sentBundles;
//    }
//
//    public List<SentBundleGD> transformToSenBundleGD(List<SentBundle> list) {
//        if (Lists.isEmptyOrNull(list)) {
//            return emptyList();
//        }
//
//        List<SentBundleGD> sentBundleGDs = new ArrayList<>();
//        for (SentBundle sentBundle : list) {
//            SentBundleGD sentBundleGD = transform(sentBundle);
//            if (sentBundleGD == null) {
//                continue;
//            }
//
//            sentBundleGDs.add(sentBundleGD);
//        }
//
//        return sentBundleGDs;
//    }
//
//    public SentBundleGD transform(SentBundle sentBundle) {
//        if (sentBundle == null) {
//            return null;
//        }
//
//        SentBundleGD item = new SentBundleGD();
//        item.id = sentBundle.bundleID;
//        item.senderZaloPayID = sentBundle.sendZaloPayID;
//        item.type = sentBundle.type;
//        item.createTime = sentBundle.createTime;
//        item.lastOpenTime = sentBundle.lastOpenTime;
//        item.totalLuck = sentBundle.totalLuck;
//        item.numOfOpenedPakages = sentBundle.numOfOpenedPakages;
//        item.numOfPackages = sentBundle.numOfPackages;
//        item.sendMessage = sentBundle.sendMessage;
//        item.status = sentBundle.status;
//        return item;
//    }
//
//    public GetSentBundle transformToSentBundleSummary(SentBundleListResponse response) {
//        if (response == null) {
//            return null;
//        }
//
//        List<SentBundle> sentBundles = Lists.transform(response.bundleResponseList, this::transform);
//        GetSentBundle item = new GetSentBundle();
//        item.totalofsentamount = response.totalOfSentAmount;
//        item.totalofsentbundle = response.totalOfSentBundle;
//        item.sentbundlelist = sentBundles;
//        return item;
//    }
//
//    public GetSentBundle transformToSentBundleSummary(List<SentBundleSummaryDB> list) {
//        if (list == null || list.size() <= 0 || list.get(0) == null) {
//            return null;
//        }
//
//        SentBundleSummaryDB sentBundleSummaryDB = list.get(0);
//        GetSentBundle item = new GetSentBundle();
//        item.totalofsentamount = ConvertHelper.unboxValue(sentBundleSummaryDB.totalOfSentAmount, 0L);
//        item.totalofsentbundle = ConvertHelper.unboxValue(sentBundleSummaryDB.totalOfSentBundle, 0L);
//        item.sentbundlelist = null;
//
//        return item;
//    }

    private SentBundle transform(SentBundleResponse bundleResponse) {
        SentBundle item = new SentBundle();
        item.bundleID = bundleResponse.bundleid;
        item.sendZaloPayID = bundleResponse.sendzalopayid;
        item.type = bundleResponse.type;
        item.createTime = bundleResponse.createtime;
        item.lastOpenTime = bundleResponse.lastopentime;
        item.totalLuck = bundleResponse.totalluck;
        item.numOfOpenedPakages = bundleResponse.numofopenedpakages;
        item.numOfPackages = bundleResponse.numofpackages;
        item.sendMessage = bundleResponse.sendmessage;
        item.status = BundleStatusEnum.AVAILABLE.getValue();
        return item;
    }

    public List<ReceivePackage> transform(List<ReceivePackageGD> list) {
        if (Lists.isEmptyOrNull(list)) {
            return Collections.emptyList();
        }
        List<ReceivePackage> receivePackageList = new ArrayList<>();
        for (ReceivePackageGD receivePackageGD : list) {
            if (receivePackageGD == null) {
                continue;
            }
            receivePackageList.add(transform(receivePackageGD));
        }
        return receivePackageList;
    }

    public ReceivePackage transform(ReceivePackageGD receivePackageGD) {
        if (receivePackageGD == null || receivePackageGD.id <= 0) {
            return null;
        }

        ReceivePackage item = new ReceivePackage();
        item.packageID = receivePackageGD.id;
        item.bundleID = ConvertHelper.unboxValue(receivePackageGD.bundleID, 0L);
        item.revZaloPayID = receivePackageGD.receiverZaloPayID;
        item.senderZaloPayID = receivePackageGD.senderZaloPayID;
        item.senderFullName = receivePackageGD.senderFullName;
        item.senderAvatar = receivePackageGD.senderAvatar;
        item.message = receivePackageGD.message;
        item.amount = ConvertHelper.unboxValue(receivePackageGD.amount, 0L);
        item.openedTime = ConvertHelper.unboxValue(receivePackageGD.openedTime, 0L);
        item.isLuckiest = ConvertHelper.unboxValue(receivePackageGD.isLuckiest, 0L);
        item.createTime = ConvertHelper.unboxValue(receivePackageGD.createTime, 0L);
        item.status = ConvertHelper.unboxValue(receivePackageGD.status, 0L);
        return item;
    }

    public ReceivePackageGD transform(ReceivePackage receivePackage) {
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

        GetReceivePacket item = new GetReceivePacket();
        item.totalofrevamount = response.totalOfRevAmount;
        item.totalofrevpackage = response.totalOfRevPackage;
        item.numofluckiestdraw = response.numOfLuckiestDraw;
        item.revpackageList = receivePackages;

        return item;
    }

    private List<ReceivePackage> transformToRevPackets(List<ReceivePackageResponse> revpackageList) {
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

            ReceivePackage item = new ReceivePackage();
            item.packageID = response.packageid;
            item.bundleID = response.bundleid;
            item.revZaloPayID = response.revzalopayid;
            item.senderZaloPayID = response.sendzalopayid;
            item.senderFullName = response.sendfullname;
            item.senderAvatar = response.avatarofsender;
            item.message = response.sendmessage;
            item.amount = response.amount;
            item.openedTime = response.openedtime;
            item.isLuckiest = response.isluckiest;
            item.createTime = response.createtime;
            item.status = RedPacketStatus.Opened.getValue();
            receivePackages.add(item);
        }
        return receivePackages;
    }

//    public GetReceivePacket transformToReceivePacketSummary(List<ReceivePacketSummaryDB> list) {
//        if (list == null || list.size() <= 0 || list.get(0) == null) {
//            return null;
//        }
//        ReceivePacketSummaryDB receivePacketSummarydb = list.get(0);
//
//        GetReceivePacket item = new GetReceivePacket();
//        item.totalofrevamount = ConvertHelper.unboxValue(receivePacketSummarydb.totalOfRevamount, 0L);
//        item.totalofrevpackage = ConvertHelper.unboxValue(receivePacketSummarydb.totalOfRevPackage, 0L);
//        item.numofluckiestdraw = ConvertHelper.unboxValue(receivePacketSummarydb.totalOfLuckiestDraw, 0L);
//        item.revpackageList = null;
//
//        return item;
//    }
//
//    public List<BundleGD> transformToBundleGD(GetSentBundle getSentBundle) {
//        List<BundleGD> bundleGDs = new ArrayList<>();
//        if (Lists.isEmptyOrNull(getSentBundle.sentbundlelist)) {
//            return bundleGDs;
//        }
//        for (int i = 0; i < getSentBundle.sentbundlelist.size(); i++) {
//            SentBundle sentBundle = getSentBundle.sentbundlelist.get(i);
//            if (sentBundle == null) {
//                continue;
//            }
//            BundleGD bundleGD = new BundleGD();
//            bundleGD.id = sentBundle.bundleID;
//            bundleGD.createTime = sentBundle.createTime;
//            bundleGD.lastTimeGetPackage = null;
//            bundleGDs.add(bundleGD);
//        }
//        return bundleGDs;
//    }
//
//    public List<BundleGD> transformToBundleGD(GetReceivePacket getReceivePacket) {
//        List<BundleGD> bundleGDs = new ArrayList<>();
//        if (Lists.isEmptyOrNull(getReceivePacket.revpackageList)) {
//            return bundleGDs;
//        }
//        for (int i = 0; i < getReceivePacket.revpackageList.size(); i++) {
//            ReceivePackage receivePackage = getReceivePacket.revpackageList.get(i);
//            if (receivePackage == null) {
//                continue;
//            }
//            BundleGD bundleGD = new BundleGD();
//            bundleGD.id = receivePackage.bundleID;
//            bundleGD.createTime = receivePackage.openedTime;
//            bundleGD.lastTimeGetPackage = null;
//            bundleGDs.add(bundleGD);
//        }
//        return bundleGDs;
//    }
}
