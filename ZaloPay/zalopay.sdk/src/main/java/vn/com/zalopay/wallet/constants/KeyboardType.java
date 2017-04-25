package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({KeyboardType.NUMBER, KeyboardType.TEXT})
@Retention(RetentionPolicy.SOURCE)
public @interface KeyboardType {
    int NUMBER = 1;
    int TEXT = 2;
}
