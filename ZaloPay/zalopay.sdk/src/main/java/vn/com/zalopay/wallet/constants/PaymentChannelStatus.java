package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PaymentChannelStatus.ENABLE, PaymentChannelStatus.DISABLE, PaymentChannelStatus.MAINTENANCE})
@Retention(RetentionPolicy.SOURCE)
public @interface PaymentChannelStatus {
    int ENABLE = 1;
    int DISABLE = 0;
    int MAINTENANCE = 2;
}
