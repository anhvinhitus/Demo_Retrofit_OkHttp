package vn.com.zalopay.wallet.datasource.task;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.ResponseBody;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.message.DownloadResourceEventMessage;
import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StorageUtil;
import vn.com.zalopay.wallet.utils.ZPWUtils;

/***
 * download file resource
 */
public class DownloadResourceTask extends BaseTask<ResponseBody> {
    private static final String TAG = DownloadResourceTask.class.getCanonicalName();
    private String mResourceZipFileURL;
    private String mResrcVer;
    private ReentrantLock mLock;

    public DownloadResourceTask(String pResourceZipFileURL, String pResrcVer) {
        super();
        this.mResourceZipFileURL = pResourceZipFileURL;
        this.mResrcVer = pResrcVer;
        mLock = new ReentrantLock();
    }

    @Override
    public void onDoTaskOnResponse(ResponseBody pResponse) {
        if (pResponse == null) {
            onPostResult(false, getDefaulErrorNetwork());
        } else if (pResponse != null) {
            try {
                /***
                 * 0.get folder storage.
                 * 1.download
                 * 2.clear folder.
                 * 3.extract
                 * 4.save version to cache.
                 * @return
                 */
                // Prepare unzip folder if still empty.
                mLock.lock();
                String unzipFolder = StorageUtil.prepareUnzipFolder();
                if (TextUtils.isEmpty(unzipFolder)) {
                    unzipFolder = StorageUtil.prepareUnzipFolder();
                }
                if (TextUtils.isEmpty(mResrcVer)) {
                    mResrcVer = SharedPreferencesManager.getInstance().getResourceVersion();
                }
                //can not create folder storage for resource.
                if (TextUtils.isEmpty(unzipFolder)) {
                    Log.e(this, "error create folder resource on device.Maybe your device memory run out of now.");
                    onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
                } else if (mResourceZipFileURL == null || mResrcVer == null) {
                    Log.e(this, "mResourceZipFileURL == null || resrcVer == null");
                    onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
                } else {
                    StorageUtil.decompress(pResponse.bytes(), unzipFolder);
                    Log.d(this, "decompressed file zip to " + unzipFolder);
                    //everything is ok, save version to cache
                    SharedPreferencesManager.getInstance().setUnzipPath(unzipFolder + mResrcVer);
                    SharedPreferencesManager.getInstance().setChecksumSDKversion(ZPWUtils.getAppVersion(GlobalData.getAppContext()));
                    onPostResult(true, null);//post signal success
                }
            } catch (IOException e) {
                onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
                Log.e(this, e);
            } catch (Exception e) {
                onPostResult(false, getDefaulErrorNetwork());
                Log.e(this, e);
            } finally {
                mLock.unlock();
            }
        }
    }

    protected void onPostResult(boolean pIsSuccess, String pMessage) {
        DownloadResourceEventMessage eventMessage = new DownloadResourceEventMessage();
        eventMessage.isSuccess = pIsSuccess;
        eventMessage.message = pMessage;
        PaymentEventBus.shared().post(eventMessage);
        Log.d(this, "posting to result download resource task");
    }

    @Override
    public void onRequestSuccess(ResponseBody pResponse) {
        Log.d(this, "onRequestSuccess");
    }

    @Override
    public void onRequestFail(Throwable e) {
        Log.d(TAG, e);
    }

    @Override
    public void onRequestInProcess() {
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error_download_resource);
    }

    @Override
    protected void doRequest() {
        DataRepository.getInstanceDownloadResource().setTask(this).downloadResource(mResourceZipFileURL);
    }

    @Override
    protected boolean doParams() {
        return true;
    }
}
