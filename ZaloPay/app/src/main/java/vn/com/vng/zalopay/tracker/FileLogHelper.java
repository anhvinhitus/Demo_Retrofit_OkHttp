package vn.com.vng.zalopay.tracker;

import android.text.TextUtils;

import com.zalopay.apploader.internal.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.filelog.FileLogStore;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.tracker.model.ApptransidLogData;

/**
 * Created by hieuvm on 4/21/17.
 * Static helper for filelog implementations
 */

public class FileLogHelper {

    private static final String ZIP_SUFFIX = ".zip";

    private static Observable<String[]> listFileLogs() {
        return listFileLogs(EventFileLog.Instance.getRootDirectory(),
                APIFailedFileLog.Instance.getCurrentFileLog(),
                EventFileLog.Instance.getCurrentFileLog(),
                PaymentConnectorErrorFileLog.Instance.getCurrentFileLog());
    }

    private static Observable<String[]> listFileLogs(File directory, File... exclude) {
        return ObservableHelper.makeObservable(() -> {
            if (!directory.exists() || !directory.isDirectory()) {
                return new String[]{};
            }

            File[] files = directory.listFiles();
            if (files == null) {
                return new String[]{};
            }

            List<String> ret = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                if (indexOf(file, exclude) >= 0) {
                    continue;
                }

                String fileName = file.getName();

                if (fileName.endsWith(ZIP_SUFFIX)) {
                    continue;
                }

                ret.add(file.getAbsolutePath());
            }

            Timber.d("filelogs should upload size [%s]", ret.size());

            return ret.toArray(new String[ret.size()]);
        });
    }

    private static int indexOf(File file, File... files) {
        if (files == null) {
            return -1;
        }

        int length = files.length;

        for (int i = 0; i < length; i++) {

            if (file != files[i]) {
                continue;
            }

            return i;
        }

        return -1;
    }

    private static Observable<String> zipFileLog(String filePath) {
        return ObservableHelper.makeObservable(() -> {
            File file = new File(filePath);
            if (!file.exists()) {
                return "";
            }

            String zipFilePath = file.getAbsolutePath().replace(".txt", ZIP_SUFFIX);
            File zipFile = new File(zipFilePath);

            if (zipFile.exists()) {
                return zipFilePath;
            }

            try {
                FileUtils.zip(new String[]{filePath}, zipFilePath);
                return zipFilePath;
            } catch (IOException e) {
                Timber.d(e, "Zip file error");
                return "";
            }
        });
    }

    public static Observable<String> uploadFileLog(String filePath, FileLogStore.Repository fileLogRepository) {
        return FileLogHelper.zipFileLog(filePath) // Zip file
                .filter(s -> !TextUtils.isEmpty(s) && FileUtils.existsAndIsFile(s))
                .flatMap(fileLogRepository::uploadFileLog) // Upload file
                .doOnNext(s -> FileUtils.deleteFileAtPathSilently(filePath)) // Remove .txt
                .doOnNext(FileUtils::deleteFileAtPathSilently) // Remove .zip
                ;
    }

    public static Observable<Boolean> uploadApptransidFileLog(FileLogStore.Repository fileLogRepository, ApptransidLogStore.Repository apptransidLogRepository) {
        return apptransidLogRepository.getWithStatusDone()
                .filter(logs -> logs != null && logs.length() > 0)
                .map(logs -> ApptransidFileLog.Instance.append(new ApptransidLogData(logs)))
                .flatMap(FileLogHelper::zipFileLog) // Zip file
                .filter(s -> !TextUtils.isEmpty(s) && FileUtils.existsAndIsFile(s))
                .flatMap(fileLogRepository::uploadFileLog) // Upload file
                .doOnNext(FileUtils::deleteFileAtPathSilently) // Remove .zip
                .doOnNext(s -> {
                    String txtFile = s.replace(ZIP_SUFFIX, ".txt");
                    FileUtils.deleteFileAtPathSilently(txtFile);
                }) // Remove .txt
                .flatMap(s -> apptransidLogRepository.removeWithStatusDone()) // Clear data in db
                ;
    }

    private static Observable<String> uploadFileLogIgnoreError(String path, FileLogStore.Repository fileLogRepository) {
        return FileLogHelper.uploadFileLog(path, fileLogRepository)
                .onErrorResumeNext(Observable.empty());
    }

    public static Observable<Boolean> uploadFileLogs(FileLogStore.Repository fileLogRepository) {
        return FileLogHelper.listFileLogs()
                .flatMap(Observable::from)
                .flatMap(path -> uploadFileLogIgnoreError(path, fileLogRepository))
                .map(s -> Boolean.TRUE);
    }

    public static Observable<Boolean> cleanupLogs() {
        return ObservableHelper.makeObservable(() -> {
            EventFileLog.Instance.cleanupLogs();
            return true;
        });
    }
}
