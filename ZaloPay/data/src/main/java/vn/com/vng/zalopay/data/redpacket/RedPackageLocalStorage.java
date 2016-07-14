package vn.com.vng.zalopay.data.redpacket;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.redpackage.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.RedPackageGD;
import vn.com.vng.zalopay.data.cache.model.RedPackageGDDao;
import vn.com.vng.zalopay.data.cache.model.RedPackageItemGD;
import vn.com.vng.zalopay.data.cache.model.RedPackageItemGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.RedPackage;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;

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
    public void putRedPackage(long bundleId, int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
        try {
            RedPackageGD redPackage = new RedPackageGD(bundleId, quantity, totalLuck, amountEach, type, sendMessage, "", RedPackage.RedPackageState.CREATE.getValue());
            getDaoSession().getRedPackageGDDao().insertOrReplaceInTx(redPackage);

            Timber.d("putRedPackage redPackage %s", redPackage);
        } catch (Exception e) {
            Timber.w("Exception while trying to put RedPackage to local storage: %s", e.getMessage());
        }
    }

    @Override
    public void updateRedPackage(long bundleId, int state) {
        try {
            if (bundleId <= 0) {
                return;
            }
            RedPackageGD redPackageGD = getRedPackage(bundleId);
            if (redPackageGD == null) {
                return;
            }
            getDaoSession().getRedPackageGDDao().insertOrReplace(redPackageGD);
        } catch (Exception e) {
            Timber.w("Exception while trying to put redPackageItem to local storage: %s", e.getMessage());
        }
    }

    @Override
    public void putRedPackageItem(long packageId, long bundleId, String zpTransID, int state) {
        try {
            if (packageId <= 0) {
                return;
            }
            RedPackageItemGD redPackageItemGD = getRedPackageItem(packageId);
            if (redPackageItemGD == null) {
                return;
            }
            getDaoSession().getRedPackageItemGDDao().insertOrReplaceInTx(redPackageItemGD);

            Timber.d("putRedPackage redPackageItemGD %s", redPackageItemGD);
        } catch (Exception e) {
            Timber.w("Exception while trying to put redPackageItem to local storage: %s", e.getMessage());
        }
    }

    private RedPackageGD getRedPackage(long bundleId) {
        List<RedPackageGD> redPackageList = getDaoSession().getRedPackageGDDao().queryBuilder().where(RedPackageGDDao.Properties.Id.eq(bundleId)).limit(1).list();
        if (redPackageList == null || redPackageList.size() <= 0) {
            return null;
        }
        return redPackageList.get(0);
    }

    private RedPackageItemGD getRedPackageItem(long packageId) {
        List<RedPackageItemGD> redPackageList = getDaoSession().getRedPackageItemGDDao().queryBuilder().where(RedPackageItemGDDao.Properties.Id.eq(packageId)).limit(1).list();
        if (redPackageList == null || redPackageList.size() <= 0) {
            return null;
        }
        return redPackageList.get(0);
    }

    @Override
    public Observable<List<RedPackage>> getAllRedPackage() {
        return ObservableHelper.makeObservable(this::queryRedPackageList)
                .doOnNext(redPackageList -> Timber.d("get %s", redPackageList.size()));
    }

    private List<RedPackage> queryRedPackageList() {
        return transform(
                getDaoSession()
                        .getRedPackageGDDao()
                        .queryBuilder()
                        .orderDesc(RedPackageGDDao.Properties.Id)
                        .where(RedPackageGDDao.Properties.State.notEq(RedPackage.RedPackageState.CREATE.getValue()))
                        .list());
    }

    private RedPackage transform(RedPackageGD bundleOrderGD) {
        if (bundleOrderGD == null) {
            return null;
        }
        return new RedPackage(bundleOrderGD.getId(), bundleOrderGD.getQuantity(), bundleOrderGD.getTotalLuck(), bundleOrderGD.getAmountEach(), bundleOrderGD.getType(), bundleOrderGD.getSendMessage());
    }

    private List<RedPackage> transform(List<RedPackageGD> redPackageList) {
        if (Lists.isEmptyOrNull(redPackageList)) {
            return emptyList();
        }

        List<RedPackage> redPackages = new ArrayList<>(redPackageList.size());
        for (RedPackageGD redPackageGD : redPackageList) {
            RedPackage redPackage = transform(redPackageGD);
            if (redPackage == null) {
                continue;
            }

            redPackages.add(redPackage);
        }

        return redPackages;
    }

    private SubmitOpenPackage transform(SubmitOpenPackageResponse redPackageResponse) {
        if (redPackageResponse == null)
            return null;
        return new SubmitOpenPackage(redPackageResponse.bundleId, redPackageResponse.packageID, redPackageResponse.zpTransID);
    }

}
