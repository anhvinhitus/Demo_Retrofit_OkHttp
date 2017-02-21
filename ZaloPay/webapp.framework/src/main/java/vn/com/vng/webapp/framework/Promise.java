package vn.com.vng.webapp.framework;

import android.support.annotation.Nullable;

/**
 * Created by huuhoa on 2/18/17.
 *
 * Interface that represents a JavaScript Promise which can be passed to the native module as a
 * method parameter.
 *
 * Methods annotated with {@link ReactMethod} that use {@link Promise} as type of the last parameter
 * will be marked as "remoteAsync" and will return a promise when invoked from JavaScript.
 */
public interface Promise {
    /**
     * Successfully resolve the Promise.
     */
    void resolve(@Nullable Object value);

    /**
     * Report an error which wasn't caused by an exception.
     */
    void reject(int code, String message);

    /**
     * Report an exception.
     */
    void reject(int code, Throwable e);

    /**
     * Report an exception with a custom error message.
     */
    void reject(int code, String message, Throwable e);

    /**
     * Report an exception, with default error code.
     * Useful in catch-all scenarios where it's unclear why the error occurred.
     */
    void reject(Throwable reason);
}
