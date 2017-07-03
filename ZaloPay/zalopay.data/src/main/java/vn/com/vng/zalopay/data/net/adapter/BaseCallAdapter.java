package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Scheduler;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.network.CallOnSubscribe;
import vn.com.vng.zalopay.network.exception.HttpEmptyResponseException;
import vn.com.zalopay.analytics.ZPAnalytics;

/**
 * Created by longlv on 08/08/2016.
 * BaseCallAdapter for retry API request when has request error
 */
abstract class BaseCallAdapter implements CallAdapter<Observable<?>> {

    private final int mMaxRetries;
    protected final Context mContext;
    private final int mHttpsApiId;
    private final int mConnectorApiId;
    private final Type mResponseType;
    private final Scheduler mScheduler;
    private int mRestRetryCount;

    BaseCallAdapter(Context context, int httpsApiId, int connectorApiId, Type responseType, Scheduler scheduler) {
        this(context, httpsApiId, connectorApiId, responseType, scheduler, Constants.NUMBER_RETRY_REST);
    }

    BaseCallAdapter(Context context, int httpsApiId, int connectorApiId, Type responseType, Scheduler scheduler, int retryNumber) {
        this.mContext = context;
        this.mHttpsApiId = httpsApiId;
        this.mConnectorApiId = connectorApiId;
        this.mResponseType = responseType;
        this.mScheduler = scheduler;
        this.mMaxRetries = retryNumber >= 0 ? retryNumber : 0;
    }

    @Override
    public Type responseType() {
        return mResponseType;
    }

    @Override
    public <R> Observable<R> adapt(Call<R> call) {
        mRestRetryCount = mMaxRetries;
        Observable<R> observable = Observable.create(new CallOnSubscribe<>(mContext, call, mHttpsApiId, mConnectorApiId))
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
                .flatMap(response -> makeObservableFromResponse(call.request(), response));
        if (mScheduler == null) {
            return observable;
        }

        return observable.subscribeOn(mScheduler);
    }

    @NonNull
    protected <R> Observable<? extends R> makeObservableFromResponse(Request request, Response<R> response) {
        Timber.d("makeObservableFromResponse response [%s]", response);
        if (response == null) {
            return Observable.error(new HttpEmptyResponseException());
        }

        if (!response.isSuccessful()) {
            return Observable.error(new HttpException(response));
        }

        R body = response.body();
        if (!(body instanceof BaseResponse)) {
            // just return as is without further processing
            // if server's response is not with agreed format
            return Observable.just(body);
        }

        BaseResponse baseResponse = (BaseResponse) body;

        if (!TextUtils.isEmpty(baseResponse.accesstoken)) {
            EventBus.getDefault().post(new NewSessionEvent(baseResponse.accesstoken));
        }

        if (!baseResponse.isSuccessfulResponse()) {
            ZPAnalytics.trackAPIError(request.url().encodedPath().replaceFirst("/", ""), 0, baseResponse.err, 0);
            return handleServerResponseError(baseResponse);
        }

        // Happy case
        return Observable.just(body);
    }

    protected abstract <R> Observable<? extends R> handleServerResponseError(BaseResponse baseResponse);
}
