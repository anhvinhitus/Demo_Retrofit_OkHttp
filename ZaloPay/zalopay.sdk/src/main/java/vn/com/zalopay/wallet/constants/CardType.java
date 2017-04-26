package vn.com.zalopay.wallet.constants;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({CardType.VISA, CardType.MASTER, CardType.JCB, CardType.PVTB, CardType.PBIDV, CardType.PSCB, CardType.PSGCB, CardType.PVCB, CardType.UNDEFINE})
@Retention(RetentionPolicy.SOURCE)
public @interface CardType {
    String VISA = "VISA";
    String MASTER = "MASTER";
    String JCB = "JCB";
    String PVTB = "123PVTB";
    String PBIDV = "123PBIDV";
    String PSCB = "123PSCB";
    String PSGCB = "123PSGCB";
    String PVCB = "ZPVCB";
    String UNDEFINE = "UND";
}
