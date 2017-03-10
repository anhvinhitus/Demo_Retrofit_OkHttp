package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Scheduler;
import timber.log.Timber;

/**
 * Created by hieuvm on 3/10/17.
 */

public class ConnectorCallAdapter extends ZaloPayCallAdapter {

    public ConnectorCallAdapter(Context context, int apiEventId, Type responseType, Scheduler scheduler) {
        super(context, apiEventId, responseType, scheduler);
    }

    @Override
    public <R> Observable<R> adapt(Call<R> call) {
        mRestRetryCount = NUMBER_RETRY_REST;
        Observable<R> observable = Observable.create(new ConnectorCallOnSubscribe<>(mContext, call, mApiEventId))
                .retryWhen(errors -> errors.flatMap(error -> {
                    if (!call.request().method().equalsIgnoreCase("GET")) {
                        return Observable.error(error);
                    }

                    Timber.d("adapt mRestRetryCount [%s] error [%s]", mRestRetryCount, error);
                    boolean needRetry = false;
                    if (mRestRetryCount >= 1) {
                        if (error instanceof IOException) {
                            needRetry = true;
                        } else if (error instanceof HttpException) {
                            Timber.d("adapt ((HttpException) error).code() [%s]", ((HttpException) error).code());
                            if (((HttpException) error).code() > 404) {
                                needRetry = true;
                            }
                        }
                    }

                    if (needRetry) {
                        mRestRetryCount--;
                        return Observable.just(null);
                    } else {
                        return Observable.error(error);
                    }
                }))
                .flatMap(this::makeObservableFromResponse);
        if (mScheduler == null) {
            return observable;
        }

        return observable.subscribeOn(mScheduler);
    }
}
