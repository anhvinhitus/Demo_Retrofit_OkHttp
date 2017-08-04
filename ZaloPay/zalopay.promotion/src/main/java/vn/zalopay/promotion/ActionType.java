package vn.zalopay.promotion;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ActionType.TRANSACTION_DETAIL, ActionType.VOUCHER_LIST})
@Retention(RetentionPolicy.SOURCE)
public @interface ActionType {
    int TRANSACTION_DETAIL = 1;
    int VOUCHER_LIST = 2;
}