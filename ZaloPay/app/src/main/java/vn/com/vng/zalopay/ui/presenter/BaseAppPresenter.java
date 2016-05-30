package vn.com.vng.zalopay.ui.presenter;


import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.repository.PassportRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseAppPresenter {

    protected final String TAG = this.getClass().getSimpleName();

    protected final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();

    protected final PassportRepository passportRepository = AndroidApplication.instance().getAppComponent().passportRepository();
    protected final UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();

    protected final Context applicationContext = AndroidApplication.instance();

    protected void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                            subscriber.onCompleted();
                        } catch (Exception ex) {
                            try {
                                subscriber.onError(ex);
                            } catch (Exception ex2) {
                            }
                        }
                    }
                });
    }


    protected void clearData() {
        userConfig.clearConfig();
        userConfig.setCurrentUser(null);
        if (AndroidApplication.instance().getUserComponent() != null) {
            AndroidApplication.instance().releaseUserComponent();
        }
    }
}
