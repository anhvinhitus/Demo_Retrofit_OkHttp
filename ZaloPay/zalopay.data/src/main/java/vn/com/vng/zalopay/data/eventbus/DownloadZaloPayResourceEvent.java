package vn.com.vng.zalopay.data.eventbus;

import vn.com.vng.zalopay.data.appresources.DownloadInfo;

/**
 * Created by longlv on 3/17/17.
 * Event when download resource zalo pay successfully.
 */

public class DownloadZaloPayResourceEvent {

    public boolean isDownloadSuccess;
    public DownloadInfo mDownloadInfo;

    public DownloadZaloPayResourceEvent(boolean isDownloadSuccess, DownloadInfo mDownloadInfo) {
        this.isDownloadSuccess = isDownloadSuccess;
        this.mDownloadInfo = mDownloadInfo;
    }
}
