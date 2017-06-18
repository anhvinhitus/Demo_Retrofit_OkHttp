package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PaymentState.PROCESSING, PaymentState.SUCCESS, PaymentState.FAILURE, PaymentState.INVALID_PASSWORD,
        PaymentState.ERROR_BALANCE, PaymentState.SECURITY})
@Retention(RetentionPolicy.SOURCE)
public @interface PaymentState {
    int PROCESSING = 0; //order is processing
    int SUCCESS = 1;// transaction is success
    int FAILURE = -1; // transaction is fail
    int INVALID_PASSWORD = -117;
    int ERROR_BALANCE = -2;
    int SECURITY = -3; // 3ds flow
}
