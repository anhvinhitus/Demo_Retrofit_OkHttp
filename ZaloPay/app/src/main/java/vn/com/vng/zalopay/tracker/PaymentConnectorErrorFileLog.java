package vn.com.vng.zalopay.tracker;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.event.UploadFileLogEvent;

/**
 * Created by hieuvm on 5/24/17.
 * Log lỗi notification server
 */

final class PaymentConnectorErrorFileLog extends AbstractFileLog {

    static final PaymentConnectorErrorFileLog Instance = new PaymentConnectorErrorFileLog();

    private static final String FILE_NAME_FORMAT = "zalopay-logs-pc-errors-%s.txt";
    private static final String PREF_CREATE_FILE_TIME = "pc_errors_create_time";

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
