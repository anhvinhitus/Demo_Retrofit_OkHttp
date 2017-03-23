package vn.com.zalopay.wallet.datasource.request;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.listener.ZPWDownloadResourceListener;
import vn.com.zalopay.wallet.service.DownloadResourceService;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StorageUtil;
import vn.com.zalopay.wallet.utils.ZPWUtils;

/***
 * download resource file and save to internal storage
 */
public class DownloadBundle extends AsyncTask<Void, Void, Boolean> {
    public static String errorMessage = null;
    //prevent duplicate request
    public static boolean processing = false;
    private String mResourceZipFileURL;
    private String mUnzipFolder;
    private String mResrcVer;
    private ZPWDownloadResourceListener mCallBack;

    public DownloadBundle(ZPWDownloadResourceListener pListener, String pResourceZipFileURL, String pUnzipFolder, String pResrcVer) {
        this.mCallBack = pListener;
        this.mResourceZipFileURL = pResourceZipFileURL;
        this.mUnzipFolder = pUnzipFolder;
        this.mResrcVer = pResrcVer;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        errorMessage = null;
        processing = true;
    }

    @Override
    protected Boolean doInBackground(Void... pParams) {
        try {
            return downloadResourceZipFile();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    /***
     * 0.get folder storage.
     * 1.download
     * 2.clear folder.
     * 3.extract
     * 4.save version to cache.
     *
     * @return
     */
    private boolean downloadResourceZipFile() throws Exception {
        // Prepare unzip folder if still empty.
        if (TextUtils.isEmpty(mUnzipFolder))
            mUnzipFolder = StorageUtil.prepareUnzipFolder();

        //can not create folder storage for resource.
        if (TextUtils.isEmpty(mUnzipFolder)) {
            Log.e(this, "Error create folder resource on device.Maybe your device memory run out of now.");

            errorMessage = GlobalData.getStringResource(RS.string.zpw_string_error_storage);
            return false;
        }

        if (TextUtils.isEmpty(mResrcVer))
            mResrcVer = SharedPreferencesManager.getInstance().getResourceVersion();

        if (mResourceZipFileURL == null || mResrcVer == null) {
            Log.e(this, "mResourceZipFileURL == null || resrcVer == null");

            errorMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error_download_resource);
            return false;
        } else {
            long current = System.currentTimeMillis();
            StorageUtil.deleteRecursive(new File(this.mUnzipFolder));

            Log.d(getClass().getName(), "DeleteRecursive finished! Load time: " + String.valueOf(System.currentTimeMillis() - current) + "ms");

            try {
                current = System.currentTimeMillis();

                Response<ResponseBody> response = DataRepository.getInstanceDownloadResource().getBundleResource(this.mResourceZipFileURL);

                DataRepository.dispose();

                if (response != null && response.isSuccessful()) {
                    Log.d(getClass().getName(), "Zip down finished! Load time: " + String.valueOf(System.currentTimeMillis() - current) + "ms");

                    current = System.currentTimeMillis();

                    StorageUtil.decompress(response.body().bytes(), this.mUnzipFolder);
                    Log.d(getClass().getName(), "Decompress file finished! Load time: " + String.valueOf(System.currentTimeMillis() - current) + "ms");
                } else {
                    errorMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error_download_resource);
                    return false;
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
                errorMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error_download_resource);
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = GlobalData.getStringResource(RS.string.zpw_string_error_storage);
                return false;
            }
        }

        //everything is ok, save version to cache
        SharedPreferencesManager.getInstance().setUnzipPath(this.mUnzipFolder + mResrcVer);
        SharedPreferencesManager.getInstance().setChecksumSDKversion(ZPWUtils.getAppVersion(GlobalData.getAppContext()));

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        processing = false;

        if (!result) {
            /***
             * fail loading resource app, need to start service to retry to loading in background.
             */
            if (!TextUtils.isEmpty(mResourceZipFileURL) && !TextUtils.isEmpty(mUnzipFolder)
                    && !TextUtils.isEmpty(mResrcVer)) {
                Log.d(this, "===starting service to download resource in background..." + mResourceZipFileURL);

                DownloadResourceService.urlResourceToDownload = mResourceZipFileURL;
                DownloadResourceService.resourcePathInStorage = mUnzipFolder;
                DownloadResourceService.resrcVer = mResrcVer;
                DownloadResourceService.start(GlobalData.getAppContext());
            }
        }

        if (this.mCallBack != null) {
            //stop service download if it still is running.
            if (!TextUtils.isEmpty(DownloadResourceService.urlResourceToDownload))
                DownloadResourceService.stop(GlobalData.getAppContext());

            this.mCallBack.onLoadResourceComplete(result);
        }
    }
}
