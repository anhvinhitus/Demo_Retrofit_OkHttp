package vn.com.vng.zalopay.promotion;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PromotionType.CASHBACK})
@Retention(RetentionPolicy.SOURCE)
public @interface PromotionType {
    int CASHBACK = 1;
}