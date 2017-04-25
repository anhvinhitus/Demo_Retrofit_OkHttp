package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PaymentActionStatus.OTP, PaymentActionStatus.THREE3DS})
@Retention(RetentionPolicy.SOURCE)
public @interface PaymentActionStatus {
    int OTP = 1;
    int THREE3DS = 2;
}
