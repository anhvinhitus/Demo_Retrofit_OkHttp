package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Link_Then_Pay.VCB, Link_Then_Pay.BIDV, Link_Then_Pay.NONE})
@Retention(RetentionPolicy.SOURCE)
public @interface Link_Then_Pay {
    int NONE = 0;
    int VCB = 1;
    int BIDV = 2;
}
