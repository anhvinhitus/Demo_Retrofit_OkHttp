package vn.com.vng.zalopay.mdl.internal;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

/**
 * Created by huuhoa on 4/26/16.
 * Clone from ZaloPaySDK
 */
public class HttpClientRequest {
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

    public static byte[] getByteArray(String url) {
        return getByteArrayAndroid(url);
    }
}
