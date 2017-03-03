package vn.com.zalopay.wallet.business.behavior.gateway;

import java.util.ArrayList;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.GetAppInfo;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get app info class
 */
public class AppInfoLoader extends SingletonBase {
    private static AppInfoLoader _object = null;

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

    public AppInfoLoader() {
        super();

        this.appId = GlobalData.appID;
        this.transactionType = GlobalData.getTransactionType();
    }

    /***
     * singleton function
     * this used by sdk
     *
     * @return
     */
    public static synchronized AppInfoLoader getInstance() {
        if (AppInfoLoader._object == null)
            AppInfoLoader._object = new AppInfoLoader();

        return AppInfoLoader._object;
    }

    /***
     * overload function
     * this is used by app
     *
     * @param pAppId
     * @param pTransType
     * @return
     */
    public static synchronized AppInfoLoader getInstance(long pAppId, ETransactionType pTransType, String pZaloUserId, String pAccessToken) {
        return new AppInfoLoader(pAppId, pTransType, pZaloUserId, pAccessToken);
    }

    /***
     * set callback to get result from app info api
     *
     * @param pLoadAppInfoListener
     * @return
     */
    public AppInfoLoader setOnLoadAppInfoListener(ILoadAppInfoListener pLoadAppInfoListener) {
        this.mLoadAppInfoListener = pLoadAppInfoListener;
        return this;
    }

    /***
     * check expired time on cache
     *
     * @return
     */
    private boolean isExpiredTime() {
        try {
            long currentTime = System.currentTimeMillis();
            long expiredTime = 0;
            expiredTime = SharedPreferencesManager.getInstance().getExpiredTimeAppChannel(String.valueOf(appId));

            return currentTime > expiredTime ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /***
     * need to call api if this app's info existed on cache and not expired time
     *
     * @return
     */
    private boolean existedAppInfoOnCache() {
        try {
            ArrayList<String> mapChannelIDList = getChannelsForApp(String.valueOf(appId), transactionType.toString());

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
            loadAppInfoForSDK();
        }
    }

    public void execureForMerchant() {
        if (existedAppInfoOnCache() && mLoadAppInfoListener != null) {
            Log.d(getClass().getName(), "app info from cache and not expired");

            mLoadAppInfoListener.onSuccess();
        } else {
            loadAppInfoForApp();
        }
    }

    /***
     * call this api,used in sdk
     */
    public void loadAppInfoForSDK() {
        BaseRequest getAppInfoTask = new GetAppInfo(mLoadAppInfoListener);
        getAppInfoTask.makeRequest();
    }

    /***
     * call api get app info,used for app
     */
    public void loadAppInfoForApp() {
        BaseRequest getAppInfoTask = new GetAppInfo(String.valueOf(appId), zaloUserId, accessToken, mLoadAppInfoListener);
        getAppInfoTask.makeRequest();
    }

    /***
     * get channels for this app on cache
     *
     * @param pAppID
     * @param pTransType
     * @return
     */
    public ArrayList<String> getChannelsForApp(String pAppID, String pTransType) throws Exception {
        String sKey = pAppID + Constants.UNDERLINE + pTransType;
        return SharedPreferencesManager.getInstance().getPmcConfigList(sKey);
    }


}
