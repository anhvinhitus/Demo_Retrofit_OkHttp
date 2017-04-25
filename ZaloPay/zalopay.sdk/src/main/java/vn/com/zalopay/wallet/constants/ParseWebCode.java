package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ParseWebCode.EXCEPTION, ParseWebCode.ATM_VERIFY_OTP_SUCCESS, ParseWebCode.ATM_RETRY_CAPTCHA, ParseWebCode.ATM_RETRY_OTP, ParseWebCode.ATM_CAPTCHA_INVALID})
@Retention(RetentionPolicy.SOURCE)
public @interface ParseWebCode {
    int EXCEPTION = 0;
    int ATM_VERIFY_OTP_SUCCESS = 13;
    int ATM_RETRY_CAPTCHA = 16;
    int ATM_RETRY_OTP = 17;
    int ATM_CAPTCHA_INVALID = -50;
}
