package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.reflect.Type;

import okhttp3.Request;
import retrofit2.Response;
import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */

final class ReactNativeCallAdapter extends BaseCallAdapter {

    ReactNativeCallAdapter(Context context, int apiClientId, Type responseType, Scheduler scheduler) {
        super(context, apiClientId, responseType, scheduler, 0);
    }

    ReactNativeCallAdapter(Context context, int apiClientId, Type responseType, Scheduler scheduler, int retryNumber) {
        super(context, apiClientId, responseType, scheduler, retryNumber);
    }

    @NonNull
    protected <R> Observable<? extends R> makeObservableFromResponse(Request request, Response<R> response) {
        return Observable.just(response.body());
    }

    @Override
    protected <R> Observable<? extends R> handleServerResponseError(BaseResponse baseResponse) {
        return null;
    }
}