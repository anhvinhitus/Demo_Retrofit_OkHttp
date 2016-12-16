package vn.com.vng.zalopay.data.util;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by huuhoa on 6/14/16.
 * Static helper for repository implementations
 */
public class ObservableHelper {
    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            T ret = func.call();

                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(ret);
                                subscriber.onCompleted();
                            }

                        } catch (Exception ex) {
                            try {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onError(ex);
                                }
                            } catch (Exception ex2) {
                                //empty
                            }
                        }
                    }
                });
    }

    private final static Observable.Transformer schedulersTransformer =
            observable -> ((Observable) observable).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) schedulersTransformer;
    }
}
