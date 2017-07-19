package vn.com.vng.zalopay.data.eventbus;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.data.appresources.DownloadInfo;

/**
 * Created by longlv on 3/17/17.
 * Event when download resource zalo pay successfully.
 */

public class DownloadZaloPayResourceEvent {

    @NonNull
    public DownloadInfo mDownloadInfo;

    public DownloadZaloPayResourceEvent(@NonNull DownloadInfo mDownloadInfo) {
        this.mDownloadInfo = mDownloadInfo;
    }
}
