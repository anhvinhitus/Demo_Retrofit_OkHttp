package vn.com.zalopay.wallet.datasource.request;

import java.util.ArrayList;

import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DChannelMapApp;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.LoadAppInfoImpl;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get app info
 */
public class AppInfoTask extends BaseTask<DAppInfoResponse> {
    private static final String TAG = AppInfoTask.class.getCanonicalName();
    private ILoadAppInfoListener mLoadAppInfoListener;
    private String mAppId;
    private String mUserId;
    private String mAccessToken;

    /***
     * constructor
     * this is used in sdk
     * @param pListener
     */
    public AppInfoTask(ILoadAppInfoListener pListener, String pAppId, String pUserId, String pAccessToken) {
        super();
        mLoadAppInfoListener = pListener;
        mAppId = pAppId;
        mUserId = pUserId;
        mAccessToken = pAccessToken;
    }

    protected boolean isNeedToUpdateAppInfo(DAppInfoResponse pResponse) {
        return pResponse != null && pResponse.returncode == 1 && pResponse.isupdateappinfo;
    }

    @Override
    public void onDoTaskOnResponse(DAppInfoResponse pResponse) {
        try {
            Log.d(this, "onDoTaskOnResponse");
            if(pResponse == null || pResponse.returncode != 1)
            {
                Log.d(this,"request not success...stopping saving response to cache");
                return;
            }
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(mAppId, expiredTime);
            if (isNeedToUpdateAppInfo(pResponse)) {
                Log.d(this, "need to update cache app info");
                if (pResponse.transtypepmcs != null && pResponse.transtypepmcs.size() > 0) {
                    Log.d(this, "update trans type...");
                    long minValue, maxValue;
                    for (DChannelMapApp channelMap : pResponse.transtypepmcs) {
                        minValue = BaseChannelInjector.MIN_VALUE_CHANNEL;
                        maxValue = BaseChannelInjector.MAX_VALUE_CHANNEL;
                        ArrayList<String> mapAppChannelIDList = new ArrayList<String>();
                        String keyMap = mAppId + Constants.UNDERLINE + channelMap.transtype;
                        for (DPaymentChannel channel : channelMap.pmclist) {
                            String appChannelID = keyMap + Constants.UNDERLINE + channel.pmcid;
                            mapAppChannelIDList.add(String.valueOf(channel.pmcid));
                            //set 1 channel
                            SharedPreferencesManager.getInstance().setPmcConfig(appChannelID, GsonUtils.toJsonString(channel));
                            //get min,max of this channel to app use
                            if (channel.isEnable()) {
                                if (channel.minvalue < minValue)
                                {
                                    minValue = channel.minvalue;
                                }
                                if (channel.maxvalue > maxValue)
                                {
                                    maxValue = channel.maxvalue;
                                }
                            }
                            Log.d(this, "===set channel to cache===" + GsonUtils.toJsonString(channel));
                        }
                        //set ids channel list
                        SharedPreferencesManager.getInstance().setPmcConfigList(keyMap, mapAppChannelIDList);
                        Log.d(this, "===set ids channel list to cache===" + mapAppChannelIDList.toString());
                        //save min,max value for each channel.those values is used by app
                        if (String.valueOf(channelMap.transtype).equalsIgnoreCase(ETransactionType.WALLET_TRANSFER.toString()) ||
                                String.valueOf(channelMap.transtype).equalsIgnoreCase(ETransactionType.TOPUP.toString()) ||
                                String.valueOf(channelMap.transtype).equalsIgnoreCase(ETransactionType.WITHDRAW.toString())) {
                            Log.d(this, "===set min/max: transtype to cache===" + channelMap.transtype + " - " + minValue + " => " + maxValue);
                            if (minValue != BaseChannelInjector.MIN_VALUE_CHANNEL)
                                SharedPreferencesManager.getInstance().setMinValueChannel(String.valueOf(channelMap.transtype), minValue);

                            if (maxValue != BaseChannelInjector.MAX_VALUE_CHANNEL)
                                SharedPreferencesManager.getInstance().setMaxValueChannel(String.valueOf(channelMap.transtype), maxValue);
                        }
                    }
                }
                SharedPreferencesManager.getInstance().setCheckSumAppChannel(mAppId, pResponse.checksum);
                //save app info to cache(id,name,icon...)
                if (pResponse.info != null) {
                    SharedPreferencesManager.getInstance().setApp(String.valueOf(pResponse.info.appid), GsonUtils.toJsonString(pResponse.info));
                }
            }
        } catch (Exception ex) {
            Log.d(this, ex);
        }
    }

    @Override
    public void onRequestSuccess(DAppInfoResponse pResponse) {
        if (!(pResponse instanceof DAppInfoResponse)) {
            onRequestFail(null);
        }
        if (pResponse.returncode < 0 && mLoadAppInfoListener != null) {
            this.mLoadAppInfoListener.onError(pResponse);
        } else if (mLoadAppInfoListener != null) {
            this.mLoadAppInfoListener.onSuccess();
        } else {
            Log.e(this, "mLoadAppInfoListener = NULL");
        }
        Log.d(this, "onRequestSuccess");
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mLoadAppInfoListener != null) {
            DAppInfoResponse appInfoResponse = new DAppInfoResponse();
            appInfoResponse.returncode = -1;
            appInfoResponse.returnmessage = getDefaulErrorNetwork();
            mLoadAppInfoListener.onError(appInfoResponse);
        } else {
            Log.d(this, "mLoadAppInfoListener = NULL");
        }
        Log.d(TAG, e);
    }

    @Override
    public void onRequestInProcess() {
        if (mLoadAppInfoListener != null) {
            mLoadAppInfoListener.onProcessing();
        }
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_loadappinfo);
    }

    @Override
    protected void doRequest() {
        try {
            Log.d(this, "===Begin getting info of app ID:===" + mAppId);
            newDataRepository().setTask(this).loadData(new LoadAppInfoImpl(), getDataParams());
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        String checkSum = null;
        try {
            checkSum = SharedPreferencesManager.getInstance().getCheckSumAppChannel(mAppId);
        } catch (Exception e) {
            Log.e(this, e);
        }
        DataParameter.prepareGetAppInfoParams(mUserId, mAppId, mAccessToken, checkSum, getDataParams());
        return true;
    }
}
