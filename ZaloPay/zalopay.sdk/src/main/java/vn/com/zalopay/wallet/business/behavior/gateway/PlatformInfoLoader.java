package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.DownloadResourceTask;
import vn.com.zalopay.wallet.datasource.task.PlatformInfoTask;
import vn.com.zalopay.wallet.message.DownloadResourceEventMessage;
import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.listener.ZPWInitResourceListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StorageUtil;

public class PlatformInfoLoader extends SingletonBase {
    private static PlatformInfoLoader _object;
    private onCheckResourceStaticListener mCheckResourceStatisListener;
    private ZPWInitResourceListener mLoadResourceListener = new ZPWInitResourceListener() {

        @Override
        public void onSuccess() {
            Log.d(this, "init resource ssucess");
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onCheckResourceStaticComplete(true, null);
            }
        }

        @Override
        public void onError(String pMessage) {
            Log.d(this, "init resource error " + pMessage);
            /***
             * delete folder resource to download again.
             * this prevent case file resource downloaded but was damaged on the wire so
             * can not parse json file.
             */
            try {
                String resPath = SharedPreferencesManager.getInstance().getUnzipPath();
                if (!TextUtils.isEmpty(resPath))
                    StorageUtil.deleteRecursive(new File(resPath));
            } catch (Exception e) {
                Log.d(this, e);
            }
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onCheckResourceStaticComplete(false, pMessage);
            }
        }
    };
    private ZPWGetGatewayInfoListener mLoadGatewayInfoListener = new ZPWGetGatewayInfoListener() {
        @Override
        public void onProcessing() {
            Log.d(this, "get platforminfo in progress");
        }

        @Override
        public void onSuccess() {
            Log.d(this, "get platforminfo success");
            initResource(mLoadResourceListener);
        }

        @Override
        public void onError(DPlatformInfo pMessage) {
            Log.d(this, pMessage != null ? pMessage.toJsonString() : "onError");
            if (pMessage != null) {
                ErrorManager.updateTransactionResult(pMessage.returncode);
            }
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onCheckResourceStaticComplete(false, pMessage != null ? pMessage.returnmessage : null);
            }
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            Log.d(this, "need to up version from getplatforminfo");
            if (!pForceUpdate) {
                initResource(mLoadResourceListener);
            }
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onUpVersion(pForceUpdate, pVersion, pMessage);
            }
        }
    };

    public PlatformInfoLoader() {
        super();
        PaymentEventBus.shared().register(this);
    }

    public synchronized static PlatformInfoLoader getInstance() {
        if (PlatformInfoLoader._object == null) {
            PlatformInfoLoader._object = new PlatformInfoLoader();
        }
        return PlatformInfoLoader._object;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDownloadResourceMessageEvent(DownloadResourceEventMessage result) {
        Log.d(this, "OnDownloadResourceMessageEvent " + GsonUtils.toJsonString(result));
        if (result.isSuccess) {
            initResource(mLoadResourceListener);
        } else if (mCheckResourceStatisListener != null) {
            mCheckResourceStatisListener.onCheckResourceStaticComplete(false, result.message);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        PaymentEventBus.shared().unregister(this);
    }

    public PlatformInfoLoader setOnCheckResourceStaticListener(onCheckResourceStaticListener pListener) {
        mCheckResourceStatisListener = pListener;
        return this;
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
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onCheckResourceStaticInProgress();
            }
            try {
                Log.d(this, "===need to load resource again===");
                retryLoadGateway(false);
            } catch (Exception e) {
                Log.e(this, e);
                throw e;
            }
        } else if (!BGatewayInfo.isValidConfig()) {
            Log.d(this, "===resource wasnt download===reload again===");
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onCheckResourceStaticInProgress();
            }
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
            if (mCheckResourceStatisListener != null) {
                mCheckResourceStatisListener.onCheckResourceStaticInProgress();
            }
            initResource(mLoadResourceListener);
        }
        //everything is ok now.
        else if (mCheckResourceStatisListener != null) {
            mCheckResourceStatisListener.onCheckResourceStaticComplete(true, null);
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

    /***
     * after download resource successfully.
     * need to load to memory.
     * @param pCallback
     */
    private void initResource(ZPWInitResourceListener pCallback) {
        if(!BGatewayInfo.isValidConfig())
        {
            Log.d(this,"call init resource but not ready for now");
            return;
        }
        BundleResourceLoader initResourceTask = new BundleResourceLoader(pCallback);
        initResourceTask.execute();
        Log.d(this,"init resource");
    }

    // retry downloading resource
    private void retryLoadResource(String pUrl, String pResourceVersion) {
        DownloadResourceTask downloadResourceTask = new DownloadResourceTask(pUrl, pResourceVersion);
        downloadResourceTask.makeRequest();
        Log.d(this, "starting retry loading resource");
    }

    //retry load platform info
    private void retryLoadGateway(boolean pForceReload) {
        BaseTask getPlatformInfo = new PlatformInfoTask(mLoadGatewayInfoListener, pForceReload);
        getPlatformInfo.makeRequest();
        Log.d(this, "need to retry load platforminfo again force "+pForceReload);
    }

    public interface onCheckResourceStaticListener {
        void onCheckResourceStaticComplete(boolean isSuccess, String pError);
        void onCheckResourceStaticInProgress();
        void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
    }
}
