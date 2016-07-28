package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.RedPacketNetworkErrorEnum;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.HttpEmptyResponseException;
import vn.com.vng.zalopay.data.exception.TokenException;

/**
 * Created by huuhoa on 7/4/16.
 * CallAdapter for pre-processing Server response
 */
final class RedPacketCallAdapter implements CallAdapter<Observable<?>> {
    private final Context mContext;
    private final Type mResponseType;
    private final Scheduler mScheduler;

    RedPacketCallAdapter(Context context, Type responseType, Scheduler scheduler) {
        this.mContext = context;
        this.mResponseType = responseType;
        this.mScheduler = scheduler;
    }

    @Override
    public Type responseType() {
        return mResponseType;
    }

    @Override
    public <R> Observable<R> adapt(Call<R> call) {
        Observable<R> observable = Observable.create(new CallOnSubscribe<>(mContext, call))
                .flatMap(this::makeObservableFromResponse);

        if (mScheduler == null) {
            return observable;
        }

        return observable.subscribeOn(mScheduler);
    }

    @NonNull
    private <R> Observable<? extends R> makeObservableFromResponse(Response<R> response) {
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
        if (!baseResponse.isSuccessfulResponse()) {
            return handleServerResponseError((BaseResponse) body, baseResponse);
        }

        // Happy case
        return Observable.just(body);
    }

    @NonNull
    private <R> Observable<? extends R> handleServerResponseError(BaseResponse body, BaseResponse baseResponse) {
        if (baseResponse.err == RedPacketNetworkErrorEnum.INVALID_ACCESS_TOKEN.getValue()) {
            EventBus.getDefault().post(new TokenExpiredEvent(baseResponse.err));
            return Observable.error(new TokenException());
        } else {
            return Observable.error(new BodyException(body.err, body.message));
        }
    }
}
