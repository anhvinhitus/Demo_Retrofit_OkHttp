package vn.com.vng.zalopay.react.model;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sinhtt on 8/8/17.
 */

@StringDef({ZPCViewMode.keyboardABC, ZPCViewMode.keyboardPhone})
@Retention(RetentionPolicy.SOURCE)
public @interface ZPCViewMode {
    String keyboardABC = "keyboardABC";
    String keyboardPhone = "keyboardPhone";
}
