package vn.com.vng.zalopay.service;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.AbsDownloadService;

/**
 * Created by AnhHieu on 5/23/16.
 * Download merchant resource;
 * Có 2 app ZALOPAY_APP_ID & WITHDRAW_APP_ID chứa config của zalopay.
 * Khi download thành công cần refresh lại config
 */
public class DownloadService extends AbsDownloadService {

    public DownloadService() {
        super();
        addResourceZaloPayInApp(BuildConfig.ZALOPAY_APP_ID);
        addResourceZaloPayInApp(BuildConfig.WITHDRAW_APP_ID);
    }

    @Override
    public void doInject() {
        AndroidApplication.instance().getAppComponent().inject(this);
    }
}
