package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import java.io.File;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.request.DownloadBundle;
import vn.com.zalopay.wallet.datasource.request.GetPlatformInfo;
import vn.com.zalopay.wallet.listener.ZPWDownloadResourceListener;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.listener.ZPWInitResourceListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StorageUtil;

public class GatewayLoader extends SingletonBase {
    private static GatewayLoader _object;
    private static GetPlatformInfo getGatewayInfoTask;
    private onCheckResourceStaticListener mCheckResourceStatisListener;
    /***
     * after download resource, need to load resource into memory/
     */
    private ZPWInitResourceListener mLoadResourceListener = new ZPWInitResourceListener() {

        @Override
        public void onSuccess() {
            Log.d(this, "====onSuccess=====");

            if (mCheckResourceStatisListener != null)
                mCheckResourceStatisListener.onCheckResourceStaticComplete(true, null);
        }

        @Override
        public void onError(String pMessage) {
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

            if (mCheckResourceStatisListener != null)
                mCheckResourceStatisListener.onCheckResourceStaticComplete(false, pMessage);
        }
    };
    private ZPWGetGatewayInfoListener mLoadGatewayInfoListener = new ZPWGetGatewayInfoListener() {
        @Override
        public void onProcessing() {
            Log.d(this, "===onProcessing===");
        }

        @Override
        public void onSuccess() {
            Log.d(this, "===onSuccess===");
            initResource(mLoadResourceListener);
        }

        @Override
        public void onError(DPlatformInfo pMessage) {
            Log.d(this, pMessage != null ? pMessage.toJsonString() : "onError");

            if (pMessage != null) {
                ErrorManager.updateTransactionResult(pMessage.returncode);
            }

            if (mCheckResourceStatisListener != null)
                mCheckResourceStatisListener.onCheckResourceStaticComplete(false, pMessage != null ? pMessage.returnmessage : null);
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            Log.d(this, "===onUpVersion===");

            if (mCheckResourceStatisListener != null)
                mCheckResourceStatisListener.onUpVersion(pForceUpdate, pVersion, pMessage);

            if (!pForceUpdate) {
                initResource(mLoadResourceListener);
            }
        }
    };
    /***
     * download resource listener.
     */
    private ZPWDownloadResourceListener mDownloadResourceListener = new ZPWDownloadResourceListener() {
        @Override
        public void onLoadResourceComplete(boolean isSuccess) {
            if (isSuccess) {
                Log.d(this, "======isSuccess=true=====");

                initResource(mLoadResourceListener);
            } else {
                Log.d(this, "=====isSuccess=false=====");

                if (mCheckResourceStatisListener != null)
                    mCheckResourceStatisListener.onCheckResourceStaticComplete(false, null);
            }
        }
    };
    public GatewayLoader() {
        super();
    }

    public synchronized static GatewayLoader getInstance() {
        if (GatewayLoader._object == null)
            GatewayLoader._object = new GatewayLoader();

        return GatewayLoader._object;
    }

    /***
     * load gateway info listener
     */
    public ZPWGetGatewayInfoListener getLoadGatewayInfoListener() {
        return mLoadGatewayInfoListener;
    }

    public GatewayLoader setOnCheckResourceStaticListener(onCheckResourceStaticListener pListener) {
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
        }
        else if (!BGatewayInfo.isValidConfig()) {
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
        String unzipFolder = StorageUtil.prepareUnzipFolder();

        if (!TextUtils.isEmpty(resourceDownloadUrl) && !TextUtils.isEmpty(resourceVersion) && !TextUtils.isEmpty(unzipFolder)) {
            retryLoadResource(resourceDownloadUrl, unzipFolder, resourceVersion);
        } else {
            retryLoadGateway(true);
        }
    }

    /***
     * after download resource successfully.
     * need to load to memory.
     *
     * @param pCallback
     */
    private void initResource(ZPWInitResourceListener pCallback) {
        BundleResourceLoader initResourceTask = new BundleResourceLoader(pCallback);
        initResourceTask.execute();
    }

    // retry downloading resource
    private void retryLoadResource(String pUrl, String pUnzipFolder, String pResourceVersion) {
        DownloadBundle downloadResourceTask = new DownloadBundle(mDownloadResourceListener, pUrl, pUnzipFolder, pResourceVersion);
        downloadResourceTask.execute();

        Log.d(this, "===starting retry loading resource...===");
    }

    //retry load gateway info
    private void retryLoadGateway(boolean pForceReload) throws Exception {
        if (getGatewayInfoTask == null)
            getGatewayInfoTask = GetPlatformInfo.getInstance(mLoadGatewayInfoListener, pForceReload);

        getGatewayInfoTask.makeRequest();

        Log.d(this, "===retryLoadGateway===");
    }

    public interface onCheckResourceStaticListener {
        void onCheckResourceStaticComplete(boolean isSuccess, String pError);

        void onCheckResourceStaticInProgress();

        void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
    }
}
