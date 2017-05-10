package vn.com.zalopay.wallet.business.behavior.gateway;

import java.util.ArrayList;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.datasource.task.AppInfoTask;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;

/***
 * get app info
 */
public class AppInfoLoader extends SingletonBase {
    private static final String TAG = AppInfoLoader.class.getCanonicalName();
    public static DAppInfo appInfo;//payment app info
    private ILoadAppInfoListener mLoadAppInfoListener;
    private long appId;
    private String zaloUserId;
    private String accessToken;
    @TransactionType
    private int transactionType;

    public AppInfoLoader(long pAppId, @TransactionType int pTransType, String pZaloUserId, String pAccessToken) {
        super();
        this.appId = pAppId;
        this.transactionType = pTransType;
        this.zaloUserId = pZaloUserId;
        this.accessToken = pAccessToken;
    }

    public static synchronized AppInfoLoader get(long pAppId, @TransactionType int pTransType, String pZaloUserId, String pAccessToken) {
        return new AppInfoLoader(pAppId, pTransType, pZaloUserId, pAccessToken);
    }

    /***
     * get channels for this app on cache
     * @param pAppID
     * @param pTransType
     * @return
     */
    public static ArrayList<String> getChannelsForAppFromCache(String pAppID, @TransactionType int pTransType) throws Exception {
        String sKey = pAppID + Constants.UNDERLINE + pTransType;
        return SharedPreferencesManager.getInstance().getPmcConfigList(sKey);
    }

    public static DAppInfo getAppInfo() {
        if (appInfo == null) {
            try {
                appInfo = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(GlobalData.appID)), DAppInfo.class);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
        return appInfo;
    }

    /***
     * set callback to get result from app info api
     * @param pLoadAppInfoListener
     * @return
     */
    public AppInfoLoader setOnLoadAppInfoListener(ILoadAppInfoListener pLoadAppInfoListener) {
        this.mLoadAppInfoListener = pLoadAppInfoListener;
        return this;
    }

    /***
     * check expired time on cache
     * @return
     */
    private boolean isExpiredTime() {
        try {
            long currentTime = System.currentTimeMillis();
            long expiredTime = SharedPreferencesManager.getInstance().getExpiredTimeAppChannel(String.valueOf(appId));
            return currentTime > expiredTime;
        } catch (Exception e) {
            Log.e(this, e);
        }
        return true;
    }

    /***
     * need to call api if this app's info existed on cache and not expired time
     * @return
     */
    private boolean existedAppInfoOnCache() {
        try {
            ArrayList<String> mapChannelIDList = getChannelsForAppFromCache(String.valueOf(appId), transactionType);
            return mapChannelIDList != null && mapChannelIDList.size() > 0 && !isExpiredTime();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public void execute() {
        if (existedAppInfoOnCache() && mLoadAppInfoListener != null) {
            mLoadAppInfoListener.onSuccess();
        } else {
            loadAppInfoForAppFromServer();
        }
    }

    /***
     * call api get app info,used for app
     */
    public void loadAppInfoForAppFromServer() {
        BaseTask appInfoTask = new AppInfoTask(mLoadAppInfoListener, String.valueOf(appId), zaloUserId, accessToken);
        appInfoTask.makeRequest();
    }
}
