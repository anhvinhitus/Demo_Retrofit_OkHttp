package vn.com.vng.zalopay.transfer.model;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sinhtt on 8/11/17.
 */

@StringDef({TransferMode.ACCOUNT_NAME, TransferMode.PHONE_NUMBER})
@Retention(RetentionPolicy.SOURCE)
public @interface TransferMode {
    String ACCOUNT_NAME = "ACCOUNT_NAME";
    String PHONE_NUMBER = "PHONE_NUMBER";
}