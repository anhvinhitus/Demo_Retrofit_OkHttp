package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;

import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.ServerMaintainEvent;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.exception.TokenException;

/**
 * Created by huuhoa on 7/4/16.
 * CallAdapter for pre-processing Server response
 */
final class ZaloPayCallAdapter extends BaseCallAdapter {

    ZaloPayCallAdapter(Context context, Type responseType, Scheduler scheduler) {
        super(context, responseType, scheduler);
    }

    @NonNull
    @Override
    protected <R> Observable<? extends R> handleServerResponseError(BaseResponse body, BaseResponse baseResponse) {
        if (baseResponse.isSessionExpired()) {
            EventBus.getDefault().post(new TokenExpiredEvent(baseResponse.err));
            return Observable.error(new TokenException(baseResponse.message));
        } else if (baseResponse.isServerMaintain()) {
            EventBus.getDefault().post(new ServerMaintainEvent(baseResponse.message));
            return Observable.error(new ServerMaintainException());
        } else if (baseResponse.isInvitationCode()) {
            return Observable.error(new InvitationCodeException(body.err, body));
        } else if (baseResponse.isAccountSuspended()) {
            return Observable.error(new AccountSuspendedException());
        } else {
            return Observable.error(new BodyException(body.err, body.message));
        }
    }
}
