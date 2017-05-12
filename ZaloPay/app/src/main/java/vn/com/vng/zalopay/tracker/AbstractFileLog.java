package vn.com.vng.zalopay.tracker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import vn.com.vng.zalopay.tracker.model.AbstractLogData;

/**
 * Created by hieuvm on 4/21/17.
 * write tracker event to a file
 */

abstract class AbstractFileLog {

    abstract String getPrefCreateFileTime();

    abstract String getFileNameFormat();

    private static final long INTERVAL_CREATE_FILE = 300000; //ms

    private final File mDirectoryFileLog;
    private final SimpleDateFormat mDateFormat;

    //
    private File mCurrentFile = null;
    private BufferedWriter mBufferedWriter;
    private Long mFileCreateTime;

    AbstractFileLog() {
        mDirectoryFileLog = new File(AndroidApplication.instance().getFilesDir(), "logs");
        mDateFormat = new SimpleDateFormat("yyyyMMddhhmm", Locale.getDefault());
    }

    void append(AbstractLogData logData) {
        Timber.d("write log : eventType %s message %s", logData.eventType, logData.getMessage());
        ensureFileLogReady(logData);
        writeToFile(logData);
    }

    private void ensureFileLogReady(AbstractLogData logData) {
        FileUtils.mkdirs(mDirectoryFileLog);
        long timestamp = logData.timestamp;
        long lastFileCreateTime = getFileCreateTime();
        if (Math.abs(timestamp - lastFileCreateTime) >= INTERVAL_CREATE_FILE) { // tạo file mới
            Timber.d("Create new file log");
            File oldFile = mCurrentFile;
            mCurrentFile = createFileLog(timestamp);

            setFileCreateTime(timestamp);
            closeWriter();
            createWriter(mCurrentFile);

            if (oldFile != null) {
                onWriteLogFinish(oldFile.getAbsolutePath());
            }

        } else {

            if (mBufferedWriter != null) {
                return;
            }

            if (mCurrentFile == null) {
                mCurrentFile = createFileLog(lastFileCreateTime);
            }

            createWriter(mCurrentFile);
        }
    }

    protected void onWriteLogFinish(String filePath) {
        //upload file log
    }

    private File createFileLog(long timestamp) {
        String fileName = String.format(Locale.getDefault(), getFileNameFormat(), mDateFormat.format(new Date(timestamp)));
        Timber.d("Create file log : name [%s]", fileName);
        return new File(mDirectoryFileLog, fileName);
    }

    private void setFileCreateTime(long timestamp) {
        mFileCreateTime = timestamp;
        AndroidApplication.instance().getAppComponent()
                .sharedPreferences().edit()
                .putLong(getPrefCreateFileTime(), timestamp)
                .apply();
    }

    private long getFileCreateTime() {
        if (mFileCreateTime == null) {
            mFileCreateTime = AndroidApplication.instance().getAppComponent()
                    .sharedPreferences().getLong(getPrefCreateFileTime(), 0);
        }
        return mFileCreateTime;
    }

    private void writeToFile(@NonNull AbstractLogData logData) {
        try {
            if (mBufferedWriter != null) {
                mBufferedWriter.write(logData.getMessage());
                mBufferedWriter.newLine();
                mBufferedWriter.flush();
            }
        } catch (IOException e) {
            Timber.e(e, "Write file log");
        }
    }

    private void createWriter(File file) {
        try {
            mBufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Timber.e(e, "Create file write");
        }
    }

    private void closeWriter() {
        try {
            if (mBufferedWriter != null) {
                mBufferedWriter.newLine();
                mBufferedWriter.flush();
                mBufferedWriter.close();
                mBufferedWriter = null;
            }
        } catch (IOException e) {
            Timber.e(e, "Close file log");
        }
    }

    public void delete() {
        closeWriter();
        if (mCurrentFile != null && mCurrentFile.exists()) {
            mCurrentFile.delete();
        }
    }

    void cleanupLogs() {
        FileUtils.deleteDirectory(mDirectoryFileLog, false);
    }

    File getRootDirectory() {
        return mDirectoryFileLog;
    }

    @Nullable
    File getCurrentFileLog() {
        return mCurrentFile;
    }

}
