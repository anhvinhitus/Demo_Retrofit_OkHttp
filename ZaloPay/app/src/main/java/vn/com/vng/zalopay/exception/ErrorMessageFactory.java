
package vn.com.vng.zalopay.exception;

import android.content.Context;

import java.net.SocketTimeoutException;
import java.util.Map;

import retrofit2.adapter.rxjava.HttpException;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.vng.uicomponent.widget.errorview.HttpStatusCodes;

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
        } else if (exception instanceof TokenException) {
            //message = context.getString(R.string.exception_token_expired_message);
            message = null;
        } else if (exception instanceof SocketTimeoutException) {
            message = context.getString(R.string.exception_timeout_message);
        } else if (exception instanceof HttpException) {
            message = mHttpStatusCode.get(((HttpException) exception).code());
        }

        return message;
    }

}
