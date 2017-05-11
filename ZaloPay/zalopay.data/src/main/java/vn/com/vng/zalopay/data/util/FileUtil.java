package vn.com.vng.zalopay.data.util;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import timber.log.Timber;

/**
 * Created by AnhHieu on 5/21/16.
 */
public class FileUtil {
    public static void decompress(byte[] compressed, String location) throws Exception {
        InputStream is = null;
        ZipInputStream zis = null;
        try {
            File destinationFolder = new File(location);
            destinationFolder.mkdirs();

            String filename;
            is = new ByteArrayInputStream(compressed);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                filename = Strings.stripLeadingPath(filename);

                // Need to create directories if not exists, or
                // it will generate an Exception...
                String path = location + File.separator + filename;
                if (ze.isDirectory()) {
                    File fmd = new File(path);
                    fmd.mkdirs();

                    hideImageFromGallery(path + File.separator);

                    continue;
                } else {
                    File file = new File(path);
                    File parent = file.getParentFile();
                    parent.mkdirs();
                }

                FileOutputStream fout = new FileOutputStream(path);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

        } finally {
            try {
                if (zis != null) {
                    zis.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //emtpty
            }
        }
    }

    /**
     * Create .nomedia file in order to prevent gallery application shows this
     * folder into album
     *
     * @param path Local path
     * @throws IOException if it's not possible to create the file.
     */
    public static void hideImageFromGallery(String path) throws IOException {
        String NOMEDIA = ".nomedia";
        File nomediaFile = new File(path + NOMEDIA);
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile();
        }
    }

    public static File ensureDirectory(String path) {
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static String readAssetToString(AssetManager assetManager, String fileName) throws IOException {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = assetManager.open(fileName);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line;
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null) {
                    fIn.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                Log.d("FileUtil", "Close stream throw exception:" + e.getMessage());
            }
        }
        return returnString.toString();
    }

    public static String readFileToString(String filePath) throws IOException {
        FileInputStream fin = null;
        BufferedReader reader = null;
        try {
            File fl = new File(filePath);
            fin = new FileInputStream(fl);
            reader = new BufferedReader(new InputStreamReader(fin));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                Timber.d(e, "Close stream throw exception");
            }
        }
    }
}
