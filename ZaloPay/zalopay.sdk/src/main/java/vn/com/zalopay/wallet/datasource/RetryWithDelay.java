package vn.com.zalopay.wallet.datasource;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

public class RetryWithDelay implements Func1<Observable<? extends Throwable>, Observable<?>> {
    protected final int maxRetries;
    protected final long retryDelayMillis;
    protected int retryCount;

    public RetryWithDelay(final int maxRetries, final long retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts.flatMap((Func1<Throwable, Observable<?>>) throwable -> {
            if (++retryCount < maxRetries) {
                return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
            }
            // Max retries hit. Just pass the error along.
            return Observable.error(throwable);
        });
    }
}
