package vn.com.vng.zalopay.ui.presenter;


import org.greenrobot.eventbus.EventBus;

import rx.Subscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.repository.PassportRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseAppPresenter {

    protected final String TAG = this.getClass().getSimpleName();

    protected final EventBus eventBus = EventBus.getDefault();

    protected PassportRepository passportRepository = AndroidApplication.instance().getAppComponent().passportRepository();


    protected void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
