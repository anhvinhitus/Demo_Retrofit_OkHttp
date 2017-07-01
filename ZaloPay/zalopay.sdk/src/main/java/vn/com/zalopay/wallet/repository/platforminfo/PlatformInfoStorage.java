package vn.com.zalopay.wallet.repository.platforminfo;

import android.text.TextUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.functions.Func0;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.interactor.ILink;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

import static vn.com.zalopay.wallet.BuildConfig.CC_CODE;

/**
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoStorage extends AbstractLocalStorage implements PlatformInfoStore.LocalStorage {

    public PlatformInfoStorage(SharedPreferencesManager pSharedPreferencesManager) {
        super(pSharedPreferencesManager);
    }

    private boolean isUpdatePlatformInfoOnCache(String pPlatformInfoCheckSum) {
        String checksumOnCache = getPlatformInfoCheckSum();
        return TextUtils.isEmpty(checksumOnCache) || (!TextUtils.isEmpty(checksumOnCache) && !checksumOnCache.equals(pPlatformInfoCheckSum));
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
            if (pResponse == null || pResponse.returncode != 1) {
                Timber.d("request not success...stopping saving response to cache");
                return;
            }
            Log.d(this, "start update platform info to cache", pResponse);
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            setExpireTime(expiredTime);
            mSharedPreferences.setPlatformInfoExpriedTimeDuration(pResponse.expiredtime);
            mSharedPreferences.setCurrentUserID(userId);
            //enable/disable deposite
            mSharedPreferences.setEnableDeposite(pResponse.isenabledeposit);
            //set maintenance withdraw
            Maintenance maintenance = new Maintenance();
            maintenance.ismaintainwithdraw = pResponse.ismaintainwithdraw;
            maintenance.maintainwithdrawfrom = pResponse.maintainwithdrawfrom;
            maintenance.maintainwithdrawto = pResponse.maintainwithdrawto;
            mSharedPreferences.setMaintenanceWithDraw(GsonUtils.toJsonString(maintenance));
            Log.d(this, "save maintain withdraw to cache", maintenance);
            // need to update cache data if chechsum is changed.
            if (isUpdatePlatformInfoOnCache(pResponse.platforminfochecksum)) {
                mSharedPreferences.setPlatformInfoCheckSum(pResponse.platforminfochecksum);
            }
           /* MapCard mapCard = new MapCard();
            mapCard.bankcode = CardType.PVTB;
            mapCard.cardname = "VO VAN CHUC";
            mapCard.last4cardno = "8156";
            mapCard.first6cardno = "970415";
            if (pResponse.cardinfos == null) {
                pResponse.cardinfos = new ArrayList<>();
            }
            pResponse.cardinfos.add(mapCard);

            mapCard = new MapCard();
            mapCard.bankcode = CardType.PBIDV;
            mapCard.cardname = "DO NGOC PHI CUONG";
            mapCard.last4cardno = "1195";
            mapCard.first6cardno = "970418";
            pResponse.cardinfos.add(mapCard);

            mapCard = new MapCard();
            mapCard.bankcode = CardType.PSGCB;
            mapCard.cardname = "NGUYEN THI MAI THANH";
            mapCard.last4cardno = "1234";
            mapCard.first6cardno = "970419";
            pResponse.cardinfos.add(mapCard);

            mapCard = new MapCard();
            mapCard.bankcode = CC_CODE;
            mapCard.cardname = "NGUYEN VAN A";
            mapCard.last4cardno = "1111";
            mapCard.first6cardno = "411111";
            pResponse.cardinfos.add(mapCard);

            mapCard = new MapCard();
            mapCard.bankcode = CardType.PSCB;
            mapCard.cardname = "DO NGOC PHI CUONG";
            mapCard.last4cardno = "1195";
            mapCard.first6cardno = "970403";
            pResponse.cardinfos.add(mapCard);*/

            // Test in case already linked account Vietcombank
//        BankAccount dBankAccount = new BankAccount();
//        dBankAccount.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank);
//        dBankAccount.firstaccountno = "093490";
//        dBankAccount.lastaccountno = "9460";
//        pResponse.bankaccounts.add(dBankAccount);
            //update card and bank account info again on cache
            ILink linkInteractor = SDKApplication.getApplicationComponent().linkInteractor();
            linkInteractor.putCards(userId, pResponse.cardinfochecksum, pResponse.cardinfos);
            linkInteractor.putBankAccounts(userId, pResponse.bankaccountchecksum, pResponse.bankaccounts);
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
    public void setExpireTime(long expireTime) {
        mSharedPreferences.setPlatformInfoExpriedTime(expireTime);
    }

    @Override
    public String getPlatformInfoCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getPlatformInfoCheckSum();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return checksum;
    }

    @Override
    public String getAppVersion() {
        String appVer = null;
        try {
            appVer = mSharedPreferences.getAppVersion();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return appVer;
    }

    @Override
    public void setAppVersion(String pAppVersion) {
        mSharedPreferences.setAppVersion(pAppVersion);
    }

    @Override
    public String getUnzipPath() {
        String unzipPath = null;
        try {
            unzipPath = mSharedPreferences.getUnzipPath();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return unzipPath;
    }

    @Override
    public void setUnzipPath(String pUnzipPath) {
        mSharedPreferences.setUnzipPath(pUnzipPath);
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
    public String getResourceDownloadUrl() {
        String url = null;
        try {
            url = mSharedPreferences.getResourceDownloadUrl();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return url;
    }

    @Override
    public void setResourceDownloadUrl(String resourceDownloadUrl) {
        mSharedPreferences.setResourceDownloadUrl(resourceDownloadUrl);
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
