package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({TransAuthenType.PIN, TransAuthenType.OTP, TransAuthenType.BOTH})
@Retention(RetentionPolicy.SOURCE)
public @interface TransAuthenType {
    int PIN = 1;
    int OTP = 2;
    int BOTH = 3;
}
