package vn.com.zalopay.wallet.datasource.task;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.LoadPlatformInfoImpl;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.SdkUtils;

/***
 * platform api and update platform's data on cache
 */
public class PlatformInfoTask extends BaseTask<DPlatformInfo> {
    private static final String TAG = PlatformInfoTask.class.getCanonicalName();
    private static PlatformInfoTask _object;
    private ZPWGetGatewayInfoListener mGetGatewayInfoListener;
    private boolean mForceReload;//force sdk re download everything.
    private boolean mNoDownloadResource;//app reoad platfrom info, this will be set to true to prevent download resource file again.

    /***
     * overload constructor
     * @param pListener
     * @param pForceReload
     */
    public PlatformInfoTask(ZPWGetGatewayInfoListener pListener, boolean pForceReload) {
        super();
        this.mGetGatewayInfoListener = pListener;
        this.mForceReload = pForceReload;
        this.mNoDownloadResource = false;
    }

    /***
     * @param pListener
     * @param pForceReload
     * @param pNoDownloadResource
     */
    public PlatformInfoTask(ZPWGetGatewayInfoListener pListener, boolean pForceReload, boolean pNoDownloadResource) {
        super();
        this.mGetGatewayInfoListener = pListener;
        this.mForceReload = pForceReload;
        this.mNoDownloadResource = pNoDownloadResource;
    }

    /***
     * is retry case from server.this variable is true
     * then need to reuse callable request in RequestKeeper.
     * @param pListener
     * @param pIsForceReload
     * @return
     */
    public synchronized static PlatformInfoTask getInstance(ZPWGetGatewayInfoListener pListener, boolean pIsForceReload) {
        if (PlatformInfoTask._object == null) {
            PlatformInfoTask._object = new PlatformInfoTask(pListener, pIsForceReload);
        }
        return PlatformInfoTask._object;
    }

    /***
     * process result from api
     * @throws Exception
     */
    private synchronized void saveResponseToCache(DPlatformInfo pResponse) throws Exception {
        Log.d(this, "===saveResponseToCache===");
        //enable/disable deposite
        SharedPreferencesManager.getInstance().setEnableDeposite(pResponse.isenabledeposit);
        Log.d(this, "saved pResponse.isenabledeposit to cache");
        //set maintenance withdraw
        WDMaintenance wdMaintenance = new WDMaintenance();
        wdMaintenance.ismaintainwithdraw = pResponse.ismaintainwithdraw;
        wdMaintenance.maintainwithdrawfrom = pResponse.maintainwithdrawfrom;
        wdMaintenance.maintainwithdrawto = pResponse.maintainwithdrawto;
        SharedPreferencesManager.getInstance().setMaintenanceWithDraw(GsonUtils.toJsonString(wdMaintenance));
        Log.d(this, "saved pResponse.ismaintainwithdraw to cache");
        // need to update cache data if chechsum is changed.
        if (isUpdatePlatformInfoOnCache(pResponse.platforminfochecksum)) {
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            SharedPreferencesManager.getInstance().setPlatformInfoExpriedTime(expiredTime);
            SharedPreferencesManager.getInstance().setPlatformInfoExpriedTimeDuration(pResponse.expiredtime);

            SharedPreferencesManager.getInstance().setChecksumSDK(pResponse.platforminfochecksum);
            SharedPreferencesManager.getInstance().setCurrentUserID(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

            //banner list for merchant
            if (pResponse.bannerresources != null) {
                SharedPreferencesManager.getInstance().setBannerList(GsonUtils.toJsonString(pResponse.bannerresources));
            }
            if (pResponse.approvedinsideappids != null) {
                SharedPreferencesManager.getInstance().setApproveInsideApps(GsonUtils.toJsonString(pResponse.approvedinsideappids));
            }
        }
        //need to update card info again on cache
        if (MapCardHelper.needUpdateMapCardListOnCache(pResponse.cardinfochecksum)) {
            MapCardHelper.saveMapCardListToCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, pResponse.cardinfochecksum, pResponse.cardinfos);
        }
        //update bank account info on cache
        if (BankAccountHelper.needUpdateMapBankAccountListOnCache(pResponse.bankaccountchecksum)) {
            BankAccountHelper.saveMapBankAccountListToCache(pResponse.bankaccountchecksum, pResponse.bankaccounts);
        }

        if (mNoDownloadResource) {
            Log.d(this, "===refresh gateway from app, so no need to download resource file again===");
            return;
        }
        /***
         * need to download new resource if
         * 1.server return isupdateresource = true;
         * 2.resource version on cached client and resource version server return is different.This case user no need to update app.
         */
        String resrcVer = SharedPreferencesManager.getInstance().getResourceVersion();
        if (pResponse.resource != null && (pResponse.isupdateresource || (!TextUtils.isEmpty(resrcVer) && !pResponse.resource.rsversion.equals(resrcVer)))) {
            SharedPreferencesManager.getInstance().setResourceVersion(pResponse.resource.rsversion);
            SharedPreferencesManager.getInstance().setResourceDownloadUrl(pResponse.resource.rsurl);
            Log.d(this, "start download sdk resource " + pResponse.resource.rsurl);
            DownloadResourceTask downloadResourceTask = new DownloadResourceTask(pResponse.resource.rsurl, pResponse.resource.rsversion);
            downloadResourceTask.makeRequest();
        }
    }

    private void onProcessResponse(DPlatformInfo pResponse) {
        if (this.mGetGatewayInfoListener == null) {
            Log.d(this, "mGetGatewayInfoListener = NULL");
            return;
        }
        if (pResponse == null || pResponse.returncode < 0) {
            this.mGetGatewayInfoListener.onError(pResponse);
            return;
        }
        if (pResponse.forceappupdate) {
            pResponse.returncode = Constants.FORCE_UP_VERSION_CODE;
            this.mGetGatewayInfoListener.onUpVersion(true, pResponse.newestappversion, pResponse.forceupdatemessage);
            return;
        }
        //notify user  have a new version on store but not force user update
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        if (!TextUtils.isEmpty(appVersion) && !TextUtils.isEmpty(pResponse.newestappversion) && !pResponse.newestappversion.equalsIgnoreCase(appVersion)) {
            this.mGetGatewayInfoListener.onUpVersion(false, pResponse.newestappversion, pResponse.forceupdatemessage);
            return;
        }
        //everything is ok
        if (pResponse.returncode == 1) {
            this.mGetGatewayInfoListener.onSuccess();
        }
    }

    private boolean isUpdatePlatformInfoOnCache(String pPlatformInfoCheckSum) {
        try {
            String checksumOnCache = SharedPreferencesManager.getInstance().getChecksumSDK();
            if (!TextUtils.isEmpty(checksumOnCache) && !checksumOnCache.equals(pPlatformInfoCheckSum)) {
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return true;
    }

    @Override
    public void onDoTaskOnResponse(DPlatformInfo pResponse) {
        try {
            if (pResponse == null || pResponse.returncode != 1) {
                Log.d(this, "request not success...stopping saving response to cache");
                return;
            }
            //There are a new version on store,app need to show update version dialog
            if (pResponse.forceappupdate) {
                Log.d(this, "Response.forceappupdate=TRUE...stopping saving response to cache");
                return;
            }
            saveResponseToCache(pResponse);
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
        }
    }

    @Override
    public void onRequestSuccess(DPlatformInfo pResponse) {
        onProcessResponse(pResponse);
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mGetGatewayInfoListener != null) {
            DPlatformInfo platformResponse = new DPlatformInfo();
            platformResponse.returncode = -1;
            platformResponse.returnmessage = getDefaulErrorNetwork();
            mGetGatewayInfoListener.onError(platformResponse);
        } else {
            Log.d(this, "mLoadAppInfoListener = NULL");
        }
        Log.d(TAG, e);
    }

    @Override
    public void onRequestInProcess() {
        if (mGetGatewayInfoListener != null) {
            mGetGatewayInfoListener.onProcessing();
        }
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_loadplatforminfo);
    }

    @Override
    protected void doRequest() {
        try {
            newDataRepository().setTask(this).loadData(new LoadPlatformInfoImpl(), getDataParams());
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            Log.d(this, "===prepare param to get platform info from server===");
            String checksum = SharedPreferencesManager.getInstance().getChecksumSDK();
            String checksumSDKV = SharedPreferencesManager.getInstance().getChecksumSDKversion();
            String resrcVer = SharedPreferencesManager.getInstance().getResourceVersion();
            //is this new user ?
            boolean isNewUser = GlobalData.isNewUser();
            String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
            //mForceReload :: refresh gateway info from app
            if ((!TextUtils.isEmpty(appVersion) && !appVersion.equals(checksumSDKV)) || !BGatewayInfo.isValidConfig() || isNewUser || mForceReload) {
                checksum = null;   //server will see this is new install, so return new resource to download
                resrcVer = null;
                SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
                SharedPreferencesManager.getInstance().setBankAccountCheckSum(null);
                Log.d(this, "checksum =null resrVer=null..reset card check sum, bank checksum");
            }
            String cardInfoCheckSum = SharedPreferencesManager.getInstance().getCardInfoCheckSum();
            String bankAccountCheckSum = SharedPreferencesManager.getInstance().getBankAccountCheckSum();
            DataParameter.prepareGetPlatformInfoParams(checksum, resrcVer, cardInfoCheckSum, bankAccountCheckSum, getDataParams());
            return true;
        } catch (Exception ex) {
            Log.e(this, ex);
            onRequestFail(ex);
            return false;
        }
    }
}
