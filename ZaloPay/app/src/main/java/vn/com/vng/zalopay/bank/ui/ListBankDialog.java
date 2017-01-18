package vn.com.vng.zalopay.bank.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by longlv on 1/18/17.
 * *
 */

public class ListBankDialog extends Dialog {

    public ListBankDialog(Context context) {
        this(context, vn.com.zalopay.wallet.R.style.alert_dialog);
    }

    public ListBankDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

    protected ListBankDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
