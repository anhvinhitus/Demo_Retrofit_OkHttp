package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BankAccountError.EMPTY_USERNAME, BankAccountError.EMPTY_PASSWORD, BankAccountError.EMPTY_CAPCHA,
        BankAccountError.WRONG_USERNAME_PASSWORD, BankAccountError.WRONG_CAPTCHA, BankAccountError.ACCOUNT_LOCKED, BankAccountError.OTHERS})
@Retention(RetentionPolicy.SOURCE)
public @interface BankAccountError {
    int EMPTY_USERNAME = 0;
    int EMPTY_PASSWORD = 1;
    int EMPTY_CAPCHA = 2;
    int WRONG_USERNAME_PASSWORD = 3;
    int WRONG_CAPTCHA = 4;
    int ACCOUNT_LOCKED = 5;
    int OTHERS = 6;
}
