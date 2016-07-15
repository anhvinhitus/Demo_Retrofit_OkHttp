package vn.com.vng.zalopay.data.redpacket;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentPackageGD;
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

    public RedPackageLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void putSentBundle(SentBundle sentBundle) {
        if (sentBundle == null || sentBundle.bundleID <= 0) {
            return;
        }
        try {
            //save SentBundle to DB
            SentBundleGD sentBundleGD = new SentBundleGD(sentBundle.bundleID, sentBundle.type, sentBundle.createTime, sentBundle.lastOpenTime, sentBundle.totalLuck, sentBundle.numOfOpenedPakages, sentBundle.numOfPackages);
            getDaoSession().getSentBundleGDDao().insertOrReplaceInTx(sentBundleGD);
            //save SentPackage of SentBundle to DB
            putSentPackage(sentBundle.packages, sentBundle.bundleID);
            Timber.d("putRedPackage sentBundleGD %s", sentBundleGD);
        } catch (Exception e) {
            Timber.w("Exception while trying to put RedPackage to local storage: %s", e.getMessage());
        }
    }

    @Override
    public void putSentPackage(List<SentPackage> sentPackages, long bundleID) {
        if (bundleID <= 0 || sentPackages == null || sentPackages.size() <= 0) {
            return;
        }
        try {
            List<SentPackageGD> sentPackageGDs = transform(sentPackages, bundleID);
            getDaoSession().getSentPackageGDDao().insertOrReplaceInTx(sentPackageGDs);

            Timber.d("putRedPackage redPackage %s", sentPackageGDs);
        } catch (Exception e) {
            Timber.w("Exception while trying to put RedPackage to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<SentBundle>> getAllSentBundle() {
        return ObservableHelper.makeObservable(this::querySentBundleList)
                .doOnNext(redPackageList -> Timber.d("get %s", redPackageList.size()));
    }

    @Override
    public void putReceivePackages(List<ReceivePackage> receivePackages) {
        if (Lists.isEmptyOrNull(receivePackages)) {
            emptyList();
        }
        try {
            List<ReceivePackageGD> receivePackageGDs = transformToReceivePackageDB(receivePackages);
            getDaoSession().getReceivePackageGDDao().insertOrReplaceInTx(receivePackageGDs);

            Timber.d("putRedPackage redPackage %s", receivePackageGDs);
        } catch (Exception e) {
            Timber.w("Exception while trying to put RedPackage to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<ReceivePackage>> getAllReceivePackage() {
        return ObservableHelper.makeObservable(this::queryReceivePackageList)
                .doOnNext(receivePackageList -> Timber.d("get %s", receivePackageList.size()));
    }

    private List<ReceivePackage> queryReceivePackageList() {
        return transformToReceivePackage(
                getDaoSession()
                        .getReceivePackageGDDao()
                        .queryBuilder()
                        .list());
    }

    private List<ReceivePackage> transformToReceivePackage(List<ReceivePackageGD> list) {
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

    private List<ReceivePackageGD> transformToReceivePackageDB(List<ReceivePackage> receivePackages) {
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

    private List<SentBundle> querySentBundleList() {
        return transform(
                getDaoSession()
                        .getSentBundleGDDao()
                        .queryBuilder()
                        .list());
    }

    private SentBundle transform(SentBundleGD sentBundleGD) {
        List<SentPackage> sentPackages = transformToSentPackage(sentBundleGD.getSentPackages());
        return new SentBundle(sentBundleGD.getId(), sentBundleGD.getType(), sentBundleGD.getCreateTime(), sentBundleGD.getLastOpenTime(), sentBundleGD.getTotalLuck(), sentBundleGD.getNumOfOpenedPakages(), sentBundleGD.getNumOfPackages(), sentPackages);
    }

    private List<SentPackageGD> transform(List<SentPackage> sentPackages, long bundleID) {
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

    private List<SentPackage> transformToSentPackage(List<SentPackageGD> list) {
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

    private List<SentBundle> transform(List<SentBundleGD> list) {
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
}
