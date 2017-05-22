
package vn.com.vng.zalopay.exception;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import retrofit2.adapter.rxjava.HttpException;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.GenericException;
import vn.com.vng.zalopay.data.exception.StringResGenericException;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.User;

public class ErrorMessageFactory {

    private ErrorMessageFactory() {
        //empty
    }

    public static String create(Context context, Throwable exception) {
        if (context == null) {
            return null;
        }

        String message = null;

        if (exception instanceof NetworkConnectionException) {
            message = context.getString(R.string.exception_no_connection);
        } else if (exception instanceof TokenException) {
            message = context.getString(R.string.exception_token_expired_message);
        } else if (exception instanceof BodyException) {
            message = exception.getMessage();
            if (TextUtils.isEmpty(message)) {
                message = ServerErrorMessage.getMessage(context, ((BodyException) exception).errorCode);
            }
        } else if (exception instanceof SocketTimeoutException
                || exception instanceof TimeoutException) {
            message = context.getString(R.string.exception_timeout_message);
        } else if (exception instanceof HttpException) {

            if (((HttpException) exception).code() == 404) {
                message = context.getString(R.string.exception_http_404);
            } else {
                message = context.getString(R.string.exception_server_error);
            }

        } else if (exception instanceof UnknownHostException) {
            message = context.getString(R.string.exception_unknown_host);
        } else if (exception instanceof SSLHandshakeException) {
            message = context.getString(R.string.exception_no_connection);
        } else if (exception instanceof SSLPeerUnverifiedException) {
            message = context.getString(R.string.exception_no_connection);
        } else if (exception instanceof GenericException) {
            message = exception.getMessage();
        } else if (exception instanceof StringResGenericException) {
            message = context.getString(((StringResGenericException) exception).mMessageRes);
        }

        if (TextUtils.isEmpty(message)) {
            message = context.getString(R.string.exception_generic);
        }

        return message;
    }

    public static String create(@NonNull Context context, @NonNull Throwable exception, @Nullable User user) {
        if (user == null) {
            return create(context, exception);
        }

        if (exception instanceof AccountSuspendedException) {
            String format = context.getString(R.string.exception_user_is_locked_format);
            if (!TextUtils.isEmpty(user.zalopayname)) {
                return String.format(format, user.zalopayname);
            } else if (user.phonenumber > 0) {
                return String.format(format, PhoneUtil.formatPhoneNumber(user.phonenumber));
            }
        }

        return create(context, exception);
    }
}
