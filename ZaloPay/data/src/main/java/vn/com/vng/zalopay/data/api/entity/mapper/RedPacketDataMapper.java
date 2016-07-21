package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.response.redpacket.RevPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentPackageGD;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SentPackage;

import static java.util.Collections.emptyList;

/**
 * Created by longlv on 16/07/2016.
 */

@Singleton
public class RedPacketDataMapper {

    @Inject
    public RedPacketDataMapper() {

    }

    public List<ReceivePackage> transformToReceivePackage(List<ReceivePackageGD> list) {
        if (Lists.isEmptyOrNull(list)) {
            return emptyList();
        }
        List<ReceivePackage> receivePackages = new ArrayList<>();
        for (ReceivePackageGD receivePackageGD: list) {
            if (receivePackageGD == null || receivePackageGD.getId() <= 0) {
                continue;
            }
            ReceivePackage receivePackage = transform(receivePackageGD);
            receivePackages.add(receivePackage);

        }
        return receivePackages;
    }

    private ReceivePackage transform(ReceivePackageGD receivePackageGD) {
        if (receivePackageGD == null || receivePackageGD.getId() <= 0) {
            return null;
        }
        return new ReceivePackage(receivePackageGD.getPackageId(), receivePackageGD.getId(), receivePackageGD.getSendZaloPayID(), receivePackageGD.getSendFullName(), receivePackageGD.getAmount(), receivePackageGD.getOpenedTime());
    }

    public List<ReceivePackageGD> transformToReceivePackageDB(List<ReceivePackage> receivePackages) {
        if (Lists.isEmptyOrNull(receivePackages)) {
            return emptyList();
        }
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();
        for (ReceivePackage receivePackage: receivePackages) {
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
        return new ReceivePackageGD(receivePackage.bundleID, receivePackage.packageID, receivePackage.sendZaloPayID, receivePackage.sendFullName, receivePackage.amount, receivePackage.openedTime);
    }

    private SentBundle transform(SentBundleGD sentBundleGD) {
        List<SentPackage> sentPackages = transformToSentPackage(sentBundleGD.getSentPackages());
        return new SentBundle(sentBundleGD.getId(), sentBundleGD.getType(), sentBundleGD.getCreateTime(), sentBundleGD.getLastOpenTime(), sentBundleGD.getTotalLuck(), sentBundleGD.getNumOfOpenedPakages(), sentBundleGD.getNumOfPackages(), sentPackages);
    }

    public List<SentPackageGD> transformToSentPackageGD(List<SentPackage> sentPackages, long bundleID) {
        List<SentPackageGD> sentPackageGDs = new ArrayList<>();
        if (sentPackages == null || sentPackages.size() <= 0) {
            return sentPackageGDs;
        }
        for (SentPackage sentPackage : sentPackages) {
            if (sentPackage == null || sentPackage.revZaloID <= 0) {
                continue;
            }
            SentPackageGD sentPackageGD = new SentPackageGD(bundleID, sentPackage.revZaloPayID, sentPackage.revZaloID, sentPackage.revFullName, sentPackage.revAvatarURL, sentPackage.openTime, sentPackage.amount, sentPackage.sendMessage, sentPackage.isLuckiest?1:0);
            sentPackageGDs.add(sentPackageGD);
        }
        return sentPackageGDs;
    }

    public List<SentPackage> transformToSentPackage(List<SentPackageGD> list) {
        List<SentPackage> sentPackages = new ArrayList<>();
        if (list == null || list.size() <= 0) {
            return  sentPackages;
        }
        for (SentPackageGD sentPackageGD : list) {
            if (sentPackageGD.getId() <= 0) {
                continue;
            }
            SentPackage sentPackage = transform(sentPackageGD);
            sentPackages.add(sentPackage);
        }
        return null;
    }

    private SentPackage transform(SentPackageGD sentPackageGD) {
        if (sentPackageGD == null) {
            return  null;
        }
        return new SentPackage(sentPackageGD.getRevZaloPayID(), sentPackageGD.getRevZaloID(), sentPackageGD.getRevFullName(), sentPackageGD.getRevAvatarURL(), sentPackageGD.getOpenTime(), sentPackageGD.getAmount(), sentPackageGD.getSendMessage(), sentPackageGD.getIsLuckiest()==1);
    }

    public List<SentBundle> transformToSentBundle(List<SentBundleGD> list) {
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
            return  null;
        }
        return new SentBundleGD(sentBundle.bundleID, sentBundle.type, sentBundle.createTime, sentBundle.lastOpenTime, sentBundle.totalLuck, sentBundle.numOfOpenedPakages, sentBundle.numOfPackages);
    }

    public List<SentBundle> transformToSentBundle(SentBundleListResponse sentBundleResponse) {
        return null;
    }

    public List<SentPackage> transformToSentPackage(SentPackageInBundleResponse packageInBundlesResponse) {
        return null;
    }

    public List<ReceivePackage> transformToReceivePackage(RevPackageInBundleResponse revPackageInBundleResponse) {
        return null;
    }
}
