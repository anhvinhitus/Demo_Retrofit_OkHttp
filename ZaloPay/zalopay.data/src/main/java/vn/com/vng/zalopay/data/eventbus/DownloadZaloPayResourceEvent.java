package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by longlv on 3/17/17.
 * Event when download resource zalo pay successfully.
 */

public class DownloadZaloPayResourceEvent {

    public boolean isDownloadSuccess;

    public DownloadZaloPayResourceEvent(boolean isDownloadSuccess) {
        this.isDownloadSuccess = isDownloadSuccess;
    }
}
