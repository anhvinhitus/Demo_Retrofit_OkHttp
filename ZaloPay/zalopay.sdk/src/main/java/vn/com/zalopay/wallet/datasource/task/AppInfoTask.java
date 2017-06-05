package vn.com.zalopay.wallet.datasource.task;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransTypeResponse;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.LoadAppInfoImpl;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.utility.GsonUtils;

/***
 * get app info
 */
public class AppInfoTask extends BaseTask<AppInfoResponse> {
    private static final String TAG = AppInfoTask.class.getCanonicalName();
    private ILoadAppInfoListener mLoadAppInfoListener;
    private String mAppId;
    private String mUserId;
    private String mAccessToken;
    private int[] transtypes;

    public AppInfoTask(ILoadAppInfoListener pListener, String pAppId, String pUserId, String pAccessToken, int[] pTranstypes) {
        super();
        mLoadAppInfoListener = pListener;
        mAppId = pAppId;
        mUserId = pUserId;
        mAccessToken = pAccessToken;
        transtypes = pTranstypes;
    }

    protected boolean isNeedToUpdateAppInfo(AppInfoResponse pResponse) {
        return pResponse != null && pResponse.returncode == 1 && pResponse.isupdateappinfo;
    }

    @Override
    public void onDoTaskOnResponse(AppInfoResponse pResponse) {
        try {
            Log.d(this, "onDoTaskOnResponse");
            if (pResponse == null || pResponse.returncode != 1) {
                Log.d(this, "request not success...stopping saving response to cache");
                return;
            }
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(mAppId, expiredTime);
            if (pResponse.hasTranstypes()) {
                long minValue, maxValue;
                for (MiniPmcTransTypeResponse miniPmcTransTypeResponse : pResponse.pmctranstypes) {
                    int transtype = miniPmcTransTypeResponse.transtype;
                    List<MiniPmcTransType> miniPmcTransTypeList = miniPmcTransTypeResponse.transtypes;
                    minValue = BaseChannelInjector.MIN_VALUE_CHANNEL;
                    maxValue = BaseChannelInjector.MAX_VALUE_CHANNEL;
                    ArrayList<String> transtypePmcIdList = new ArrayList<>();

                    String appInfoTranstypeKey = getAppTranstypeKey(transtype);
                    for (MiniPmcTransType miniPmcTransType : miniPmcTransTypeList) {
                        String pmcKey = miniPmcTransType.getPmcKey(maxValue, transtype, miniPmcTransType.pmcid);
                        //save default for new atm/cc and bank account/zalopay pmc
                        if (!transtypePmcIdList.contains(pmcKey)) {
                            transtypePmcIdList.add(pmcKey);
                            MiniPmcTransType defaultPmcTranstype = new MiniPmcTransType(miniPmcTransType);
                            //reset to default value to atm pmc because it is up to bank
                            if (miniPmcTransType.isAtmChannel()) {
                                defaultPmcTranstype.resetToDefault();
                            }
                            SharedPreferencesManager.getInstance().setPmcConfig(pmcKey, GsonUtils.toJsonString(defaultPmcTranstype));//set 1 channel
                            Log.d(this, "save channel to cache key " + pmcKey, defaultPmcTranstype);
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
                    if (transtype == TransactionType.MONEY_TRANSFER
                            || transtype == TransactionType.TOPUP
                            || transtype == TransactionType.WITHDRAW) {
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

            if (pResponse.needUpdateAppInfo()) {
                //save app info to cache(id,name,icon...)
                SharedPreferencesManager.getInstance().setApp(String.valueOf(pResponse.info.appid), GsonUtils.toJsonString(pResponse.info));
                SharedPreferencesManager.getInstance().setCheckSumAppChannel(mAppId, pResponse.appinfochecksum);
                Log.d(this, "save app info to cache and update new checksum", pResponse.info);
            }
        } catch (Exception ex) {
            Log.d(this, ex);
        }
    }

    @Override
    public void onRequestSuccess(AppInfoResponse pResponse) {
        if (!(pResponse instanceof AppInfoResponse)) {
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
            AppInfoResponse appInfoResponse = new AppInfoResponse();
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
            Log.d(this, "start get app info " + mAppId);
            newDataRepository().setTask(this).loadData(new LoadAppInfoImpl(), getDataParams());
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        String appInfoCheckSum = null;
        String[] transtypeCheckSum = new String[0];
        try {
            appInfoCheckSum = SharedPreferencesManager.getInstance().getCheckSumAppChannel(mAppId);
            if (!TextUtils.isEmpty(appInfoCheckSum)) {
                transtypeCheckSum = new String[transtypes.length];
                for (int i = 0; i < transtypes.length; i++) {
                    transtypeCheckSum[i] = SharedPreferencesManager.getInstance().getTransypePmcCheckSum(getAppTranstypeKey(transtypes[i]));
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        DataParameter.prepareGetAppInfoParams(mUserId, mAppId, mAccessToken, appInfoCheckSum, transtypes, transtypeCheckSum, getDataParams());
        return true;
    }

    private String getAppTranstypeKey(int transtype) {
        StringBuilder appTransTypePmcKey = new StringBuilder();
        appTransTypePmcKey.append(mAppId)
                .append(Constants.UNDERLINE)
                .append(transtype);
        return appTransTypePmcKey.toString();
    }
}
