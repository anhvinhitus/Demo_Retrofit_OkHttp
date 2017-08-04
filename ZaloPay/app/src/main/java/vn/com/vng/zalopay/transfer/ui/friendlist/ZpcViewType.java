package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by hieuvm on 8/4/17.
 * *
 */

@IntDef({ZpcViewType.ZPC_All, ZpcViewType.ZPC_PhoneBook})
@Retention(RetentionPolicy.SOURCE)
public @interface ZpcViewType {
    int ZPC_All = 1;
    int ZPC_PhoneBook = 2;
}
