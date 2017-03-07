package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import java.util.ArrayList;

import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannelStatus;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DChannelMapApp;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetAppInfoImpl;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get app info class
 */
public class GetAppInfo extends BaseRequest<DAppInfoResponse> {
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
            try {
                appID = String.valueOf(GlobalData.getPaymentInfo().appID);
                zaloUserId = GlobalData.getPaymentInfo().userInfo.zaloPayUserId;
                accessToken = GlobalData.getPaymentInfo().userInfo.accessToken;
            } catch (Exception e) {
                Log.e(this, e);
                onRequestFail(null);
            }
        } else {
            onRequestFail(GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user));
        }

    }

    /***
     * ovreloading constructor
     * this's used by app
     *
     * @param pListener
     */
    public GetAppInfo(String pAppId, String pZaloUserId, String pAccessToken, ILoadAppInfoListener pListener) {
        super();

        mLoadAppInfoListener = pListener;
        appID = pAppId;
        zaloUserId = pZaloUserId;
        accessToken = pAccessToken;
    }

    private boolean isNeedToUpdateAppInfo(DAppInfoResponse pResponse) {
        return pResponse != null && pResponse.returncode == 1 && pResponse.isupdateappinfo;
    }

    private void onPostResult(DAppInfoResponse pResponse) {
        if (pResponse == null || pResponse.returncode < 0) {
            this.mLoadAppInfoListener.onError(pResponse);
        } else {
            this.mLoadAppInfoListener.onSuccess();
        }
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        if (!(getResponse() instanceof DAppInfoResponse)) {
            onRequestFail(null);
            return;
        }

        long expiredTime = getResponse().expiredtime + System.currentTimeMillis();

        SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(appID, expiredTime);

        if (isNeedToUpdateAppInfo(getResponse())) {

            SharedPreferencesManager.getInstance().setCheckSumAppChannel(appID, getResponse().checksum);

            if (getResponse().transtypepmcs == null || getResponse().transtypepmcs.size() <= 0) {
                getResponse().returncode = -1;
                getResponse().returnmessage = GlobalData.getStringResource(RS.string.zpw_app_info_exclude_channel);
            } else {
                //we have channels for this app
                //save them to cache

                long minValue, maxValue;

                for (DChannelMapApp channelMap : getResponse().transtypepmcs) {
                    minValue = BaseChannelInjector.MIN_VALUE_CHANNEL;
                    maxValue = BaseChannelInjector.MAX_VALUE_CHANNEL;

                    ArrayList<String> mapAppChannelIDList = new ArrayList<String>();

                    String keyMap = appID + Constants.UNDERLINE + channelMap.transtype;

                    //for testing
                    /*
                    DPaymentChannel channelVCB = new DPaymentChannel();
                    channelVCB.pmcid = 37;
                    channelVCB.pmcname = "Tài khoản ngân hàng";
                    channelVCB.status = EPaymentChannelStatus.ENABLE;
                    channelVCB.minvalue = 1000;
                    channelVCB.maxvalue = 20000000;
                    channelMap.pmclist.add(channelVCB);
                    */

                    for (DPaymentChannel channel : channelMap.pmclist) {
                        String appChannelID = keyMap + Constants.UNDERLINE + channel.pmcid;

                        mapAppChannelIDList.add(String.valueOf(channel.pmcid));

                        //set 1 channel
                        SharedPreferencesManager.getInstance().setPmcConfig(appChannelID, GsonUtils.toJsonString(channel));

                        //get min,max of this channel to app use
                        if (channel.isEnable()) {
                            if (channel.minvalue < minValue)
                                minValue = channel.minvalue;
                            if (channel.maxvalue > maxValue)
                                maxValue = channel.maxvalue;
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
        mResponse = new DAppInfoResponse();
    }

    @Override
    protected void onRequestInProcess() {
        if (mLoadAppInfoListener != null) {
            mLoadAppInfoListener.onProcessing();
        }
    }

    @Override
    protected void doRequest() {
        try {
            Log.d(this, "===Begin getting info of app ID:===" + appID);
            DataRepository.newInstance().setDataSourceListener(getDataSourceListener()).getData(new GetAppInfoImpl(), getDataParams());
        } catch (Exception e) {
            Log.e(this, e);

            onRequestFail(null);
        }
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
