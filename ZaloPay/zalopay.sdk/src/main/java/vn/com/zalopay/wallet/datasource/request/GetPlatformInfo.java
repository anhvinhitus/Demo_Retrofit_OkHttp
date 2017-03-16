package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DChannelMapApp;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.PaymentSemaphore;
import vn.com.zalopay.wallet.datasource.RequestKeeper;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.listener.ZPWDownloadResourceListener;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;
import vn.com.zalopay.wallet.service.PlatformInfoRetryService;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StorageUtil;
import vn.com.zalopay.wallet.utils.ZPWUtils;

/***
 * class to call platform api and update platform's data on cache
 */
public class GetPlatformInfo extends BaseRequest<DPlatformInfo> {
    private static GetPlatformInfo _object;
    //semaphore to sync write data to cache, init queue permit with 1 item
    protected static PaymentSemaphore mPaymentSemaphore = new PaymentSemaphore();
    private ZPWGetGatewayInfoListener mCallBack;
    private boolean mIsProcessing = false;
    //force sdk re download everything.
    private boolean mForceReload;
    //app reoad platfrom info, this will be set to true to prevent download resource file again.
    private boolean mNoDownloadResource;
    //return callback as soon as possible to app
    private boolean mCallBackAsSoonAsPossible;
    protected void initialize()
    {
        mPaymentSemaphore.setPoolSize(1);
    }
    private ZPWDownloadResourceListener mDownloadResourceListener = new ZPWDownloadResourceListener() {
        @Override
        public void onLoadResourceComplete(boolean isSuccess) {
            if (!isSuccess) {
                mResponse = null;
            }

            if (!mCallBackAsSoonAsPossible)
                onPostResultCallBack();
        }
    };

    /***
     * over constructor
     *
     * @param pListener
     * @param pForceReload
     */
    public GetPlatformInfo(ZPWGetGatewayInfoListener pListener, boolean pForceReload) {
        super();
        this.mCallBack = pListener;
        this.mForceReload = pForceReload;
        this.mNoDownloadResource = false;
        this.mCallBackAsSoonAsPossible = false;
        initialize();
    }

    /***
     * @param pListener
     * @param pForceReload
     * @param pNoDownloadResource
     */
    public GetPlatformInfo(ZPWGetGatewayInfoListener pListener, boolean pForceReload, boolean pNoDownloadResource) {
        super();
        this.mCallBack = pListener;
        this.mForceReload = pForceReload;
        this.mNoDownloadResource = pNoDownloadResource;
        this.mCallBackAsSoonAsPossible = false;
        initialize();
    }

    /***
     * constructor
     *
     * @param pListener
     */
    public GetPlatformInfo(ZPWGetGatewayInfoListener pListener, boolean pForceReload, boolean pNoDownloadResource, boolean pCallBackAsSoonAsPossible) {
        super();
        this.mCallBack = pListener;
        this.mForceReload = pForceReload;
        this.mNoDownloadResource = pNoDownloadResource;
        this.mCallBackAsSoonAsPossible = pCallBackAsSoonAsPossible;
        initialize();
    }
    //endregion

    /***
     * is retry case from server.this variable is true
     * then need to reuse callable request in RequestKeeper.
     *
     * @param pListener
     * @param pIsForceReload
     * @return
     */
    public synchronized static GetPlatformInfo getInstance(ZPWGetGatewayInfoListener pListener, boolean pIsForceReload) {
        if (GetPlatformInfo._object == null)
            GetPlatformInfo._object = new GetPlatformInfo(pListener, pIsForceReload);

        return GetPlatformInfo._object;
    }

    public boolean isProcessing() {
        return this.mIsProcessing;
    }

    /***
     * process result from api
     *
     * @throws Exception
     */
    private synchronized void processGatewayInfoResult(DPlatformInfo pResponse) throws Exception {
        Log.d(this, "===processGatewayInfoResult()===");
        mPaymentSemaphore.acquire();
        Log.d(this, "got a permit from semaphore");
        Log.d(this, "available permit in semaphore " + mPaymentSemaphore.getAvailablePermits());
        //request fail
        if (pResponse == null || pResponse.returncode != 1) {
            onRequestFail(pResponse != null ? pResponse.returnmessage : null);
            return;
        }

        //There are a new version on store,app need to show update version dialog
        if (pResponse.forceappupdate) {
            pResponse.returncode = Constants.FORCE_UP_VERSION_CODE;

            onPostResultCallBack();

            Log.d(this, "===mPlatformInfoResult.forceappupdate=true===");

            return;
        }

        //notify user  have a new version on store but not force user setup
        String appVersion = ZPWUtils.getAppVersion(GlobalData.getAppContext());
        if (!TextUtils.isEmpty(appVersion) && !TextUtils.isEmpty(pResponse.newestappversion) && !pResponse.newestappversion.equalsIgnoreCase(appVersion)) {
            pResponse.returncode = Constants.UP_VERSION_CODE;
        }

        //enable/disable deposite
        SharedPreferencesManager.getInstance().setEnableDeposite(pResponse.isenabledeposit);

        //set maintenance withdraw
        WDMaintenance wdMaintenance = new WDMaintenance();
        wdMaintenance.ismaintainwithdraw = pResponse.ismaintainwithdraw;
        wdMaintenance.maintainwithdrawfrom = pResponse.maintainwithdrawfrom;
        wdMaintenance.maintainwithdrawto = pResponse.maintainwithdrawto;
        SharedPreferencesManager.getInstance().setMaintenanceWithDraw(GsonUtils.toJsonString(wdMaintenance));

        // need to update cache data if chechsum is changed.
        if (isNeedUpdatePlatformInfoInCache(pResponse.platforminfochecksum)) {
            Log.d(this, "===preparing update platform info on cache===");

            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();

            SharedPreferencesManager.getInstance().setPlatformInfoExpriedTime(expiredTime);
            SharedPreferencesManager.getInstance().setPlatformInfoExpriedTimeDuration(pResponse.expiredtime);

            // Checksum string
            SharedPreferencesManager.getInstance().setChecksumSDK(pResponse.platforminfochecksum);
            SharedPreferencesManager.getInstance().setCurrentUserID(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

            //banner list for merchant
            if (pResponse.bannerresources != null) {
                SharedPreferencesManager.getInstance().setBannerList(GsonUtils.toJsonString(pResponse.bannerresources));
            }

            if (pResponse.approvedinsideappids != null) {
                SharedPreferencesManager.getInstance().setApproveInsideApps(GsonUtils.toJsonString(pResponse.approvedinsideappids));
            }

            //app info zalopay
            if (pResponse.info != null) {
                SharedPreferencesManager.getInstance().setApp(String.valueOf(pResponse.info.appid), GsonUtils.toJsonString(pResponse.info));
            }

            //zalopay channel transtype
            if (pResponse.transtypepmcs != null && pResponse.transtypepmcs.size() > 0) {
                for (DChannelMapApp channelMap : pResponse.transtypepmcs) {
                    ArrayList<String> mapAppChannelIDList = new ArrayList<String>();

                    String keyMap = String.valueOf(pResponse.info != null ? pResponse.info.appid : GlobalData.appID);
                    keyMap += Constants.UNDERLINE + channelMap.transtype;

                    for (DPaymentChannel channel : channelMap.pmclist) {
                        String appChannelID = keyMap + Constants.UNDERLINE + channel.pmcid;

                        mapAppChannelIDList.add(String.valueOf(channel.pmcid));

                        SharedPreferencesManager.getInstance().setPmcConfig(appChannelID, GsonUtils.toJsonString(channel));
                    }

                    SharedPreferencesManager.getInstance().setPmcConfigList(keyMap, mapAppChannelIDList);
                }
            }
        }

        //need to update card info again on cache
        if (MapCardHelper.isNeedUpdateMapCardInfoOnCache(pResponse.cardinfochecksum)) {

            //for testing
            /*
            DMappedCard mappedCard = new DMappedCard();
            mappedCard.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_bidv);
            mappedCard.first6cardno = "970418";
            mappedCard.last4cardno  = "1017";
            pResponse.cardinfos.add(mappedCard);
            */
            MapCardHelper.updateMapCardInfoListOnCache(pResponse.cardinfochecksum, pResponse.cardinfos);
            try {
                MapCardHelper.updateMapCardInfoListOnCache(pResponse.cardinfochecksum, pResponse.cardinfos);
            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
        //update bank account info on cache
        if (BankAccountHelper.isNeedUpdateBankAccountInfoOnCache(pResponse.bankaccountchecksum)) {
            //for testing
            /*
            DBankAccount dBankAccount = new DBankAccount();
			dBankAccount.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank);
			dBankAccount.firstaccountno = "093490";
			dBankAccount.lastaccountno = "9460";
			pResponse.bankaccounts.add(dBankAccount);
			*/
            BankAccountHelper.updateBankAccountListOnCache(pResponse.bankaccountchecksum, pResponse.bankaccounts);
        }

        if (mNoDownloadResource) {
            onPostResultCallBack();

            Log.d(this, "===refresh gateway from app, so no need to download resource file again===");

            return;
        }

        /***
         * need to download new resource if
         * 1.server return isupdateresource = true;
         * 2.resource version on cached client and resource version server return is different.This
         * case user no need to update app.
         */
        String resrcVer = SharedPreferencesManager.getInstance().getResourceVersion();

        if (!mNoDownloadResource && pResponse.resource != null && (pResponse.isupdateresource || (!TextUtils.isEmpty(resrcVer)
                && !pResponse.resource.rsversion.equals(resrcVer)))) {
            /***
             * callback to app as soon as possible,no need to wait for downloading resource file.
             */
            if (mCallBackAsSoonAsPossible) {
                onPostResultCallBack();
            }

            SharedPreferencesManager.getInstance().setResourceVersion(pResponse.resource.rsversion);
            SharedPreferencesManager.getInstance().setResourceDownloadUrl(pResponse.resource.rsurl);

            String unzipFolder = StorageUtil.prepareUnzipFolder();

            // Start downloading
            DownloadBundle downloadResourceTask = new DownloadBundle(mDownloadResourceListener, pResponse.resource.rsurl, unzipFolder, pResponse.resource.rsversion);
            downloadResourceTask.execute();
        } else {
            onPostResultCallBack();
        }
    }

    private void onPostResultCallBack() {
        DataRepository.dispose();
        mIsProcessing = false;
        Log.d(this, "release a permit to semaphore");
        mPaymentSemaphore.release();
        if (this.mCallBack == null) {
            Log.d(this, "mCallBack = null");
            return;
        }
        //success
        if (getResponse() != null && getResponse().returncode >= 1) {
            //stop service retry if it still is running
            PlatformInfoRetryService.stop(GlobalData.getAppContext());
            this.mCallBack.onSuccess();
        }
        //force update version
        else if (getResponse() != null && getResponse().returncode == Constants.FORCE_UP_VERSION_CODE) {
            this.mCallBack.onUpVersion(true, getResponse().newestappversion, getResponse().forceupdatemessage);
        }
        //notify to app without need to update new version
        else if (getResponse() != null && getResponse().returncode == Constants.UP_VERSION_CODE) {
            this.mCallBack.onUpVersion(false, getResponse().newestappversion, getResponse().forceupdatemessage);
        }
        //fail
        else if (getResponse() == null || getResponse().returncode < 1) {
            //get errror message if download resource happened error
            if (!TextUtils.isEmpty(DownloadBundle.errorMessage)) {
                if (getResponse() == null) {
                    createReponse(-1, DownloadBundle.errorMessage);
                }
                getResponse().returncode = -1;
                getResponse().returnmessage = DownloadBundle.errorMessage;
            }
            /***
             * the first time run app, checksumsdk is null
             * need to start service retry load gateway info in the first time run app
             */
            try {
                if (TextUtils.isEmpty(SharedPreferencesManager.getInstance().getChecksumSDK())) {
                    PlatformInfoRetryService.start(GlobalData.getAppContext());

                    return;
                }
            } catch (Exception e) {
                Log.d(this, e);
            }

            this.mCallBack.onError(getResponse());
        } else {
            this.mCallBack.onError(getResponse());
        }
    }

    private boolean isNeedUpdatePlatformInfoInCache(String pPlatformInfoCheckSum) {
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
    protected void onRequestSuccess() throws Exception {
        try {
            DPlatformInfo platformInfo = null;
            if (getResponse() instanceof DPlatformInfo) {
                platformInfo = getResponse();
            }

            if (platformInfo == null) {
                onRequestFail(null);
                return;
            }
            processGatewayInfoResult(platformInfo);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(null);
        }
    }

    @Override
    protected void onRequestFail(String pMessage) {
        if (!TextUtils.isEmpty(pMessage)) {
            if (getResponse() == null) {
                createReponse(-1, pMessage);
            }
            getResponse().returncode = -1;
            getResponse().returnmessage = pMessage;
        }

        onPostResultCallBack();
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new DPlatformInfo();
    }

    @Override
    protected void doRequest() {
        try {
            DataRepository.newInstance().setDataSourceListener(getDataSourceListener()).getPlatformInfo(getDataParams());
        } catch (Exception e) {
            Log.e(this, e);
            mResponse = null;
            onRequestFail(null);
        }
    }

    /***
     * retry platforminfo from service or in GatewayLoader
     */
    public void makeRetry() {
        mIsProcessing = true;
        try {
            Log.d(this, "===starting to retry platform info=====");
            DataRepository.newInstance().setDataSourceListener(getDataSourceListener()).retryPlatformInfo(RequestKeeper.requestPlatformInfo);
        } catch (Exception e) {
            Log.e(this, e);

            mResponse = null;
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            mIsProcessing = true;

            Log.d(this, "===starting to get platform info=====");

            String checksum = SharedPreferencesManager.getInstance().getChecksumSDK();
            String checksumSDKV = SharedPreferencesManager.getInstance().getChecksumSDKversion();
            String resrcVer = SharedPreferencesManager.getInstance().getResourceVersion();

            //is this new user ?
            boolean isNewUser = GlobalData.isNewUser();

            String appVersion = ZPWUtils.getAppVersion(GlobalData.getAppContext());

            //mForceReload :: refresh gateway info from app
            if ((!TextUtils.isEmpty(appVersion) && !appVersion.equals(checksumSDKV)) || !BGatewayInfo.isValidConfig() || isNewUser || mForceReload) {
                //server will see this is new install, so return new resource to download
                checksum = null;
                resrcVer = null;
                SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
                SharedPreferencesManager.getInstance().setBankAccountCheckSum(null);
                Log.d(this, "checksum =null;resrVer=null..reset card check sum, bank checksum");
            }

            try {
                String cardInfoCheckSum = SharedPreferencesManager.getInstance().getCardInfoCheckSum();
                String bankAccountCheckSum = SharedPreferencesManager.getInstance().getBankAccountCheckSum();
                DataParameter.prepareGetPlatformInfoParams(checksum, resrcVer, cardInfoCheckSum, bankAccountCheckSum, getDataParams());
            } catch (Exception e) {
                Log.e(this, e);
                mResponse = null;
                onPostResultCallBack();
            }
        } catch (Exception ex) {
            Log.e(this, ex);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }
}
