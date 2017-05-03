package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.DownloadResourceTask;
import vn.com.zalopay.wallet.datasource.task.PlatformInfoTask;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.message.SdkResourceInitMessage;
import vn.com.zalopay.wallet.message.SdkUpVersionMessage;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

public class PlatformInfoLoader extends SingletonBase {
    private static PlatformInfoLoader _object;
    private ZPWGetGatewayInfoListener mLoadGatewayInfoListener = new ZPWGetGatewayInfoListener() {
        @Override
        public void onProcessing() {
            Log.d(this, "get platforminfo in progress");
        }

        @Override
        public void onSuccess() {
            Log.d(this, "get platforminfo success, continue initialize resource to memory");
            if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).initializeResource();
            }
        }

        @Override
        public void onError(DPlatformInfo pMessage) {
            Log.d(this, pMessage != null ? pMessage.toJsonString() : "onError");
            if (pMessage != null) {
                ErrorManager.updateTransactionResult(pMessage.returncode);
            }
            SdkResourceInitMessage message = new SdkResourceInitMessage();
            message.success = false;
            message.message = pMessage != null ? pMessage.returnmessage : null;
            PaymentEventBus.shared().post(message);
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            Log.d(this, "need to up version from getplatforminfo");
            if (!pForceUpdate) {
                if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                    ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).initializeResource();
                }
            }
            SdkUpVersionMessage message = new SdkUpVersionMessage();
            message.forceupdate = pForceUpdate;
            message.version = pVersion;
            message.message = pMessage;
            PaymentEventBus.shared().post(message);
        }
    };

    public PlatformInfoLoader() {
        super();
    }

    public synchronized static PlatformInfoLoader getInstance() {
        if (PlatformInfoLoader._object == null) {
            PlatformInfoLoader._object = new PlatformInfoLoader();
        }
        return PlatformInfoLoader._object;
    }

    public void checkStaticResource() throws Exception {
        //check resource whether existed or not.
        boolean needToReloadPlatforminfo;
        try {
            needToReloadPlatforminfo = BGatewayInfo.isNeedToGetPlatformInfo();
        } catch (Exception e) {
            Log.e(this, e);
            needToReloadPlatforminfo = true;
        }
        if (needToReloadPlatforminfo) {
            try {
                Log.d(this, "===need to load resource again===");
                retryLoadGateway(false);
            } catch (Exception e) {
                Log.e(this, e);
                throw e;
            }
        } else if (!BGatewayInfo.isValidConfig()) {
            Log.d(this, "===resource didnt download===reload again===");
            try {
                retryLoadInfo();
            } catch (Exception e) {
                Log.d(this, e);
                throw e;
            }
        }
        //resource existed  and need to load into memory
        else if (!ResourceManager.isInit()) {
            Log.d(this, "===resource was downloaded but not init===init resource now");
            if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).initializeResource();
            }
        }
        //everything is ok now.
        else {
            SdkResourceInitMessage message = new SdkResourceInitMessage();
            message.success = true;
            PaymentEventBus.shared().post(message);
        }
    }

    /***
     * in case load gateway info successful but can not donwload resource
     * then we had resource version,resource url in cache
     * now need to retry to download again.
     */
    private void retryLoadInfo() throws Exception {
        String resourceVersion = SharedPreferencesManager.getInstance().getResourceVersion();
        String resourceDownloadUrl = SharedPreferencesManager.getInstance().getResourceDownloadUrl();
        if (!TextUtils.isEmpty(resourceDownloadUrl) && !TextUtils.isEmpty(resourceVersion)) {
            retryLoadResource(resourceDownloadUrl, resourceVersion);
        } else {
            retryLoadGateway(true);
        }
    }

    // retry downloading resource
    private void retryLoadResource(String pUrl, String pResourceVersion) {
        DownloadResourceTask downloadResourceTask = new DownloadResourceTask(pUrl, pResourceVersion);
        downloadResourceTask.makeRequest();
        Log.d(this, "starting retry download resource " + pUrl);
    }

    //retry load platform info
    private void retryLoadGateway(boolean pForceReload) {
        BaseTask getPlatformInfo = new PlatformInfoTask(mLoadGatewayInfoListener, pForceReload);
        getPlatformInfo.makeRequest();
        Log.d(this, "need to retry load platforminfo again force " + pForceReload);
    }

    public interface onCheckResourceStaticListener {
        void onCheckResourceStaticComplete(boolean isSuccess, String pError);

        void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
    }
}
