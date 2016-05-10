
package vn.com.vng.zalopay.exception;

import android.content.Context;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
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
        } else if (exception instanceof VideoNotFoundException) {
            message = context.getString(R.string.exception_video_not_found);
        } else if (exception instanceof BodyException) {
            message = exception.getMessage();
        }

        return message;
    }
}
