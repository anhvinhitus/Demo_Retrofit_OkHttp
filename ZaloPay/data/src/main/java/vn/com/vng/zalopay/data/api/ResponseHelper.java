package vn.com.vng.zalopay.data.api;

import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.exception.TokenException;

/**
 * Created by huuhoa on 6/22/16.
 */
public class ResponseHelper {
    public static boolean shouldIgnoreError(Throwable e) {
        if (e instanceof TokenException || e instanceof ServerMaintainException || e instanceof AccountSuspendedException) {
            return true;
        }

        return false;
    }
}
