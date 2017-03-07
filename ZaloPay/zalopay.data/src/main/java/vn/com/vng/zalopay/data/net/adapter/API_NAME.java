package vn.com.vng.zalopay.data.net.adapter;

import android.support.annotation.Keep;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by huuhoa on 1/10/17.
 * Annotation for API NAME in tracking API timing
 */

@Keep
@Target(METHOD)
@Retention(RUNTIME)
public @interface API_NAME {
    @Keep
    int value();
}
