package vn.com.vng.zalopay.utils;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader extends AsyncTask<FileDownloader.DownloadParam, Void, Void> {
    @Override
    protected Void doInBackground(DownloadParam... downloadParams) {
        if (downloadParams == null || downloadParams.length == 0) {
            return null;
        }

        DownloadParam param = downloadParams[0];
        if (param.url == null ||
            param.dest == null ||
            param.fileName == null ||
            param.fileName.equalsIgnoreCase("") ||
            param.url.equalsIgnoreCase("") ||
            param.dest.equalsIgnoreCase("")) {
            return null;
        }

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            ensureRootDirExists(param.dest);
            URL url = new URL(param.url);
            String destinationPath = param.dest + param.fileName + ".ttf";
            File dest = new File(destinationPath);

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                param.callback.onFail(new Exception("Cannot connect to server"));
            }

            input = new BufferedInputStream(url.openStream(), 8 * 1024);
            output = new FileOutputStream(dest);
            byte data[] = new byte[8 * 1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            param.callback.onSuccess(dest);
        } catch (IOException e) {
            e.printStackTrace();
            param.callback.onFail(e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                param.callback.onFail(e);
            }
        }
        return null;
    }

    private File ensureRootDirExists(String path) throws IOException {
        File rootDir = new File(path);
        if (!(rootDir.mkdirs() || rootDir.isDirectory())) {
            throw new IOException("Couldn't create root storage directory '" + path + "'");
        }
        return rootDir;
    }

    public interface DownloadCallback {
        void onSuccess(File dest);
        void onFail(Exception e);
    }

    public static class DownloadParam {
        public DownloadCallback callback;
        public String url;
        public String dest;
        public String fileName;
    }
}

