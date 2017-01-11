package vn.com.vng.zalopay.data.eventbus;

import vn.com.vng.zalopay.data.appresources.DownloadInfo;

/**
 * Created by longlv on 1/11/17.
 * *
 */

public class DownloadAppEvent {

    public boolean isDownloadSuccess;
    public DownloadInfo mDownloadInfo;

    public DownloadAppEvent(boolean isDownloadSuccess, DownloadInfo downloadInfo) {
        this.isDownloadSuccess = isDownloadSuccess;
        mDownloadInfo = downloadInfo;
    }
}
