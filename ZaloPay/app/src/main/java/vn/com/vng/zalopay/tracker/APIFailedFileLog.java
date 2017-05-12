package vn.com.vng.zalopay.tracker;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.event.UploadFileLogEvent;

/**
 * Created by hieuvm on 5/10/17.
 * *
 */

final class APIFailedFileLog extends AbstractFileLog {

    static final APIFailedFileLog Instance = new APIFailedFileLog();

    private static final String FILE_NAME_FORMAT = "zalopay-logs-api-errors-%s.txt";
    private static final String PREF_CREATE_FILE_TIME = "api_errors_create_time";

    @Override
    String getPrefCreateFileTime() {
        return PREF_CREATE_FILE_TIME;
    }

    @Override
    String getFileNameFormat() {
        return FILE_NAME_FORMAT;
    }

    @Override
    protected void onWriteLogFinish(String filePath) {
        Timber.d("onWriteLogFinish: logPath %s", filePath);

        AndroidApplication.instance().getAppComponent()
                .eventBus().post(new UploadFileLogEvent(filePath));
    }
}
