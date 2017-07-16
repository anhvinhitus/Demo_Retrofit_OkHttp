package vn.com.zalopay.wallet.interactor;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.api.RetryWithDelay;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.constants.Constants;
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

    private String getDefaultError() {
        return GlobalData.getAppContext().getResources().getString(R.string.sdk_error_download_resource_mess);
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
                return Observable.error(getMessException(GlobalData.getAppContext().getResources().getString(R.string.sdk_error_extract_resource_mess)));
            } else if (mResourceZipFileURL == null || mResourceVersion == null) {
                return Observable.error(getMessException(GlobalData.getAppContext().getResources().getString(R.string.sdk_error_extract_resource_mess)));
            } else {
                StorageUtil.decompress(responseBody.bytes(), unzipFolder);
                Timber.d("extract resource to %s", unzipFolder);
                mPlatformStorage.setResourceVersion(mResourceVersion);
                mPlatformStorage.setResourcePath(unzipFolder + mResourceVersion);
                return Observable.just(new SdkDownloadResourceMessage(true, null));
            }
        } catch (IOException e) {
            Timber.w(e, "Exception IO");
            return Observable.error(getMessException(GlobalData.getAppContext().getResources().getString(R.string.sdk_error_extract_resource_mess)));
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
