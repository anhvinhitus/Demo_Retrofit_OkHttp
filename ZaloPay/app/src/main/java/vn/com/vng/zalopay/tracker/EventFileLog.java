package vn.com.vng.zalopay.tracker;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.event.UploadFileLogEvent;

/**
 * Created by hieuvm on 5/10/17.
 * *
 */

final class EventFileLog extends AbstractFileLog {

    static final EventFileLog Instance = new EventFileLog();

    private static final String FILE_NAME_FORMAT = "zalopay-logs-events-%s.txt";
    private static final String PREF_CREATE_FILE_TIME = "file_log_create_time";

    @Override
    String getPrefCreateFileTime() {
        return PREF_CREATE_FILE_TIME;
    }

    @Override
    String getFileNameFormat() {
        return FILE_NAME_FORMAT;
    }

    @Override
    protected void onWriteLogFinish(String path) {
        Timber.d("onWriteLogFinish: logPath %s", path);
        AndroidApplication.instance().getAppComponent()
                .eventBus().post(new UploadFileLogEvent(path));
    }
}
