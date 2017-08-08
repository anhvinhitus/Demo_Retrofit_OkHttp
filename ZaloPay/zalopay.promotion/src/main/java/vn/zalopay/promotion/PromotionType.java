package vn.zalopay.promotion;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PromotionType.CASHBACK, PromotionType.VOUCHER})
@Retention(RetentionPolicy.SOURCE)
public @interface PromotionType {
    int CASHBACK = 1;
    int VOUCHER = 2;
}