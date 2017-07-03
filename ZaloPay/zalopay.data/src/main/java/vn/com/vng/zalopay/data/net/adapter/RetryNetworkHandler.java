package vn.com.vng.zalopay.data.net.adapter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by hieuvm on 7/3/17.
 * *
 */

final class RetryNetworkHandler implements
        Func1<Observable<? extends Throwable>, Observable<?>> {

    private static final int DEFAULT_RETRY_COUNT = 3;

    private final int mMaxRetries;
    private final int mRetryDelayMillis;
    private int mRetryCount;
    private final Request mRequest;

    RetryNetworkHandler(Request request) {
        this(request, DEFAULT_RETRY_COUNT, 0);
    }

    RetryNetworkHandler(Request request, int maxRetries, int retryDelayMillis) {
        mMaxRetries = maxRetries;
        mRetryDelayMillis = retryDelayMillis;
        mRequest = request;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts.flatMap(throwable -> {

            if (!mRequest.method().equalsIgnoreCase("GET")) {
                return Observable.error(throwable);
            }

            boolean needRetry = false;

            if (throwable instanceof IOException) {
                needRetry = true;
            } else if (throwable instanceof HttpException) {
                Timber.d("adapt ((HttpException) error).code() [%s]", ((HttpException) throwable).code());
                if (((HttpException) throwable).code() > 404) {
                    needRetry = true;
                }
            }

            if (!needRetry || ++mRetryCount >= mMaxRetries) {
                return Observable.error(throwable);
            }

            return Observable.timer(mRetryDelayMillis, TimeUnit.MILLISECONDS);
        });
    }
}