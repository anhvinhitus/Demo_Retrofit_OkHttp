package vn.com.vng.zalopay.data.repository;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by AnhHieu on 5/18/16.
 */
public class BaseRepository {

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
