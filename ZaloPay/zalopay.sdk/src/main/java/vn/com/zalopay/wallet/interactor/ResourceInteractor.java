package vn.com.zalopay.wallet.interactor;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.api.RetryWithDelay;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkDownloadResourceMessage;
import vn.com.zalopay.wallet.exception.SdkResourceException;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;

/***
 * download file resource
 */
public class ResourceInteractor {
    private Context mContext;
    private String mResourceZipFileURL;
    private String mResourceVersion;
    private ReentrantLock mLock;
    private PlatformInfoStore.LocalStorage mPlatformStorage;
    private IDownloadService mDownloadService;

    public ResourceInteractor(Context pContext, IDownloadService downloadService, PlatformInfoStore.LocalStorage pPlatformStorage, String pResourceZipFileURL, String pResrcVer) {
        this.mContext = pContext;
        this.mDownloadService = downloadService;
        this.mPlatformStorage = pPlatformStorage;
        this.mResourceZipFileURL = pResourceZipFileURL;
        this.mResourceVersion = pResrcVer;
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
            onPostResult(false, getDefaultError());
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
            String unzipFolder = StorageUtil.prepareUnzipFolder(mContext, BuildConfig.FOLDER_RESOURCE);
            //can not create folder storage for resource.
            if (TextUtils.isEmpty(unzipFolder)) {
                Timber.w("error create folder resource on device. Maybe your device memory run out of now");
                onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
            } else if (mResourceZipFileURL == null || mResourceVersion == null) {
                onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
            } else {
                StorageUtil.decompress(responseBody.bytes(), unzipFolder);
                Timber.d("decompressed file zip to %s", unzipFolder);
                //everything is ok, save version to cache
                mPlatformStorage.setUnzipPath(unzipFolder + mResourceVersion);
                mPlatformStorage.setAppVersion(SdkUtils.getAppVersion(GlobalData.getAppContext()));
                onPostResult(true, null);//post signal success
            }
        } catch (IOException e) {
            onPostResult(false, GlobalData.getStringResource(RS.string.zpw_string_error_storage));
            Log.e(this, e);
        } catch (Exception e) {
            onPostResult(false, getDefaultError());
            Log.e(this, e);
        } finally {
            mLock.unlock();
        }
    }

    private void onPostResult(boolean success, String message) {
        SdkDownloadResourceMessage eventMessage = new SdkDownloadResourceMessage(success, message);
        SDKApplication.getApplicationComponent().eventBus().post(eventMessage);
        Timber.d("posting to result download resource task");
    }

    private String getDefaultError() {
        return GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error_download_resource);
    }

    public Observable<Boolean> getResource() {
        return observableDownload(mResourceZipFileURL)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(this::saveResource)
                .map(responseBodyResponse -> true);
    }

    public Observable<SdkDownloadResourceMessage> fetchResource() {
        return observableDownload(mResourceZipFileURL)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .concatMap(this::observeSaveResource);
    }

    private Observable<SdkDownloadResourceMessage> observeSaveResource(Response<ResponseBody> pResponse) {
        if (pResponse == null || pResponse.body() == null) {
            return Observable.error(new SdkResourceException(getDefaultError()));
        }
        try {
            /*
             * 0.get folder storage.
             * 1.download
             * 2.clear folder.
             * 3.extract
             * 4.save version to cache.
             */
            ResponseBody responseBody = pResponse.body();
            mLock.lock();
            String unzipFolder = StorageUtil.prepareUnzipFolder(mContext, BuildConfig.FOLDER_RESOURCE);
            //can not create folder storage for resource.
            if (TextUtils.isEmpty(unzipFolder)) {
                Timber.w("error create folder resource on device. Maybe your device memory run out of now");
                return Observable.error(getMessException(GlobalData.getStringResource(RS.string.zpw_string_error_storage)));
            } else if (mResourceZipFileURL == null || mResourceVersion == null) {
                return Observable.error(getMessException(GlobalData.getStringResource(RS.string.zpw_string_error_storage)));
            } else {
                StorageUtil.decompress(responseBody.bytes(), unzipFolder);
                Timber.d("decompressed file zip to %s", unzipFolder);
                mPlatformStorage.setResourceVersion(mResourceVersion);
                mPlatformStorage.setUnzipPath(unzipFolder + mResourceVersion);
                return Observable.just(new SdkDownloadResourceMessage(true, null));//post signal success
            }
        } catch (IOException e) {
            Timber.w(e, "Exception IO");
            return Observable.error(getMessException(GlobalData.getStringResource(RS.string.zpw_string_error_storage)));
        } catch (Exception e) {
            Timber.w(e, "Exception on save resource");
            return Observable.error(getMessException(getDefaultError()));
        } finally {
            mLock.unlock();
        }
    }

    private SdkResourceException getMessException(String error) {
        return new SdkResourceException(error);
    }
}
