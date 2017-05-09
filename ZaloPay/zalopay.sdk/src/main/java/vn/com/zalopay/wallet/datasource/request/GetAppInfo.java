package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
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
    private String appID;
    private String zaloUserId;
    private String accessToken;

    /***
     * constructor
     * this is used in sdk
     *
     * @param pListener
     */
    public GetAppInfo(ILoadAppInfoListener pListener) {
        super();
        mLoadAppInfoListener = pListener;
        if (GlobalData.getPaymentInfo() != null && GlobalData.getPaymentInfo().userInfo != null && GlobalData.getPaymentInfo().userInfo.isUserInfoValid()) {
            appID = String.valueOf(GlobalData.getPaymentInfo().appID);
            zaloUserId = GlobalData.getPaymentInfo().userInfo.zaloPayUserId;
            accessToken = GlobalData.getPaymentInfo().userInfo.accessToken;
        } else {
            onRequestFail(GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user));
        }

    }

    /***
     * ovreloading constructor
     * this's used by app
     * @param pListener
     */
    public GetAppInfo(String pAppId, String pZaloUserId, String pAccessToken, ILoadAppInfoListener pListener) {
        super();
        mLoadAppInfoListener = pListener;
        appID = pAppId;
        zaloUserId = pZaloUserId;
        accessToken = pAccessToken;
    }

    private boolean isNeedToUpdateAppInfo(AppInfoResponse pResponse) {
        return pResponse != null && pResponse.returncode == 1 && pResponse.isupdateappinfo;
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
        SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(appID, expiredTime);
        if (isNeedToUpdateAppInfo(getResponse())) {
            SharedPreferencesManager.getInstance().setCheckSumAppChannel(appID, getResponse().checksum);
            if (getResponse().pmctranstypes == null || getResponse().pmctranstypes.size() <= 0) {
                getResponse().returncode = -1;
                getResponse().returnmessage = GlobalData.getStringResource(RS.string.zpw_app_info_exclude_channel);
            } else {
                long minValue, maxValue;
                int checkPmc = -1;
                for (int transtype : getResponse().pmctranstypes.keySet()) {
                    List<MiniPmcTransType> miniPmcTransTypeList = getResponse().pmctranstypes.get(transtype);
                    minValue = BaseChannelInjector.MIN_VALUE_CHANNEL;
                    maxValue = BaseChannelInjector.MAX_VALUE_CHANNEL;
                    ArrayList<String> transtypePmcIdList = new ArrayList<>();

                    StringBuilder transtypePmcKey = new StringBuilder();
                    transtypePmcKey.append(appID)
                            .append(Constants.UNDERLINE)
                            .append(transtype);
                    for (MiniPmcTransType miniPmcTransType : miniPmcTransTypeList) {
                        String pmcKey = miniPmcTransType.getPmcKey(miniPmcTransType.pmcid);
                        //save default pmc for new atm/cc
                        if (checkPmc != miniPmcTransType.pmcid) {
                            transtypePmcIdList.add(pmcKey);
                            SharedPreferencesManager.getInstance().setPmcConfig(pmcKey, GsonUtils.toJsonString(miniPmcTransType));//set 1 channel
                            Log.d(this, "save channel to cache key " + pmcKey + " " + GsonUtils.toJsonString(miniPmcTransType));
                        }
                        checkPmc = miniPmcTransType.pmcid;

                        StringBuilder pmcId = new StringBuilder();
                        pmcId.append(pmcKey).append(Constants.UNDERLINE).append(miniPmcTransType.bankcode);
                        SharedPreferencesManager.getInstance().setPmcConfig(pmcId.toString(), GsonUtils.toJsonString(miniPmcTransType));//set 1 channel
                        Log.d(this, "save channel to cache key " + pmcId.toString() + " " + GsonUtils.toJsonString(miniPmcTransType));
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
                    }
                    //set ids channel list
                    SharedPreferencesManager.getInstance().setPmcConfigList(transtypePmcKey.toString(), transtypePmcIdList);
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
            //save app info to cache(id,name,icon...)
            if (getResponse().info != null) {
                SharedPreferencesManager.getInstance().setApp(String.valueOf(getResponse().info.appid), GsonUtils.toJsonString(getResponse().info));
            }
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
        String checkSum = null;
        try {
            checkSum = SharedPreferencesManager.getInstance().getCheckSumAppChannel(appID);
        } catch (Exception e) {
            Log.e(this, e);
        }
        DataParameter.prepareGetAppInfoParams(zaloUserId, appID, accessToken, checkSum, getDataParams());
        return true;
    }
}
