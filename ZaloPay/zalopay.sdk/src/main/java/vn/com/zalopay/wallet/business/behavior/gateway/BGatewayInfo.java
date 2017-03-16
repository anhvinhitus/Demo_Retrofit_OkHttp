package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import java.io.File;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.GetPlatformInfo;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;

/**
 * Gateway info
 * checksum and check expiredtime check version ....
 * download new gateway info ..
 */
public class BGatewayInfo extends SingletonBase {
    private static BGatewayInfo mGatewayInfo = null;

    //prevent duplicate thread
    private boolean mProcessing;

    private ZPWGatewayInfoCallback mClientCallback;
    private ZPWGetGatewayInfoListener mListener = new ZPWGetGatewayInfoListener() {

        @Override
        public void onSuccess() {
            //mark as thread is finish
            mProcessing = false;
            if (mClientCallback != null)
                mClientCallback.onFinish();

        }

        @Override
        public void onProcessing() {
            if (mClientCallback != null) {
                mClientCallback.onProcessing();
            }
        }

        @Override
        public void onError(DPlatformInfo pMessage) {
            //mark as thread is finish
            mProcessing = false;
            if (pMessage != null) {
                ErrorManager.updateTransactionResult(pMessage.returncode);
            }
            if (mClientCallback != null) {
                mClientCallback.onError(pMessage != null ? pMessage.returnmessage : null);
            }
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            //mark as thread is finish
            mProcessing = false;
            if (mClientCallback != null) {
                mClientCallback.onUpVersion(pForceUpdate, pVersion, pMessage);
            }
        }
    };

    public BGatewayInfo() {
        super();
    }

    public static synchronized BGatewayInfo getInstance() {
        if (BGatewayInfo.mGatewayInfo == null) {
            BGatewayInfo.mGatewayInfo = new BGatewayInfo();
        }
        return BGatewayInfo.mGatewayInfo;
    }

    /***
     * rule for retry call get platform info
     * 1.expired time over
     * 2.app version is different
     * 3.resource file not exist
     * 4.new user
     * @return
     */
    public static boolean isNeedToGetPlatformInfo() throws Exception {
        long currentTime = System.currentTimeMillis();
        long expiredTime = SharedPreferencesManager.getInstance().getPlatformInfoExpriedTime();
        String checksumSDKV = SharedPreferencesManager.getInstance().getChecksumSDKversion();
        String userID = SharedPreferencesManager.getInstance().getCurrentUserID();
        boolean isNewUser = GlobalData.isNewUser();
        Log.d("isNeedToGetPlatformInfo", "==== BGatewayInfo.execute ====user id====" + userID);
        return currentTime > expiredTime || !ZPWUtils.getAppVersion(GlobalData.getAppContext()).equals(checksumSDKV) || !isValidConfig() || isNewUser;
    }

    /***
     * is file config.json existed?
     *
     * @return
     */
    public static boolean isValidConfig() {
        String path = null;
        try {
            path = SharedPreferencesManager.getInstance().getUnzipPath();
            File file = new File(path + File.separator + ResourceManager.CONFIG_FILE);
            // Check if res is missing ??
            return !TextUtils.isEmpty(path) && file.exists();
        } catch (Exception e) {
            Log.e("isValidConfig", e);
        }
        return false;
    }

    /***
     * call get platform info
     *
     * @param pListener
     */
    public synchronized void execute(ZPWGatewayInfoCallback pListener) throws Exception {
        this.mClientCallback = pListener;
        //keep weak merchant listener to callback in case need to retry platforminfo
        GlobalData.setMerchantCallBack(pListener);
        boolean isNeedToReloadPlatformInfo = isNeedToGetPlatformInfo();
        Log.d(this, "===isNeedToReloadPlatformInfo=" + isNeedToReloadPlatformInfo);
        if (isNeedToReloadPlatformInfo) {
            // Check if the task has finished yet?
            if (mProcessing) {
                Log.d(this, "there're a task get platforminfo is running...");
                if (this.mClientCallback != null) {
                    this.mClientCallback.onFinish();
                }
                return;
            }
            this.mProcessing = true;
            this.mClientCallback.onProcessing();
            try {
                Log.d(getClass().getName(), "Get gateway from server");
                BaseRequest task = new GetPlatformInfo(mListener, false, false, true);
                task.makeRequest();
            } catch (Exception e) {
                Log.d(this, e);
                if (this.mClientCallback != null) {
                    this.mClientCallback.onError(e != null ? e.getMessage() : null);
                }
            }
        } else {
            Log.d(getClass().getName(), "Get gateway from cache");
            if (mClientCallback != null) {
                mClientCallback.onFinish();
            }
        }

    }

    /***
     * app need to call this to re-update after user reset PIN
     * @param pListener
     */
    public synchronized void refreshPlatformInfo(ZPWGatewayInfoCallback pListener) {
        this.mClientCallback = pListener;
        try {
            BaseRequest task = new GetPlatformInfo(mListener, true, true);
            task.makeRequest();
        } catch (Exception e) {
            Log.e(this, e);
            if (this.mClientCallback != null) {
                this.mClientCallback.onError(e != null ? e.getMessage() : null);
            }
        }
    }
}
