package vn.com.vng.zalopay.utils;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by huuhoa on 1/12/17.
 * Log writer for Zalo Pay Analytics
 */

public class ZaloPayEventLogWriter implements ZPTracker {
    private static final int EVENTTYPE_EVENT = 1;
    private static final int EVENTTYPE_TIMING = 2;
    private FileWriter mFileWriter;
    private BufferedWriter mBufferWriter;
    private PrintWriter mPrintWriter;
    private String mCurrentFileName;
    private Context mApplicationContext;
    private long mMaxTime = 5 * 60 * 1000;
    private long mLastLogEvent = 0;

    public ZaloPayEventLogWriter(Context context) {
        mApplicationContext = context;
        String logPathName = mApplicationContext.getCacheDir().getAbsolutePath() +
                File.separator + "logs";
        File logDir = new File(logPathName);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        mLastLogEvent = 0;
    }

    private String getFileName() {
        long currentTimeStamp = System.currentTimeMillis();
        if (currentTimeStamp - mLastLogEvent < mMaxTime) {
            return mCurrentFileName;
        }
        mLastLogEvent = currentTimeStamp;

        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmm");
        String fileName = format.format(currentTime);
        Timber.d("New logFile: %s", fileName);

        return mApplicationContext.getCacheDir().getAbsolutePath() +
                File.separator + "logs" +
                File.separator + "zalopay-" + fileName + ".txt";
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {
        log(EVENTTYPE_EVENT, eventId, eventValue != null ? eventValue : 0);
    }

    @Override
    public void trackScreen(String screenName) {

    }

    @Override
    public void trackTiming(int eventId, long value) {
        log(EVENTTYPE_TIMING, eventId, value);
    }

    @Override
    public void trackApptransidEvent(String apptransid, int appid, int step, int step_result,
                                     int pcmid, int transtype, long transid, int sdk_result,
                                     int server_result, String source) {

    }

    private void log(int eventType, int eventId, long value) {
        String lineValue = String.format("%d, %d, %d, %d", eventType, eventId, value, System.currentTimeMillis());
        String expectedFileName = getFileName();
        if (expectedFileName.equalsIgnoreCase(mCurrentFileName)) {
            // use current file
            mPrintWriter.println(lineValue);
            return;
        }

        try {
            if (mPrintWriter != null) {
                mPrintWriter.close();
            }

            mCurrentFileName = expectedFileName;
            mFileWriter = new FileWriter(mCurrentFileName, true);
            mBufferWriter = new BufferedWriter(mFileWriter);
            mPrintWriter = new PrintWriter(mBufferWriter);

            mPrintWriter.println(lineValue);
        } catch (IOException e) {
            Timber.e(e, "Exception while append log");
            mCurrentFileName = null;
            mFileWriter = null;
            mPrintWriter = null;
            mBufferWriter = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            if (mPrintWriter != null) {
                mPrintWriter.close();
            }
            mPrintWriter = null;
        } catch (Exception e) {
            Timber.d(e, "Exception while finalizing ZaloPayEventLogWriter");
        }
    }
}
