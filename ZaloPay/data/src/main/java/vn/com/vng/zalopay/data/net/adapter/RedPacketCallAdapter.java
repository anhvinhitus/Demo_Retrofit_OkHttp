package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;

import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.RedPacketNetworkErrorEnum;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.TokenException;

/**
 * Created by huuhoa on 7/4/16.
 * CallAdapter for pre-processing Server response
 */
final class RedPacketCallAdapter extends BaseCallAdapter {

    RedPacketCallAdapter(Context context, Type responseType, Scheduler scheduler) {
        super(context, responseType, scheduler);
    }

    @NonNull
    @Override
    protected <R> Observable<? extends R> handleServerResponseError(BaseResponse body, BaseResponse baseResponse) {
        if (baseResponse.err == RedPacketNetworkErrorEnum.INVALID_ACCESS_TOKEN.getValue()) {
            EventBus.getDefault().post(new TokenExpiredEvent(baseResponse.err));
            return Observable.error(new TokenException());
        } else {
            return Observable.error(new BodyException(body.err, body.message));
        }
    }
}
