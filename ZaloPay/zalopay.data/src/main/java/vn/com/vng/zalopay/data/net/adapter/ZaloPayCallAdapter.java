package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.HashSet;

import okhttp3.Request;
import rx.Observable;
import rx.Scheduler;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
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
class ZaloPayCallAdapter extends BaseCallAdapter {

    private static final HashSet<String> IGNORE_API_MAINTAIN = new HashSet<>();

    static {
        IGNORE_API_MAINTAIN.add(Constants.UMUPLOAD_API.FILE_LOG);
        IGNORE_API_MAINTAIN.add("v001/tpe/sdkwriteatmtime");
        IGNORE_API_MAINTAIN.add("v001/tpe/sdkerrorreport");
    }

    ZaloPayCallAdapter(Context context, int httpsApiId, int connectorApiId, Type responseType, Scheduler scheduler) {
        super(context, httpsApiId, connectorApiId, responseType, scheduler);
    }

    @Override
    <R> Observable<? extends R> handleServerResponseError(Request request, BaseResponse baseResponse) {

        boolean skipTracking = false;
        try {
            if (baseResponse.isSessionExpired()) {
                TokenException exception = new TokenException(baseResponse.err, baseResponse.message);
                postThrowToLoginScreenEvent(exception);
                return Observable.error(exception);
            } else if (baseResponse.isServerMaintain()) {
                String path = request.url().encodedPath().replaceFirst("/", "");
                ServerMaintainException exception = new ServerMaintainException(baseResponse.err, baseResponse.message);
                if (IGNORE_API_MAINTAIN.contains(path)) {
                    skipTracking = true;
                    return Observable.error(exception);
                }

                postThrowToLoginScreenEvent(exception);
                return Observable.error(exception);
            } else if (baseResponse.isInvitationCode()) {
                return Observable.error(new InvitationCodeException(baseResponse.err, baseResponse));
            } else if (baseResponse.isAccountSuspended()) {
                AccountSuspendedException exception = new AccountSuspendedException(baseResponse.err, baseResponse.message);
                postThrowToLoginScreenEvent(exception);
                return Observable.error(exception);
            } else {
                return Observable.error(new BodyException(baseResponse.err, baseResponse.message));
            }
        } finally {
            if (!skipTracking) {
                trackAPIError(request.url().encodedPath().replaceFirst("/", ""), baseResponse.err);
            }
        }
    }

    private void postThrowToLoginScreenEvent(BodyException exception) {
        Timber.d("Post ThrowToLoginScreenEvent");
        EventBus.getDefault().post(new ThrowToLoginScreenEvent(exception));
    }
}
