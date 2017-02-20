package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;

import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.ThrowToLoginScreenEvent;
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

    ZaloPayCallAdapter(Context context, int apiEventId, Type responseType, Scheduler scheduler) {
        super(context, apiEventId, responseType, scheduler);
    }

    @NonNull
    @Override
    protected <R> Observable<? extends R> handleServerResponseError(BaseResponse body, BaseResponse baseResponse) {
        if (baseResponse.isSessionExpired()) {
            TokenException exception = new TokenException(baseResponse.err, baseResponse.message);
            EventBus.getDefault().post(new ThrowToLoginScreenEvent(exception));
            return Observable.error(exception);
        } else if (baseResponse.isServerMaintain()) {
            ServerMaintainException exception = new ServerMaintainException(baseResponse.err, baseResponse.message);
            EventBus.getDefault().post(new ThrowToLoginScreenEvent(exception));
            return Observable.error(exception);
        } else if (baseResponse.isInvitationCode()) {
            return Observable.error(new InvitationCodeException(body.err, body));
        } else if (baseResponse.isAccountSuspended()) {
            AccountSuspendedException exception = new AccountSuspendedException(baseResponse.err, baseResponse.message);
            EventBus.getDefault().post(new ThrowToLoginScreenEvent(exception));
            return Observable.error(exception);
        } else {
            return Observable.error(new BodyException(body.err, body.message));
        }
    }
}
