
package vn.com.vng.zalopay.exception;

import android.content.Context;
import android.text.TextUtils;

import com.zalopay.ui.widget.errorview.HttpStatusCodes;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import retrofit2.adapter.rxjava.HttpException;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.GenericException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.exception.TokenException;

public class ErrorMessageFactory {

    private ErrorMessageFactory() {
        //empty
    }

    private final static Map<Integer, String> mHttpStatusCode;

    static {
        mHttpStatusCode = HttpStatusCodes.getCodesMap();
    }


    public static String create(Context context, Throwable exception) {
        if (context == null) {
            return null;
        }
        String message = context.getString(R.string.exception_generic);

        if (exception instanceof NetworkConnectionException) {
            message = context.getString(R.string.exception_no_connection);
        } else if (exception instanceof BodyException) {
            message = exception.getMessage();
            if (TextUtils.isEmpty(message)) {
                message = NetworkError.getMessage(context, ((BodyException) exception).errorCode);
            }
        } else if (exception instanceof TokenException) {
            //message = context.getString(R.string.exception_token_expired_message);
            message = null;
        } else if (exception instanceof SocketTimeoutException
                || exception instanceof TimeoutException) {
            message = context.getString(R.string.exception_timeout_message);
        } else if (exception instanceof HttpException) {
            message = context.getString(R.string.exception_server_error);
        } else if (exception instanceof UnknownHostException) {
            message = context.getString(R.string.exception_unknown_host);
        } else if (exception instanceof SSLHandshakeException) {
            message = context.getString(R.string.exception_no_connection);
        } else if (exception instanceof SSLPeerUnverifiedException) {
            message = context.getString(R.string.exception_no_connection);
        } else if (exception instanceof ServerMaintainException) {
            message = null;
        } else if (exception instanceof GenericException) {
            message = exception.getMessage();
        }

        return message;
    }
}
