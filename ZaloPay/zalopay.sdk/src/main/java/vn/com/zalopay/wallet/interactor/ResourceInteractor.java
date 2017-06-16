package vn.com.zalopay.wallet.interactor;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.functions.Action1;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.api.RetryWithDelay;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkDownloadResourceMessage;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;

/***
 * download file resource
 */
public class ResourceInteractor {
    private Context mContext;
    private String mResourceZipFileURL;
    private String mResrcVer;
    private ReentrantLock mLock;
    private PlatformInfoStore.LocalStorage mPlatformStorage;
    private IDownloadService mDownloadService;
    private Action1<Response<ResponseBody>> downloadOnNext = this::saveResource;

    public ResourceInteractor(Context pContext, IDownloadService downloadService, PlatformInfoStore.LocalStorage pPlatformStorage, String pResourceZipFileURL, String pResrcVer) {
        this.mContext = pContext;
        this.mDownloadService = downloadService;
        this.mPlatformStorage = pPlatformStorage;
        this.mResourceZipFileURL = pResourceZipFileURL;
        this.mResrcVer = pResrcVer;
        this.mLock = new ReentrantLock();
    }

    protected Observable<Response<ResponseBody>> observableDownload(String pUrlFile) {
        return Observable.defer(() -> {
            try {
                return Observable.just(mDownloadService.getFile(pUrlFile).execute());
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    private void saveResource(Response<ResponseBody> pResponse) {
        if (pResponse == null || pResponse.body() == null) {
            onPostResult(false, getDefaulErrorNetwork());
            return;
        }
        try {
            /***
             * 0.get folder storage.
             * 1.download
             * 2.clear folder.
             * 3.extract
             * 4.save version to cache.
             */
            ResponseBody responseBody = pResponse.body();
            // Prepare unzip folder
            mLock.lock();
            if (TextUtils.isEmpty(mResrcVer)) {
                mResrcVer = mPlatformStorage.getResourceVersion();
            }
            String unzipFolder = StorageUtil.prepareUnzipFolder(mContext, BuildConfig.FOLDER_RESOURCE);
            //can not create folder storage for resource.
            if (TextUtils.isEmpty(unzipFolder)) {
                Log.e(this, "error create folder resource on device. Maybe your device memory run out of now.");
                onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
            } else if (mResourceZipFileURL == null || mResrcVer == null) {
                Log.e(this, "mResourceZipFileURL == null || resrcVer == null");
                onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
            } else {
                StorageUtil.decompress(responseBody.bytes(), unzipFolder);
                Log.d(this, "decompressed file zip to ", unzipFolder);
                //everything is ok, save version to cache
                mPlatformStorage.setUnzipPath(unzipFolder + mResrcVer);
                mPlatformStorage.setAppVersion(SdkUtils.getAppVersion(GlobalData.getAppContext()));
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

    private void onPostResult(boolean pIsSuccess, String pMessage) {
        SdkDownloadResourceMessage eventMessage = new SdkDownloadResourceMessage();
        eventMessage.success = pIsSuccess;
        eventMessage.message = pMessage;
        SDKApplication.getApplicationComponent().eventBus().post(eventMessage);
        Log.d(this, "posting to result download resource task");
    }

    private String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error_download_resource);
    }

    public Observable<Boolean> getResource() {
        return observableDownload(mResourceZipFileURL)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(downloadOnNext)
                .map(responseBodyResponse -> true);
    }
}
