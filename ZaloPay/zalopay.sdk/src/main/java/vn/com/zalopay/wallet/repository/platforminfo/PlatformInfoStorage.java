package vn.com.zalopay.wallet.repository.platforminfo;

import android.text.TextUtils;

import rx.Observable;
import rx.functions.Func0;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoStorage extends AbstractLocalStorage implements PlatformInfoStore.LocalStorage {

    public PlatformInfoStorage(SharedPreferencesManager pSharedPreferencesManager) {
        super(pSharedPreferencesManager);
    }

    private boolean isUpdatePlatformInfoOnCache(String pPlatformInfoCheckSum) {
        String checksumOnCache = getChecksumSDK();
        return (!TextUtils.isEmpty(checksumOnCache) && !checksumOnCache.equals(pPlatformInfoCheckSum));
    }

    @Override
    public Observable<PlatformInfoCallback> get() {
        return Observable.defer(new Func0<Observable<PlatformInfoCallback>>() {
            @Override
            public Observable<PlatformInfoCallback> call() {
                try {
                    Long expireTime = getExpireTime();
                    PlatformInfoCallback platformInfoCallback = new PlatformInfoCallback(expireTime);
                    Log.d(this, "load app info from cache", expireTime);
                    return Observable.just(platformInfoCallback);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }

    @Override
    public void put(String userId, PlatformInfoResponse pResponse) {
        try {
            Log.d(this, "start save platform info to cache", pResponse);
            if (pResponse == null || pResponse.returncode != 1) {
                Log.d(this, "request not success...stopping saving response to cache");
                return;
            }
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            mSharedPreferences.setPlatformInfoExpriedTime(expiredTime);
            mSharedPreferences.setPlatformInfoExpriedTimeDuration(pResponse.expiredtime);
            Log.d(this, "update platform info expire time", expiredTime);
            //enable/disable deposite
            mSharedPreferences.setEnableDeposite(pResponse.isenabledeposit);
            Log.d(this, "save isenabledeposit to cache", pResponse.isenabledeposit);
            //set maintenance withdraw
            Maintenance maintenance = new Maintenance();
            maintenance.ismaintainwithdraw = pResponse.ismaintainwithdraw;
            maintenance.maintainwithdrawfrom = pResponse.maintainwithdrawfrom;
            maintenance.maintainwithdrawto = pResponse.maintainwithdrawto;
            mSharedPreferences.setMaintenanceWithDraw(GsonUtils.toJsonString(maintenance));
            Log.d(this, "save ismaintainwithdraw to cache", maintenance);
            // need to update cache data if chechsum is changed.
            if (isUpdatePlatformInfoOnCache(pResponse.platforminfochecksum)) {
                mSharedPreferences.setChecksumSDK(pResponse.platforminfochecksum);
                mSharedPreferences.setCurrentUserID(userId);
                //banner list for merchant
                if (pResponse.bannerresources != null) {
                    mSharedPreferences.setBannerList(GsonUtils.toJsonString(pResponse.bannerresources));
                }
                if (pResponse.approvedinsideappids != null) {
                    mSharedPreferences.setApproveInsideApps(GsonUtils.toJsonString(pResponse.approvedinsideappids));
                }
            }
            //need to update card info again on cache
            if (MapCardHelper.needUpdateMapCardListOnCache(pResponse.cardinfochecksum)) {
                //for testing
               /* MapCard mapCard = new MapCard();
                mapCard.bankcode = CardType.PVTB;
                mapCard.cardname = "VO VAN CHUC";
                mapCard.last4cardno = "8156";
                mapCard.first6cardno = "970415";
                pResponse.cardinfos.add(mapCard);*/
                MapCardHelper.saveMapCardListToCache(userId, pResponse.cardinfochecksum, pResponse.cardinfos);
            }
            //update bank account info on cache
            // Test in case already linked account Vietcombank
//        BankAccount dBankAccount = new BankAccount();
//        dBankAccount.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank);
//        dBankAccount.firstaccountno = "093490";
//        dBankAccount.lastaccountno = "9460";
//        pResponse.bankaccounts.add(dBankAccount);
            // ===============================================
            if (BankAccountHelper.needUpdateMapBankAccountListOnCache(pResponse.bankaccountchecksum)) {
                BankAccountHelper.saveMapBankAccountListToCache(userId, pResponse.bankaccountchecksum, pResponse.bankaccounts);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    public long getExpireTimeDuration() {
        long expiretime = 0;
        try {
            expiretime = mSharedPreferences.getPlatformInfoExpriedTimeDuration();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return expiretime;
    }

    @Override
    public long getExpireTime() {
        long expiretime = 0;
        try {
            expiretime = mSharedPreferences.getPlatformInfoExpriedTime();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return expiretime;
    }

    @Override
    public String getChecksumSDK() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getChecksumSDK();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return checksum;
    }

    @Override
    public String getChecksumSDKVersion() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getChecksumSDKversion();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return checksum;
    }

    @Override
    public String getResourceVersion() {
        String resoureVersion = null;
        try {
            resoureVersion = mSharedPreferences.getResourceVersion();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return resoureVersion;
    }

    @Override
    public void setResourceVersion(String resourceVersion) {
        mSharedPreferences.setResourceVersion(resourceVersion);
    }

    @Override
    public String getCardInfoCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getCardInfoCheckSum();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return checksum;
    }

    @Override
    public void setCardInfoCheckSum(String checkSum) {
        mSharedPreferences.setCardInfoCheckSum(checkSum);
    }

    @Override
    public String getBankAccountCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getBankAccountCheckSum();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return checksum;
    }

    @Override
    public void setBankAccountCheckSum(String checkSum) {
        mSharedPreferences.setBankAccountCheckSum(checkSum);
    }

    @Override
    public void setResourceDownloadUrl(String resourceDownloadUrl) {
        mSharedPreferences.setResourceDownloadUrl(resourceDownloadUrl);
    }

    @Override
    public String getUserId() {
        String userId = null;
        try {
            userId = mSharedPreferences.getCurrentUserID();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return userId;
    }

    @Override
    public void clearCardMapCheckSum() {
        mSharedPreferences.setCardInfoCheckSum(null);
    }

    @Override
    public void clearBankAccountMapCheckSum() {
        mSharedPreferences.setBankAccountCheckSum(null);
    }
}
