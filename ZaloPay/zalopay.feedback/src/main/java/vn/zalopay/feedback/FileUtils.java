package vn.zalopay.feedback;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by hieuvm on 1/7/17.
 */

final class FileUtils {

    @Nullable
    static String writeStringToFile(Context context, String content, String fileName) {
        FileWriter out = null;
        try {
            File directory = new File(context.getCacheDir() + File.separator + "data");
            mkdirs(directory);
            File file = new File(directory, fileName);
            out = new FileWriter(file);
            out.write(content);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Timber.d(e, "writeStringToFile: ");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }

    static boolean mkdirs(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                return true;
            }
            directory.delete();
        }

        return !directory.mkdirs() && !directory.isDirectory();
    }
}
