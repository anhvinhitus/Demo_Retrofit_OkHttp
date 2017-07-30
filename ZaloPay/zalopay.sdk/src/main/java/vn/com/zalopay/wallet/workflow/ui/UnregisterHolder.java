package vn.com.zalopay.wallet.workflow.ui;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;

/**
 * Unregister form
 * Created by huuhoa on 7/30/17.
 */
public class UnregisterHolder {
    LinearLayout llUnregister;
    Spinner spnWalletType;
    Spinner spnPhoneNumber;
    EditText edtPhoneNumber;
    VPaymentDrawableEditText edtPassword;

    UnregisterHolder() {
    }

    Spinner getSpnWalletType() {
        return spnWalletType;
    }

    Spinner getSpnPhoneNumber() {
        return spnPhoneNumber;
    }

    public VPaymentDrawableEditText getEdtPassword() {
        return edtPassword;
    }

    public EditText getEdtPhoneNumber() {
        return edtPhoneNumber;
    }
}
