package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;

import okhttp3.Request;
import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.RedPacketNetworkErrorEnum;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.ThrowToLoginScreenEvent;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.TokenException;

/**
 * Created by huuhoa on 7/4/16.
 * CallAdapter for pre-processing Server response
 */
final class RedPacketCallAdapter extends BaseCallAdapter {

    RedPacketCallAdapter(Context context, int httpsApiId, int connectorApiId, Type responseType, Scheduler scheduler) {
        super(context, httpsApiId, connectorApiId, responseType, scheduler);
    }

    @Override
    <R> Observable<? extends R> handleServerResponseError(Request request, BaseResponse baseResponse) {
        trackAPIError(request.url().encodedPath().replaceFirst("/", ""), baseResponse.err);
        if (baseResponse.err == RedPacketNetworkErrorEnum.INVALID_ACCESS_TOKEN.getValue()) {
            TokenException exception = new TokenException(baseResponse.err, baseResponse.message);
            EventBus.getDefault().postSticky(new ThrowToLoginScreenEvent(exception));
            return Observable.error(exception);
        } else {
            return Observable.error(new BodyException(baseResponse.err, baseResponse.message));
        }
    }
}
