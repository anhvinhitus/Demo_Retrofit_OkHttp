package vn.com.vng.zalopay.data.download;

import android.app.IntentService;
import android.content.Intent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.ResponseBody;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;

/**
 * Created by AnhHieu on 5/20/16.
 */
public class DownloadService extends IntentService {

//    @Inject
//    private OkHttpClient httpClient;

    private boolean running;

    public DownloadService() {
        super("DownloadService");
        Timber.tag("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private List<AppResourceEntity> listResource = new ArrayList<>();

    private void executeNext() {
        if (running) return; // Only one task at a time.

        int size = listResource.size();

        if (size != 0) {
            running = true;

        } else {
            Timber.d("Service stopping!");
            stopSelf(); // No more tasks are present. Stop.
        }
    }

    private boolean download(String url, String path) {
        File temp = ensureDirectory(path);

//        final Call call = httpClient.newCall(new Request.Builder().url(url).get().build());
        boolean result = false;
//        try {
//            Response response = call.execute();
//            if (response.code() == 200) {
//                result = writeResponseBodyToDisk(response.body(), temp);
//            }
//        } catch (Exception ex) {
//            call.cancel();
//        }
//
//        if (result) {
//            // delete  temp
//            unzip();
//        }

        return result;
    }

    private boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Timber.w("Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void unzip(String zipFilePath, String destinationPath) {
        try {
            File archive = new File(zipFilePath);

            ZipFile zipfile = new ZipFile(archive);

            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                Timber.d("Unzip file %s", entry.getName());
                unzipEntry(zipfile, entry, destinationPath);

            }

            zipfile.close();
        } catch (Exception e) {
            Timber.e(e, "Unzip exception");
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        Timber.v("Extracting: " + entry);

        InputStream zin = zipfile.getInputStream(entry);
        BufferedInputStream inputStream = new BufferedInputStream(zin);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copyFile(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {

        if (dir.exists()) {
            return;
        }

        Timber.v("Creating dir " + dir.getName());

        if (!dir.mkdirs()) {

            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    private File ensureDirectory(String path) {
        File file = new File(path);
        ensureDirectory(file);
        return file;
    }

    private void ensureDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
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

}
