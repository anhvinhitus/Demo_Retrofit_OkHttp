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

    private boolean writeResponseBodyToDisk(ResponseBody body, File temp) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[1024 * 2];

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
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private boolean download(String url, Callback callback) {

        File temp = ensureDirectory(getTempFilePath());

        final Call call = httpClient.newCall(new Request.Builder().url(url).get().build());
        boolean result = false;
        try {
            Response response = call.execute();
            if (response.code() == 200) {
                result = writeResponseBodyToDisk(response.body(), temp);
            }
        } catch (Exception ex) {

            Timber.e("download exception %s", ex);

            return false;
        } finally {
            call.cancel();
        }

        if (result) {
            // delete  temp

            String destinationPath = getUnZipPath(downloadInfo);

            Timber.d("destinationPath %s", destinationPath);
            try {
                unzipFile(temp, destinationPath, false);
            } catch (IOException ex) {
                result = false;
            }
        }

        if (temp != null && temp.exists()) {
            temp.delete();
        }

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
