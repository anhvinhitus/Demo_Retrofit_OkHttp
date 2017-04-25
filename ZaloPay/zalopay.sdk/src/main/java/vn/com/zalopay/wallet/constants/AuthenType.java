package vn.com.zalopay.wallet.constants;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({AuthenType.OTP, AuthenType.TOKEN})
@Retention(RetentionPolicy.SOURCE)
public @interface AuthenType {
    String OTP = "otp";
    String TOKEN = "token";
}
