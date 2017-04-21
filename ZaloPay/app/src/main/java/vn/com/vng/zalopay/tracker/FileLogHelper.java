package vn.com.vng.zalopay.tracker;

import android.text.TextUtils;

import com.zalopay.apploader.internal.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.filelog.FileLogStore;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by hieuvm on 4/21/17.
 * *
 */

public class FileLogHelper {

    private static final String ZIP_SUFFIX = ".zip";

    public static Observable<String[]> listFileLogs() {
        return listFileLogs(FileLog.sDirectoryFileLog, FileLog.sCurrentFile);
    }

    private static Observable<String[]> listFileLogs(File directory, File exclude) {
        return ObservableHelper.makeObservable(() -> {
            if (!directory.exists() || !directory.isDirectory()) {
                return new String[]{};
            }

            File[] files = directory.listFiles();
            List<String> ret = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        continue;
                    }

                    if (file.equals(exclude)) {
                        continue;
                    }

                    String fileName = file.getName();

                    if (!fileName.endsWith(ZIP_SUFFIX)) {
                        continue;
                    }

                    ret.add(file.getAbsolutePath());
                }
            }

            return ret.toArray(new String[ret.size()]);
        });
    }

    public static Observable<String> zipFileLog(String filePath) {
        return ObservableHelper.makeObservable(() -> {
            File file = new File(filePath);
            if (!file.exists()) {
                return "";
            }

            String zipFile = file.getAbsolutePath().replace(".txt", ZIP_SUFFIX);
            FileUtils.deleteFileAtPathSilently(zipFile);

            try {
                FileUtils.zip(new String[]{filePath}, zipFile);
                return zipFile;
            } catch (IOException e) {
                return "";
            }
        });
    }

    public static Observable<String> uploadFileLog(String filePath, FileLogStore.Repository fileLogRepository) {
        return FileLogHelper.zipFileLog(filePath)
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(fileLogRepository::uploadFileLog) // Upload file
                .doOnNext(FileUtils::deleteFileAtPathSilently) // Remove .zip
                .doOnNext(s -> FileUtils.deleteFileAtPathSilently(filePath)) // Remove .txt
                .doOnError(Timber::w);
    }

}
