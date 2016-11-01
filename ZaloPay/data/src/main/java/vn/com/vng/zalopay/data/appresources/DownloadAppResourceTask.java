package vn.com.vng.zalopay.data.appresources;

import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by AnhHieu on 5/21/16.
 *
 */
final class DownloadAppResourceTask {

    public interface Callback {
        void onSuccess();

        void onFailure();

        void onProgress(int progress);
    }

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

    public void execute(Callback callback) {
        //download(downloadInfo.)

        boolean isDownloadSuccess = download(downloadInfo, callback);

        Timber.d("isDownload %s", isDownloadSuccess);

        if (!isDownloadSuccess) {
            mLocalStorage.increaseRetryDownload(downloadInfo.appid);

            if (callback != null) {
                callback.onFailure();
            }
        } else {
            mLocalStorage.increaseStateDownload(downloadInfo.appid);

            if (callback != null) {
                callback.onSuccess();
            }
        }
    }

    private boolean download(DownloadInfo downloadInfo, Callback callback) {

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


    private String getExternalBundleFolder(int appId) {
        return String.format(Locale.getDefault(), "%s/modules/%d/app", mBundleRootFolder, appId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadAppResourceTask that = (DownloadAppResourceTask) o;

        return downloadInfo != null ? downloadInfo.equals(that.downloadInfo) : that.downloadInfo == null;

    }

    @Override
    public int hashCode() {
        return downloadInfo != null ? downloadInfo.hashCode() : 0;
    }
}
