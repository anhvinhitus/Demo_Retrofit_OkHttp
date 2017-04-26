package vn.com.vng.zalopay.tracker;

import android.support.annotation.NonNull;

import com.zalopay.apploader.internal.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.event.UploadFileLogEvent;

/**
 * Created by hieuvm on 4/21/17.
 * write tracker event to a file
 */

final class FileLog {

    static final FileLog Instance = new FileLog();

    private static final long INTERVAL_CREATE_FILE = 300000; //ms
    private static final String MSG_FORMAT = "%s,%s,%s,%s";
    private static final String FILE_NAME_FORMAT = "zalopay-logs-%s.txt";
    private static final String PREF_CREATE_FILE_TIME = "file_log_create_time";


    private final File sDirectoryFileLog;
    private final SimpleDateFormat sDateFormat;

    //
    private File sCurrentFile = null;
    private BufferedWriter sBufferedWriter;
    private Long mFileCreateTime;

    private FileLog() {
        sDirectoryFileLog = new File(AndroidApplication.instance().getFilesDir(), "logs");
        sDateFormat = new SimpleDateFormat("yyyyMMddhhmm", Locale.getDefault());
    }

    void append(LogData logData) {
        Timber.d("write log : eventType %s eventId %s value %s timestamp %s", logData.eventType, logData.eventId, logData.value, logData.timestamp);
        ensureFileLogReady(logData);
        writeToFile(logData);
    }

    private void ensureFileLogReady(LogData logData) {
        FileUtils.mkdirs(sDirectoryFileLog);
        long timestamp = logData.timestamp;
        long lastFileCreateTime = getFileCreateTime();
        if (Math.abs(timestamp - lastFileCreateTime) >= INTERVAL_CREATE_FILE) { // tạo file mới
            Timber.d("Create new file log");
            File oldFile = sCurrentFile;
            sCurrentFile = createFileLog(timestamp);

            setFileCreateTime(timestamp);
            closeWriter();
            createWriter(sCurrentFile);

            if (oldFile != null) {
                postFileLog(oldFile.getAbsolutePath());
            }

        } else {

            if (sBufferedWriter != null) {
                return;
            }

            if (sCurrentFile == null) {
                sCurrentFile = createFileLog(lastFileCreateTime);
            }

            createWriter(sCurrentFile);
        }
    }

    private void postFileLog(String path) {
        AndroidApplication.instance().getAppComponent()
                .eventBus().post(new UploadFileLogEvent(path));
    }

    private File createFileLog(long timestamp) {
        String fileName = String.format(Locale.getDefault(), FILE_NAME_FORMAT, sDateFormat.format(new Date(timestamp)));
        Timber.d("Create file log : name [%s]", fileName);
        return new File(sDirectoryFileLog, fileName);
    }

    private void setFileCreateTime(long timestamp) {
        mFileCreateTime = timestamp;
        AndroidApplication.instance().getAppComponent()
                .sharedPreferences().edit()
                .putLong(PREF_CREATE_FILE_TIME, timestamp)
                .apply();
    }

    private long getFileCreateTime() {
        if (mFileCreateTime == null) {
            mFileCreateTime = AndroidApplication.instance().getAppComponent()
                    .sharedPreferences().getLong(PREF_CREATE_FILE_TIME, 0);
        }
        return mFileCreateTime;
    }

    private void writeToFile(@NonNull LogData logData) {
        try {
            if (sBufferedWriter != null) {
                sBufferedWriter.write(formatMsg(logData));
                sBufferedWriter.newLine();
                sBufferedWriter.flush();
            }
        } catch (IOException e) {
            Timber.e(e, "Write file log");
        }
    }

    private void createWriter(File file) {
        try {
            sBufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Timber.e(e, "Create file write");
        }
    }

    private void closeWriter() {
        try {
            if (sBufferedWriter != null) {
                sBufferedWriter.newLine();
                sBufferedWriter.flush();
                sBufferedWriter.close();
                sBufferedWriter = null;
            }
        } catch (IOException e) {
            Timber.e(e, "Close file log");
        }
    }

    public void delete() {
        closeWriter();
        if (sCurrentFile != null && sCurrentFile.exists()) {
            sCurrentFile.delete();
        }
    }

    void cleanupLogs() {
        FileUtils.deleteDirectory(sDirectoryFileLog, false);
    }

    private String formatMsg(@NonNull LogData logData) {
        return String.format(Locale.getDefault(), MSG_FORMAT, logData.eventType, logData.eventId, logData.value == null ? "" : logData.value, logData.timestamp);
    }

    File getRootDirectory() {
        return sDirectoryFileLog;
    }

    File getCurrentFileLog() {
        return sCurrentFile;
    }

    static class LogData {

        LogData(int eventType, int eventId, Long eventValue, long timestamp) {
            this.eventType = eventType;
            this.eventId = eventId;
            this.value = eventValue;
            this.timestamp = timestamp;
        }

        int eventType;
        int eventId;
        public Long value;
        long timestamp;
    }

}
