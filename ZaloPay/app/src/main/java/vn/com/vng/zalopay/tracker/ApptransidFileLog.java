package vn.com.vng.zalopay.tracker;

import com.zalopay.apploader.internal.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.tracker.model.ApptransidLogData;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by khattn on 4/28/17.
 * write tracker apptransid event to a file
 */

class ApptransidFileLog {

    static final ApptransidFileLog Instance = new ApptransidFileLog();
    private static final String FILE_NAME_FORMAT = "zpv2-zalopay-logs-apptransid-%s.txt";

    private final File mDirectoryFileLog;
    private final SimpleDateFormat mDateFormat;

    private ApptransidFileLog() {
        mDirectoryFileLog = new File(AndroidApplication.instance().getFilesDir(), "logs");
        mDateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
    }

    String append(ApptransidLogData logData) {
        try {
            FileUtils.mkdirs(mDirectoryFileLog);
            File mCurrentFile = createFileLog(System.currentTimeMillis());
            AndroidUtils.writeToFile(logData.getMessage(), mCurrentFile.getAbsolutePath());
            return mCurrentFile.getAbsolutePath();
        } catch (IOException e) {
            Timber.d(e, "Fail to append file");
            return "";
        }
    }

    private File createFileLog(long timestamp) {
        String fileName = String.format(Locale.getDefault(), FILE_NAME_FORMAT, mDateFormat.format(new Date(timestamp)));
        Timber.d("Create file log : name [%s]", fileName);
        return new File(mDirectoryFileLog, fileName);
    }
}
