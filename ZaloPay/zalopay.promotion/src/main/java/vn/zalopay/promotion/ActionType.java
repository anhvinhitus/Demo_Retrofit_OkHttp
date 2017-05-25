package vn.zalopay.promotion;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ActionType.TRANSACTION_DETAIL})
@Retention(RetentionPolicy.SOURCE)
public @interface ActionType {
    int TRANSACTION_DETAIL = 1;
}