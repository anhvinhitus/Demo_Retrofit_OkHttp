package vn.com.vng.zalopay.service;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.eventbus.DownloadZaloPayResourceEvent;
import vn.com.vng.zalopay.utils.ConfigUtil;

/**
 * Created by hieuvm on 4/10/17.
 * *
 */

@Singleton
public class GlobalAppService {
    private final EventBus mEventBus;

    @Inject
    public GlobalAppService(EventBus eventBus) {
        mEventBus = eventBus;
        mEventBus.register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDownloadResourceSuccessEvent(DownloadZaloPayResourceEvent event) {

        Timber.d("on Download app 1 resource success : url [%s]", event.mDownloadInfo.url);

        if (!event.isDownloadSuccess) {
            return;
        }

        ConfigUtil.loadConfigFromResource();
        AndroidApplication.instance().initIconFont();
    }

}
