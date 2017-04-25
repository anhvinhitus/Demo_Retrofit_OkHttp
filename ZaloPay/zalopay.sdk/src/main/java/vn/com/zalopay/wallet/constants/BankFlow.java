package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BankFlow.LOADWEB, BankFlow.PARSEWEB, BankFlow.API})
@Retention(RetentionPolicy.SOURCE)
public @interface BankFlow {
    int LOADWEB = 1;
    int PARSEWEB = 2;
    int API = 3;
}
