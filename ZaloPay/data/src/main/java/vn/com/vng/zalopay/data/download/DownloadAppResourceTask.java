package vn.com.vng.zalopay.data.download;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

import static vn.com.vng.zalopay.data.download.FileUtil.*;

/**
 * Created by AnhHieu on 5/21/16.
 */
public class DownloadAppResourceTask {

    public interface Callback {
        void onSuccess();

        void onFailure();

        void onProgress(int progress);
    }

    private final OkHttpClient httpClient;

    private final Context context;
    private final DownLoadInfo downloadInfo;

    public DownloadAppResourceTask(Context context, DownLoadInfo appResourceEntity, OkHttpClient mOkHttpClient) {
        this.downloadInfo = appResourceEntity;
        this.context = context;
        this.httpClient = mOkHttpClient;
    }

    public void execute(Callback callback) {
        //download(downloadInfo.)

        boolean isDownloadSuccess = download(downloadInfo.url, callback);

        Timber.d("isDownload %s", isDownloadSuccess);

        if (!isDownloadSuccess) {
            deleteDirectory(new File(getUnZipPath(downloadInfo)));
            if (callback != null) {
                callback.onFailure();
            }
        } else {
            if (callback != null) {
                callback.onSuccess();
            }
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String temp) {

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

    private boolean download(String url, Callback callback) {

        Timber.d("url download %s", url);

        String temp = getTempFilePath();

        final Call call = httpClient.newCall(new Request.Builder().url(url).get().build());
        boolean result = false;
        try {
            Response response = call.execute();
            if (response.code() == 200) {
                result = writeResponseBodyToDisk(response.body(), temp);
            } else {
                Timber.e("response.code() %s", response.code());
            }
        } catch (Exception ex) {

            Timber.e("download exception %s", ex);

            return false;
        } finally {
            call.cancel();
        }
        Timber.i("result download %s", result);
        if (result) {
            // delete  temp
            String destinationPath = getUnZipPath(downloadInfo);
            Timber.d("destinationPath %s", destinationPath);
            try {
                unzip(temp, destinationPath);
            } catch (Exception ex) {
                Timber.e(ex, "exception unzip ");
                result = false;
            }
        }


        if (!result) {

        }

        deleteFile(temp);

        return result;
    }


    private String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private String getResourcePath() {
        return getRootPath() + File.separator + "zmres";
    }

    private String getTempFilePath() {
        return getResourcePath() + File.separator + "temp.zip";
    }

    private String getUnZipPath(DownLoadInfo downLoadInfo) {
        return getResourcePath() + File.separator + downLoadInfo.appname + File.separator + downLoadInfo.checksum;
    }
}
