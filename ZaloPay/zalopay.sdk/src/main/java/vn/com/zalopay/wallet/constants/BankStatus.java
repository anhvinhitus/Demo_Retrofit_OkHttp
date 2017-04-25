package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BankStatus.ACTIVE, BankStatus.MAINTENANCE})
@Retention(RetentionPolicy.SOURCE)
public @interface BankStatus {
    int ACTIVE = 1;
    int MAINTENANCE = 2;
}
