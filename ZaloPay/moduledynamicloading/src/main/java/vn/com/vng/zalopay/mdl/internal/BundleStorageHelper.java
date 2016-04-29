package vn.com.vng.zalopay.mdl.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Environment;
import android.util.Base64;

import timber.log.Timber;

/**
 * Created by huuhoa on 4/26/16.
 * Storage helper for bundle manager
 * Clone from ZaloPaySDK
 */
public class BundleStorageHelper {
    /**
     * Download the ap-to-date resource zip file from server and decompress it
     * on storage (internal storage or external sd-card)
     *
     * @return {@code TRUE} if success, {@code FALSE} otherwise
     */
    public static boolean downloadResourceZipFile(String resourceUrl, String unzipFolder, String version) {
        if (resourceUrl == null) {
            return false;
        }

        long mStartTime = System.currentTimeMillis();
        long current = System.currentTimeMillis();
        deleteRecursive(new File(unzipFolder));
        Timber.d("DeleteRecursive finished! Load time: " + String.valueOf(System.currentTimeMillis() - current)
                        + "ms");

        try {
            current = System.currentTimeMillis();

            // Clear heap
            System.gc();

            byte[] compressedBytes = HttpClientRequest.getByteArray(resourceUrl);
            Timber.d("Zip down finished! Load time: " + String.valueOf(System.currentTimeMillis() - current) + "ms");

            current = System.currentTimeMillis();
            decompress(compressedBytes, unzipFolder + File.pathSeparator + version);
            Timber.d("Decompress file finished! Load time: " + String.valueOf(System.currentTimeMillis() - current)
                            + "ms");
        } catch (NullPointerException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Timber.d("Completed! Load time: " + String.valueOf(System.currentTimeMillis() - mStartTime) + "ms");

        return true;
    }

    /**
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static void decompress(byte[] compressed, String location) throws IOException {

        InputStream is;
        ZipInputStream zis;

        String filename;
        is = new ByteArrayInputStream(compressed);
        zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();

            // Need to create directories if not exists, or
            // it will generate an Exception...
            if (ze.isDirectory()) {
                String path = location + File.separator + filename;
                File fmd = new File(path);
                fmd.mkdirs();
                hideImageFromGallery(path + File.separator);
                continue;
            }
            FileOutputStream fout = new FileOutputStream(location + File.separator + filename);

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            zis.closeEntry();
        }

        zis.close();

    }

    public static void decompress(String zipText, String location) throws IOException {
        byte[] compressed = Base64.decode(zipText, Base64.DEFAULT);
        InputStream is;
        ZipInputStream zis;

        String filename;
        is = new ByteArrayInputStream(compressed);
        zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();
            // Need to create directories if not exists, or
            // it will generate an Exception...
            if (ze.isDirectory()) {
                String path = location + File.separator + filename;
                File fmd = new File(path);
                fmd.mkdirs();
                hideImageFromGallery(path + File.separator);
                continue;
            }
            FileOutputStream fout = new FileOutputStream(location + File.separator + filename);

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            zis.closeEntry();
        }
        zis.close();
    }

    /**
     * Create .nomedia file in order to prevent gallery application shows this
     * folder into album
     *
     * @param path
     *            Local path
     *
     * @throws IOException
     *             if it's not possible to create the file.
     */
    public static void hideImageFromGallery(String path) throws IOException {
        String NOMEDIA = ".nomedia";
        File nomediaFile = new File(path + NOMEDIA);
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile();
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }
}
