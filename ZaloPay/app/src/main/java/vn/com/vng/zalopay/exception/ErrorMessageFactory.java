
package vn.com.vng.zalopay.exception;

import android.content.Context;

import java.net.SocketTimeoutException;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.data.exception.VideoNotFoundException;

public class ErrorMessageFactory {

    private ErrorMessageFactory() {
        //empty
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
        } else if (exception instanceof TokenException) {

        } else if (exception instanceof SocketTimeoutException) {
            Timber.w(exception, "SocketTimeoutException");
            message = context.getString(R.string.exception_timeout_message);
        }

        return message;
    }

}
