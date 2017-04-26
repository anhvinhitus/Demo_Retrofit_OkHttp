package vn.com.vng.zalopay.data.net.adapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by hieuvm on 4/22/17.
 * Sử dụng để retry upload filelog
 */

public class RetryFileLogUpload implements
        Func1<Observable<? extends Throwable>, Observable<?>> {

    private static final int DEFAULT_RETRY_COUNT = 3;

    private final int mMaxRetries;
    private final int mRetryDelayMillis;
    private int mRetryCount;

    public RetryFileLogUpload() {
        this.mMaxRetries = DEFAULT_RETRY_COUNT;
        this.mRetryDelayMillis = 0;
        this.mRetryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts.flatMap(throwable -> {

            if (!(throwable instanceof TimeoutException)) {
                return Observable.error(throwable);
            }

            if (++mRetryCount >= mMaxRetries) {
                return Observable.error(throwable);
            }

            return Observable.timer(mRetryDelayMillis, TimeUnit.MILLISECONDS);
        });
    }
}