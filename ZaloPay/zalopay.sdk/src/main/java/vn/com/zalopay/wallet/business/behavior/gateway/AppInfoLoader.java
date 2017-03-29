package vn.com.zalopay.wallet.business.behavior.gateway;

import java.util.ArrayList;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.request.AppInfoTask;
import vn.com.zalopay.wallet.datasource.request.BaseTask;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get app info
 */
public class AppInfoLoader extends SingletonBase {
    private ILoadAppInfoListener mLoadAppInfoListener;
    private long appId;
    private String zaloUserId;
    private String accessToken;
    private ETransactionType transactionType;

    public AppInfoLoader(long pAppId, ETransactionType pTransType, String pZaloUserId, String pAccessToken) {
        super();
        this.appId = pAppId;
        this.transactionType = pTransType;
        this.zaloUserId = pZaloUserId;
        this.accessToken = pAccessToken;
    }

    public static synchronized AppInfoLoader get(long pAppId, ETransactionType pTransType, String pZaloUserId, String pAccessToken) {
        return new AppInfoLoader(pAppId, pTransType, pZaloUserId, pAccessToken);
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
            return currentTime > expiredTime ? true : false;
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
            ArrayList<String> mapChannelIDList = getChannelsForAppFromCache(String.valueOf(appId), transactionType.toString());
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

    /***
     * get channels for this app on cache
     * @param pAppID
     * @param pTransType
     * @return
     */
    public static ArrayList<String> getChannelsForAppFromCache(String pAppID, String pTransType) throws Exception {
        String sKey = pAppID + Constants.UNDERLINE + pTransType;
        return SharedPreferencesManager.getInstance().getPmcConfigList(sKey);
    }
}
