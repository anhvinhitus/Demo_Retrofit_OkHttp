package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({OrderStatus.PROCESSING, OrderStatus.SUCCESS, OrderStatus.FAILURE, OrderStatus.INVALID_PASSWORD})
@Retention(RetentionPolicy.SOURCE)
public @interface OrderStatus {
    int PROCESSING = 0; //order is processing
    int SUCCESS = 1;// transaction is success
    int FAILURE = -1; // transaction is fail
    int INVALID_PASSWORD = -117;
    int ERROR_BALANCE = -2;
}
