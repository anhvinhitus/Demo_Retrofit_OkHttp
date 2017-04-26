package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PaymentError.COMPONENT_NULL, PaymentError.DATA_INVALID, PaymentError.NETWORKING_ERROR})
@Retention(RetentionPolicy.SOURCE)
public @interface PaymentError {
    int COMPONENT_NULL = 1;
    int DATA_INVALID = 2;
    int NETWORKING_ERROR = 3;
}
