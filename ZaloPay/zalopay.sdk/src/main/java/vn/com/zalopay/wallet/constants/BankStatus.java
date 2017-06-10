package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BankStatus.ACTIVE, BankStatus.DISABLE, BankStatus.MAINTENANCE, BankStatus.UPVERSION})
@Retention(RetentionPolicy.SOURCE)
public @interface BankStatus {
    int DISABLE = -9999;
    int ACTIVE = 1;
    int MAINTENANCE = 2;
    int UPVERSION = 9999;
}
