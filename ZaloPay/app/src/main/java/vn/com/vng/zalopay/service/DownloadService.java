package vn.com.vng.zalopay.service;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.appresources.AbsDownloadService;

/**
 * Created by AnhHieu on 5/23/16.
 */
public class DownloadService extends AbsDownloadService {

    @Override
    public void doInject() {
        AndroidApplication.instance().getAppComponent().inject(this);
    }
}
