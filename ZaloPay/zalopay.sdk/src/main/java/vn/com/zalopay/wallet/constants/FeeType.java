package vn.com.zalopay.wallet.constants;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({FeeType.MAX, FeeType.SUM})
@Retention(RetentionPolicy.SOURCE)
public @interface FeeType {
    String SUM = "SUM";
    String MAX = "MAX";
}
