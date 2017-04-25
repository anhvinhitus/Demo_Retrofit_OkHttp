package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({CardChannel.ATM, CardChannel.CREDIT})
@Retention(RetentionPolicy.SOURCE)
public @interface CardChannel {
    int ATM = 0;
    int CREDIT = 1;
}
