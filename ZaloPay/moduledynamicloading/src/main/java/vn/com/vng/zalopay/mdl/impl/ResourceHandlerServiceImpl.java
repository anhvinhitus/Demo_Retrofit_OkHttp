package vn.com.vng.zalopay.mdl.impl;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.mdl.MiniApplicationException;
import vn.com.vng.zalopay.mdl.ResourceHandlerService;
import vn.com.vng.zalopay.mdl.internal.FileUtils;

/**
 * Created by huuhoa on 4/29/16.
 * Implementation of resource handler service
 */
public class ResourceHandlerServiceImpl implements ResourceHandlerService {
    @Inject
    Context mContext;

    @Override
    public String getBundleRoot() {
        String packageName = mContext.getPackageName();
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + packageName + "/pmres";
    }

    @Override
    public void downloadFile(String url, String destinationPath) throws MiniApplicationException {
        try {
            DownloadHelper.downloadFile(url, destinationPath);
        } catch (NullPointerException e) {
            throw new MiniApplicationException("Null pointer in argument");
        } catch (IOException e) {
            throw new MiniApplicationException("IOException", e);
        }
    }

    @Override
    public void unzipFile(String zipPathName, String destinationPath) throws MiniApplicationException {
        try {
            FileUtils.unzipFile(zipPathName, destinationPath, true);
        } catch (IOException e) {
            throw new MiniApplicationException("IOException", e);
        }
    }

    private static class StreamHelper {
        static void copy(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private static class DownloadHelper {
        public static void downloadFile(String url, String destinationPath) throws NullPointerException,
                FileNotFoundException, IOException {
            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(destinationPath)) {
                throw new NullPointerException("url and destinationPath cannot be null");
            }

            File outputFile = new File(destinationPath);
            OutputStream outputStream = new FileOutputStream(outputFile);
            InputStream stream = null;
            try {
                URL toDownload = new URL(url);
                stream = toDownload.openStream();
                StreamHelper.copy(stream, outputStream);
            } catch (IOException e) {
                Timber.e(e, "Error");
                throw e;
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private static byte[] getByteArrayAndroid(String url) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream stream = null;
            try {
                URL toDownload = new URL(url);
                byte[] chunk = new byte[4096];
                int bytesRead;

                stream = toDownload.openStream();

                while ((bytesRead = stream.read(chunk)) > 0) {
                    outputStream.write(chunk, 0, bytesRead);
                }
            } catch (MalformedURLException e) {
                Timber.e(e, "Error");
                return null;
            } catch (IOException e) {
                Timber.e(e, "Error");
                return null;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException ex) {
                    Timber.e(ex, "############# NETWORK ERROR: " + ex.getMessage() + " ############");
                }
            }

            byte[] byteArray = outputStream.toByteArray();

            try {
                outputStream.close();
            } catch (IOException ex) {
                Timber.e(ex, "############# NETWORK ERROR: " + ex.getMessage() + " ############");
            }

            return byteArray;
        }
    }
}
