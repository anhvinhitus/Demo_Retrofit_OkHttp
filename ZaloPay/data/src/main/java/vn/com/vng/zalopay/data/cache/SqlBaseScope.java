package vn.com.vng.zalopay.data.cache;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by AnhHieu on 5/5/16.
 */
public class SqlBaseScope {

    protected final DaoSession daoSession;

    public SqlBaseScope(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    public DaoSession getDaoSession() {
        return daoSession;
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
