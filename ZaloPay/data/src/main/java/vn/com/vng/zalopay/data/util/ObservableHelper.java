package vn.com.vng.zalopay.data.util;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;

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
