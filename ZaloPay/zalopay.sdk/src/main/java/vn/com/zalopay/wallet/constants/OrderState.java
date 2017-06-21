package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({OrderState.SUBMIT, OrderState.QUERY_STATUS, OrderState.NO_STATUS})
@Retention(RetentionPolicy.SOURCE)
public @interface OrderState {
    int SUBMIT = 1;
    int QUERY_STATUS = 2;
    int NO_STATUS = 3;
}
