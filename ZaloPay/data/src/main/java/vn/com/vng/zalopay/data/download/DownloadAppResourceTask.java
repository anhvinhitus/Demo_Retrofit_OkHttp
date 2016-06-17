package vn.com.vng.zalopay.data.download;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.AppResource;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;

import static vn.com.vng.zalopay.data.download.FileUtil.ensureDirectory;
import static vn.com.vng.zalopay.data.download.FileUtil.unzip;

/**
 * Created by AnhHieu on 5/21/16.
 *
 */
public class DownloadAppResourceTask {

    public interface Callback {
        void onSuccess();

        void onFailure();

        void onProgress(int progress);
    }

    private final OkHttpClient httpClient;

    private final Context context;
    private final DownloadInfo downloadInfo;
    private final AppResource.LocalStorage mLocalStorage;

    private final String mBundleRootFolder;

    public DownloadAppResourceTask(Context context,
                                   DownloadInfo appResourceEntity,
                                   OkHttpClient mOkHttpClient,
                                   AppResource.LocalStorage localStorage,
                                   String rootBundle) {

        this.downloadInfo = appResourceEntity;
        this.context = context;
        this.httpClient = mOkHttpClient;
        this.mLocalStorage = localStorage;
        this.mBundleRootFolder = rootBundle;
    }

    public void execute(Callback callback) {
        //download(downloadInfo.)

        boolean isDownloadSuccess = download(downloadInfo, callback);

        Timber.d("isDownload %s", isDownloadSuccess);

        if (!isDownloadSuccess) {
            if (callback != null) {
                callback.onFailure();
            }

            mLocalStorage.increaseRetryDownload(downloadInfo.appid);
        } else {
            mLocalStorage.increaseStateDownload(downloadInfo.appid);

            if (callback != null) {
                callback.onSuccess();
            }
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, File temp) {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            byte[] fileReader = new byte[1024 * 4];

            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(temp);

            while (true) {
                int read = inputStream.read(fileReader);

                if (read == -1) {
                    break;
                }

                outputStream.write(fileReader, 0, read);

                fileSizeDownloaded += read;
            }

            outputStream.flush();

            if (fileSize != fileSizeDownloaded) {
                return false;
            }
            return true;
        } catch (IOException e) {
            Timber.e(e, " download exception %s", e);
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
            }
        }

    }

    private boolean download(DownloadInfo downloadInfo, Callback callback) {

        Timber.d("url download %s", downloadInfo.url);
        String resourcePath = getResourcePath();
        ensureDirectory(resourcePath);
        final File file = new File(resourcePath, "temp.zip");

        final Call call = httpClient.newCall(new Request.Builder().url(downloadInfo.url).get().build());
        boolean result = false;
        try {
            Response response = call.execute();
            if (response.code() == 200) {
                result = writeResponseBodyToDisk(response.body(), file);
            } else {
                Timber.e("response.code() %s", response.code());
            }

            response.body().close();
        } catch (Exception ex) {
            Timber.e("download exception %s", ex);
        }

        Timber.i("result download %s", result);
        if (result) {
            String destinationPath = getExternalBundleFolder(downloadInfo.appid);
            Timber.d("destinationPath %s", destinationPath);
            try {
                unzip(file.getAbsolutePath(), destinationPath);
            } catch (Exception ex) {
                Timber.e(ex, "exception unzip ");
                result = false;
            }
        }

        if (file.exists()) {
            file.delete();
        }

        return result;
    }


    public String getExternalBundleFolder(int appId) {
        return String.format(Locale.getDefault(), "%s/modules/%d/app", mBundleRootFolder, appId);
    }

    public String getResourcePath() {
        return mBundleRootFolder;
    }
}
