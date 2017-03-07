package vn.com.vng.zalopay.data.appresources;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by AnhHieu on 5/21/16.
 * *
 */
final class DownloadAppResourceTask {

    private final OkHttpClient httpClient;
    private final DownloadInfo downloadInfo;
    private final AppResourceStore.LocalStorage mLocalStorage;
    private final String mBundleRootFolder;

    DownloadAppResourceTask(DownloadInfo appResourceEntity,
                            OkHttpClient mOkHttpClient,
                            AppResourceStore.LocalStorage localStorage,
                            String rootBundle) {

        this.downloadInfo = appResourceEntity;
        this.httpClient = mOkHttpClient;
        this.mLocalStorage = localStorage;
        this.mBundleRootFolder = rootBundle;
    }

    boolean execute() {
        boolean isDownloadSuccess;
        try {
            isDownloadSuccess = download(downloadInfo);

            Timber.d("isDownload %s", isDownloadSuccess);

            if (isDownloadSuccess) {
                mLocalStorage.increaseStateDownload(downloadInfo.appid);
            } else {
                mLocalStorage.increaseRetryDownload(downloadInfo.appid);
            }
        } catch (Throwable t) {
            Timber.w(t, "Error while executing download task");
            isDownloadSuccess = false;
        }

        return isDownloadSuccess;
    }

    /**
     * Synchronous call to download resources from server
     * @return true if download success
     */
    private boolean download(DownloadInfo downloadInfo) {

        Timber.d("url download %s", downloadInfo.url);
        String destinationPath = getExternalBundleFolder(downloadInfo.appid);
        Timber.d("destinationPath %s", destinationPath);

        FileUtil.ensureDirectory(mBundleRootFolder);
        FileUtil.ensureDirectory(destinationPath);

        boolean result = false;
        try {
            final Call call = httpClient.newCall(new Request.Builder().url(downloadInfo.url).get().build());
            Response response = call.execute();

            if (response != null && response.isSuccessful()) {
                FileUtil.decompress(response.body().bytes(), destinationPath);
                result = true;
            } else {
                int code = response == null ? -1 : response.code();
                Timber.w("Response: %d for download URL %s", code, downloadInfo.url);
            }

            if (response != null) {
                response.body().close();
            }
        } catch (Exception ex) {
            Timber.e(ex, "Exception while downloading Apps: %s", downloadInfo.url);
        }

        Timber.i("result download %s", result);

        return result;
    }


    private String getExternalBundleFolder(long appId) {
        //return String.format(Locale.getDefault(), "%s/modules/%d/app", mBundleRootFolder, appId);
        return ResourceHelper.getPath(appId);
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadAppResourceTask that = (DownloadAppResourceTask) o;

        return downloadInfo != null ? downloadInfo.equals(that.downloadInfo) : that.downloadInfo == null;
    }

    @Override
    public int hashCode() {
        return downloadInfo != null ? downloadInfo.hashCode() : 0;
    }
}
