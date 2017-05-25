package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannelStatus;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransTypeResponse;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetAppInfoImpl;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get app info class
 */
public class GetAppInfo extends BaseRequest<AppInfoResponse> {
    private ILoadAppInfoListener mLoadAppInfoListener;
    private long appID;
    private String zaloUserId;
    private String accessToken;
    private int[] transtypes;

    /***
     * constructor
     * this is used in sdk
     *
     * @param pListener
     */
    public GetAppInfo(int[] pTranstypes, ILoadAppInfoListener pListener) {
        super();
        mLoadAppInfoListener = pListener;
        if (GlobalData.getPaymentInfo() != null && GlobalData.getPaymentInfo().userInfo != null && GlobalData.getPaymentInfo().userInfo.isUserInfoValid()) {
            appID = GlobalData.getPaymentInfo().appID;
            zaloUserId = GlobalData.getPaymentInfo().userInfo.zaloPayUserId;
            accessToken = GlobalData.getPaymentInfo().userInfo.accessToken;
            transtypes = pTranstypes;
        } else {
            onRequestFail(GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user));
        }

    }

    /***
     * ovreloading constructor
     * this's used by app
     * @param pListener
     */
    public GetAppInfo(int[] pTranstypes, long pAppId, String pZaloUserId, String pAccessToken, ILoadAppInfoListener pListener) {
        super();
        mLoadAppInfoListener = pListener;
        appID = pAppId;
        zaloUserId = pZaloUserId;
        accessToken = pAccessToken;
        transtypes = pTranstypes;
    }

    private void onPostResult(AppInfoResponse pResponse) {
        if (pResponse == null || pResponse.returncode < 0) {
            this.mLoadAppInfoListener.onError(pResponse);
        } else {
            this.mLoadAppInfoListener.onSuccess();
        }
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        if (!(getResponse() instanceof AppInfoResponse)) {
            onRequestFail(null);
            return;
        }
        long expiredTime = getResponse().expiredtime + System.currentTimeMillis();
        SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(String.valueOf(appID), expiredTime);
        if (getResponse().hasTranstypes()) {
            long minValue, maxValue;
            for (MiniPmcTransTypeResponse miniPmcTransTypeResponse : getResponse().pmctranstypes) {
                int transtype = miniPmcTransTypeResponse.transtype;
                if (!ETransactionType.isMember(transtype)) {
                    Log.d(this, "skip transype " + transtype);
                    continue;
                }
                List<MiniPmcTransType> miniPmcTransTypeList = miniPmcTransTypeResponse.transtypes;
                minValue = BaseChannelInjector.MIN_VALUE_CHANNEL;
                maxValue = BaseChannelInjector.MAX_VALUE_CHANNEL;
                ArrayList<String> transtypePmcIdList = new ArrayList<>();

                String appInfoTranstypeKey = getAppTranstypeKey(transtype);
                for (MiniPmcTransType miniPmcTransType : miniPmcTransTypeList) {
                    String pmcKey = miniPmcTransType.getPmcKey(appID, String.valueOf(transtype), miniPmcTransType.pmcid);
                    //save default for new atm/cc and bank account/zalopay pmc
                    if (!transtypePmcIdList.contains(pmcKey)) {
                        transtypePmcIdList.add(pmcKey);
                        //reset to default value to atm pmc because it is up to bank
                        if (miniPmcTransType.isAtmChannel()) {
                            miniPmcTransType.status = EPaymentChannelStatus.ENABLE;
                            miniPmcTransType.minvalue = -1;
                            miniPmcTransType.maxvalue = -1;
                            miniPmcTransType.feerate = 0;
                            miniPmcTransType.minfee = 0;
                        }
                        SharedPreferencesManager.getInstance().setPmcConfig(pmcKey, GsonUtils.toJsonString(miniPmcTransType));//set 1 channel
                        Log.d(this, "save channel to cache key " + pmcKey, miniPmcTransType);
                    }
                    if (!miniPmcTransType.isEnable()) {
                        continue;
                    }
                    //get min,max of this channel to app use
                    if (miniPmcTransType.minvalue < minValue) {
                        minValue = miniPmcTransType.minvalue;
                    }
                    if (miniPmcTransType.maxvalue > maxValue) {
                        maxValue = miniPmcTransType.maxvalue;
                    }
                    if (TextUtils.isEmpty(miniPmcTransType.bankcode)) {
                        continue;
                    }
                    StringBuilder pmcId = new StringBuilder();
                    pmcId.append(pmcKey).append(Constants.UNDERLINE).append(miniPmcTransType.bankcode);
                    SharedPreferencesManager.getInstance().setPmcConfig(pmcId.toString(), GsonUtils.toJsonString(miniPmcTransType));//set 1 channel
                    Log.d(this, "save channel to cache key " + pmcId.toString(), miniPmcTransType);
                }
                SharedPreferencesManager.getInstance().setPmcConfigList(appInfoTranstypeKey, transtypePmcIdList);//set ids channel list
                SharedPreferencesManager.getInstance().setTranstypePmcCheckSum(appInfoTranstypeKey, miniPmcTransTypeResponse.checksum); //set transtype checksum
                Log.d(this, "save ids channel list to cache " + transtypePmcIdList.toString());
                //save min,max value for each channel.those values is used by app
                if (String.valueOf(transtype).equalsIgnoreCase(ETransactionType.WALLET_TRANSFER.toString())
                        || String.valueOf(transtype).equalsIgnoreCase(ETransactionType.TOPUP.toString())
                        || String.valueOf(transtype).equalsIgnoreCase(ETransactionType.WITHDRAW.toString())) {
                    if (minValue != BaseChannelInjector.MIN_VALUE_CHANNEL) {
                        SharedPreferencesManager.getInstance().setMinValueChannel(String.valueOf(transtype), minValue);
                        Log.d(this, "save min value " + minValue + " transtype " + transtype);
                    }
                    if (maxValue != BaseChannelInjector.MAX_VALUE_CHANNEL) {
                        SharedPreferencesManager.getInstance().setMaxValueChannel(String.valueOf(transtype), maxValue);
                        Log.d(this, "save max value " + maxValue + " transtype " + transtype);
                    }
                }
            }
        }

        if (getResponse().needUpdateAppInfo()) {
            //save app info to cache(id,name,icon...)
            SharedPreferencesManager.getInstance().setApp(String.valueOf(getResponse().info.appid), GsonUtils.toJsonString(getResponse().info));
            SharedPreferencesManager.getInstance().setCheckSumAppChannel(String.valueOf(appID), getResponse().appinfochecksum);
            Log.d(this, "save app info to cache and update new checksum", getResponse().info);
        }
        onPostResult(getResponse());
    }

    @Override
    protected void onRequestFail(String pMessage) {
        if (mLoadAppInfoListener != null) {
            String mess = !TextUtils.isEmpty(pMessage) ? pMessage : GlobalData.getStringResource(RS.string.zpw_alert_network_error_getappinfo);
            if (getResponse() == null) {
                createReponse(-1, mess);
            }
            getResponse().returncode = -1;
            getResponse().returnmessage = mess;
            mLoadAppInfoListener.onError(getResponse());
        }
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new AppInfoResponse();
    }

    @Override
    protected void onRequestInProcess() {
        if (mLoadAppInfoListener != null) {
            mLoadAppInfoListener.onProcessing();
        }
    }

    @Override
    protected void doRequest() {
        Log.d(this, "start get info of app ID " + appID);
        DataRepository.newInstance().setDataSourceListener(getDataSourceListener()).getData(new GetAppInfoImpl(), getDataParams());
    }

    @Override
    protected boolean doParams() {
        String appInfoCheckSum = null;
        String[] transtypeCheckSum = new String[0];
        try {
            appInfoCheckSum = SharedPreferencesManager.getInstance().getCheckSumAppChannel(String.valueOf(appID));
            if (!TextUtils.isEmpty(appInfoCheckSum)) {
                transtypeCheckSum = new String[transtypes.length];
                for (int i = 0; i < transtypes.length; i++) {
                    transtypeCheckSum[i] = SharedPreferencesManager.getInstance().getTransypePmcCheckSum(getAppTranstypeKey(transtypes[i]));
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        DataParameter.prepareGetAppInfoParams(zaloUserId, String.valueOf(appID), accessToken, appInfoCheckSum, transtypes, transtypeCheckSum, getDataParams());
        return true;
    }

    private String getAppTranstypeKey(int transtype) {
        StringBuilder appTransTypePmcKey = new StringBuilder();
        appTransTypePmcKey.append(appID)
                .append(Constants.UNDERLINE)
                .append(transtype);
        return appTransTypePmcKey.toString();
    }
}
