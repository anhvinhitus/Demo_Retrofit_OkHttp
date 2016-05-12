package vn.com.vng.zalopay.ui.presenter;


import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
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
}
