package vn.com.vng.zalopay.tracker;

import android.os.Environment;
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

    private static final long INTERVAL_CREATE_FILE = 1 * 60 * 1000;// 300000; //ms
    private static final String MSG_FORMAT = "%s,%s,%s,%s";
    private static final String FILE_NAME_FORMAT = "zalopay-logs-%s.txt";
    private static final String PREF_CREATE_FILE_TIME = "file_log_create_time";
     static final File sDirectoryFileLog;
    private static final SimpleDateFormat sDateFormat;

    //
    private static File sCurrentFile = null;
    private static BufferedWriter sBufferedWriter;
    private static Long mFileCreateTime;

    static {
        //sDirectoryFileLog = new File(AndroidApplication.instance().getFilesDir(), "logs");
        sDirectoryFileLog = new File(Environment.getExternalStorageDirectory(), "logs");
        sDateFormat = new SimpleDateFormat("yyyyMMddhhmm", Locale.getDefault());
    }

    private FileLog() {
    }


    static void append(LogData logData) {
        Timber.d("write log : eventType %s eventId %s value %s timestamp %s", logData.eventType, logData.eventId, logData.value, logData.timestamp);
        ensureFileLogReady(logData);
        writeToFile(logData);
    }

    private static void ensureFileLogReady(LogData logData) {
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

    private static void postFileLog(String path) {
        AndroidApplication.instance().getAppComponent()
                .eventBus().post(new UploadFileLogEvent(path));
    }

    private static File createFileLog(long timestamp) {
        String fileName = String.format(Locale.getDefault(), FILE_NAME_FORMAT, sDateFormat.format(new Date(timestamp)));
        Timber.d("Create file log : name [%s]", fileName);
        return new File(sDirectoryFileLog, fileName);
    }

    private static void setFileCreateTime(long timestamp) {
        mFileCreateTime = timestamp;
        AndroidApplication.instance().getAppComponent()
                .sharedPreferences().edit()
                .putLong(PREF_CREATE_FILE_TIME, timestamp)
                .apply();
    }

    private static long getFileCreateTime() {
        if (mFileCreateTime == null) {
            mFileCreateTime = AndroidApplication.instance().getAppComponent()
                    .sharedPreferences().getLong(PREF_CREATE_FILE_TIME, 0);
        }
        return mFileCreateTime;
    }

    private static void writeToFile(@NonNull LogData logData) {
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

    private static void createWriter(File file) {
        try {
            sBufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Timber.e(e, "Create file write");
        }
    }

    private static void closeWriter() {
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

    public static void delete() {
        closeWriter();
        if (sCurrentFile != null && sCurrentFile.exists()) {
            sCurrentFile.delete();
        }
    }

    public static void cleanupLogs() {
        FileUtils.deleteDirectory(sDirectoryFileLog, false);
    }

    private static String formatMsg(@NonNull LogData logData) {
        return String.format(Locale.getDefault(), MSG_FORMAT, logData.eventType, logData.eventId, logData.value == null ? "" : logData.value, logData.timestamp);
    }

    static class LogData {

        LogData(int eventType, int eventId, Long eventValue, long timestamp) {
            this.eventType = eventType;
            this.eventId = eventId;
            this.value = eventValue;
            this.timestamp = timestamp;
        }

        public int eventType;
        public int eventId;
        public Long value;
        public long timestamp;
    }

}
