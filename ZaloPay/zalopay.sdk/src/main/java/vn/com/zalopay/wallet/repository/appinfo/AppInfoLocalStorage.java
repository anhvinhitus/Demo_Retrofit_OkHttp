package vn.com.zalopay.wallet.repository.appinfo;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransTypeResponse;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class AppInfoLocalStorage extends AbstractLocalStorage implements AppInfoStore.LocalStorage {
    public AppInfoLocalStorage(SharedPreferencesManager sharedPreferences) {
        super(sharedPreferences);
    }

    @Override
    public void put(long pAppId, AppInfoResponse pResponse) {
        try {
            Log.d(this, "start save app info", pResponse);
            if (pResponse == null || pResponse.returncode != 1) {
                Log.d(this, "request not success...stopping saving response to cache");
                return;
            }
            long expiredTime = pResponse.expiredtime + System.currentTimeMillis();
            mSharedPreferences.setExpiredTimeAppChannel(String.valueOf(pAppId), expiredTime);
            if (pResponse.hasTranstypes()) {
                long minValue, maxValue;
                for (MiniPmcTransTypeResponse miniPmcTransTypeResponse : pResponse.pmctranstypes) {
                    int transtype = miniPmcTransTypeResponse.transtype;
                    List<MiniPmcTransType> miniPmcTransTypeList = miniPmcTransTypeResponse.transtypes;
                    minValue = BaseChannelInjector.MIN_VALUE_CHANNEL;
                    maxValue = BaseChannelInjector.MAX_VALUE_CHANNEL;
                    ArrayList<String> transtypePmcIdList = new ArrayList<>();

                    String appInfoTranstypeKey = getTranstypeCheckSumKey(pAppId, transtype);
                    for (MiniPmcTransType miniPmcTransType : miniPmcTransTypeList) {
                        String pmcKey = MiniPmcTransType.getPmcKey(pAppId, transtype, miniPmcTransType.pmcid);
                        //save default for new atm/cc and bank account/zalopay pmc
                        if (!transtypePmcIdList.contains(pmcKey)) {
                            transtypePmcIdList.add(pmcKey);
                            MiniPmcTransType defaultPmcTranstype = new MiniPmcTransType(miniPmcTransType);
                            //reset to default value to atm pmc because it is up to bank
                            if (miniPmcTransType.isAtmChannel()) {
                                defaultPmcTranstype.resetToDefault();
                            }
                            mSharedPreferences.setPmcConfig(pmcKey, GsonUtils.toJsonString(defaultPmcTranstype));//set 1 channel
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
                        mSharedPreferences.setPmcConfig(pmcId.toString(), GsonUtils.toJsonString(miniPmcTransType));//set 1 channel
                        Log.d(this, "save channel to cache key " + pmcId.toString(), miniPmcTransType);
                    }
                    mSharedPreferences.setPmcConfigList(appInfoTranstypeKey, transtypePmcIdList);//set ids channel list
                    mSharedPreferences.setTranstypePmcCheckSum(appInfoTranstypeKey, miniPmcTransTypeResponse.checksum); //set transtype checksum
                    Log.d(this, "save ids channel list to cache " + transtypePmcIdList.toString());
                    //save min,max value for each channel.those values is used when user input amount
                    if (transtype == TransactionType.MONEY_TRANSFER || transtype == TransactionType.TOPUP || transtype == TransactionType.WITHDRAW) {
                        if (minValue != BaseChannelInjector.MIN_VALUE_CHANNEL) {
                            mSharedPreferences.setMinValueChannel(String.valueOf(transtype), minValue);
                            Log.d(this, "save min value " + minValue + " transtype " + transtype);
                        }
                        if (maxValue != BaseChannelInjector.MAX_VALUE_CHANNEL) {
                            mSharedPreferences.setMaxValueChannel(String.valueOf(transtype), maxValue);
                            Log.d(this, "save max value " + maxValue + " transtype " + transtype);
                        }
                    }
                }
            }

            if (pResponse.needUpdateAppInfo()) {
                //save app info to cache(id,name,icon...)
                mSharedPreferences.setApp(String.valueOf(pResponse.info.appid), GsonUtils.toJsonString(pResponse.info));
                mSharedPreferences.setCheckSumAppChannel(String.valueOf(pAppId), pResponse.appinfochecksum);
                Log.d(this, "save app info to cache and update new checksum", pResponse.info);
            }
        } catch (Exception ex) {
            Log.d(this, ex);
        }
    }

    @Override
    public Observable<AppInfo> get(long appId) {
        return Observable.defer(() -> {
            try {
                AppInfo appInfo = GsonUtils.fromJsonString(mSharedPreferences.getAppById(String.valueOf(appId)), AppInfo.class);
                if (appInfo != null) {
                    appInfo.expriretime = getExpireTime(appId);
                    Log.d(this, "load app info from cache", appInfo);
                    return Observable.just(appInfo);
                } else {
                    return Observable.just(null);
                }
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    public long getExpireTime(long appId) {
        try {
            return mSharedPreferences.getExpiredTimeAppChannel(String.valueOf(appId));
        } catch (Exception e) {
            Log.e(this, e);
        }
        return 0;
    }

    @Override
    public String getAppInfoCheckSum(long appId) {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getCheckSumAppChannel(String.valueOf(appId));
        } catch (Exception e) {
            Log.e(this, e);
        }
        return !TextUtils.isEmpty(checksum) ? checksum : "";
    }

    @Override
    public String getTranstypeCheckSum(String key) {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getTransypePmcCheckSum(key);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return !TextUtils.isEmpty(checksum) ? checksum : "";
    }

    @Override
    public void setExpireTime(long appId, long expireTime) {
        mSharedPreferences.setExpiredTimeAppChannel(String.valueOf(appId), expireTime);
    }

    @Override
    public String getTranstypeCheckSumKey(long pAppId, int transtype) {
        StringBuilder appTransTypePmcKey = new StringBuilder();
        appTransTypePmcKey.append(pAppId)
                .append(Constants.UNDERLINE)
                .append(transtype);
        return appTransTypePmcKey.toString();
    }
}
