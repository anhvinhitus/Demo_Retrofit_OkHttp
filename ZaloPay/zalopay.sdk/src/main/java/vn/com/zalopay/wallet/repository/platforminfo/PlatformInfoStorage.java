package vn.com.zalopay.wallet.repository.platforminfo;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.entity.response.PlatformInfoResponse;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.interactor.ILinkSourceInteractor;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;
import vn.com.zalopay.wallet.repository.SharedPreferencesManager;

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
    public void put(String userId, PlatformInfoResponse pResponse) {
        try {
            if (pResponse == null || pResponse.returncode != 1) {
                Timber.d("request not success...stopping saving response to cache");
                return;
            }
            Timber.d("start update platform info to cache %s", GsonUtils.toJsonString(pResponse));
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            setExpireTime(expiredTime);
            mSharedPreferences.setAppVersion(SdkUtils.getAppVersion(GlobalData.getAppContext()));
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
            Timber.d("save maintain withdraw to cache %s", GsonUtils.toJsonString(maintenance));
            // need to update cache data if chechsum is changed.
            if (isUpdatePlatformInfoOnCache(pResponse.platforminfochecksum)) {
                setCheckSum(pResponse.platforminfochecksum);
            }

            /*if (pResponse.cardinfos == null) {
                pResponse.cardinfos = new ArrayList<>();
            }
            MapCard mapCard = new MapCard();
            mapCard.bankcode = CardType.PVTB;
            mapCard.cardname = "VO VAN CHUC";
            mapCard.last4cardno = "8156";
            mapCard.first6cardno = "970415";
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
            pResponse.cardinfos.add(mapCard);

            // Test in case already linked account Vietcombank
            BankAccount dBankAccount = new BankAccount();
            dBankAccount.bankcode = CardType.PVCB;
            dBankAccount.firstaccountno = "093490";
            dBankAccount.lastaccountno = "9460";
            pResponse.bankaccounts.add(dBankAccount);*/
            //update card and bank account info again on cache
            ILinkSourceInteractor linkInteractor = SDKApplication.getApplicationComponent().linkInteractor();
            linkInteractor.putCards(userId, pResponse.cardinfochecksum, pResponse.cardinfos);
            linkInteractor.putBankAccounts(userId, pResponse.bankaccountchecksum, pResponse.bankaccounts);
        } catch (Exception e) {
            Timber.d(e, "Exception save platform info");
        }
    }

    @Override
    public long getExpireTimeDuration() {
        long expiretime = 0;
        try {
            expiretime = mSharedPreferences.getPlatformInfoExpriedTimeDuration();
        } catch (Exception e) {
            Timber.d(e, "Exception getExpireTimeDuration");
        }
        return expiretime;
    }

    @Override
    public long getExpireTime() {
        long expiretime = 0;
        try {
            expiretime = mSharedPreferences.getPlatformInfoExpriedTime();
        } catch (Exception e) {
            Timber.d(e, "Exception getExpireTime");
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
            Timber.d(e, "Exception getPlatformInfoCheckSum");
        }
        return checksum;
    }

    @Override
    public String getAppVersion() {
        String appVer = null;
        try {
            appVer = mSharedPreferences.getAppVersion();
        } catch (Exception e) {
            Timber.d(e, "Exception getAppVersion");
        }
        return appVer;
    }

    @Override
    public void setAppVersion(String pAppVersion) {
        mSharedPreferences.setAppVersion(pAppVersion);
    }

    @Override
    public String getResourcePath() {
        String resourcePath = null;
        try {
            resourcePath = mSharedPreferences.getUnzipPath();
        } catch (Exception e) {
            Timber.d(e, "Exception getResourcePath");
        }
        return resourcePath;
    }

    @Override
    public void setResourcePath(String pUnzipPath) {
        mSharedPreferences.setUnzipPath(pUnzipPath);
    }

    @Override
    public String getResourceVersion() {
        String resoureVersion = null;
        try {
            resoureVersion = mSharedPreferences.getResourceVersion();
        } catch (Exception e) {
            Timber.d(e, "Exception getResourceVersion");
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
            Timber.d(e, "Exception getCardInfoCheckSum");
        }
        return checksum;
    }

    @Override
    public void setCardInfoCheckSum(String checkSum) {
        mSharedPreferences.setCardInfoCheckSum(checkSum);
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
            Timber.d(e, "Exception getBankAccountCheckSum");
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
            Timber.d(e, "Exception getUserId");
        }
        return userId;
    }

    @Override
    public void setCheckSum(String checkSum) {
        mSharedPreferences.setPlatformInfoCheckSum(checkSum);
    }

    @Override
    public boolean enableTopup() {
        try {
            return mSharedPreferences.getEnableDeposite();
        } catch (Exception ex) {
            Timber.w(ex, "Exception check enable deposit");
        }
        return true;
    }

    @Override
    public Maintenance withdrawMaintain() {
        try {
            String maintenanceOb = mSharedPreferences.getMaintenanceWithDraw();
            if (TextUtils.isEmpty(maintenanceOb)) {
                return null;
            }
            return GsonUtils.fromJsonString(maintenanceOb, Maintenance.class);
        } catch (Exception ex) {
            Timber.w(ex, "Exception get maintain withdraw");
        }
        return null;
    }

    @Override
    public SharedPreferencesManager sharePref() {
        return mSharedPreferences;
    }
}
