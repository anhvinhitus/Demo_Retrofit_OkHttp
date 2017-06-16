package com.zalopay.ui.widget.password.bottomsheet;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.IPinCallBack;

public abstract class PasswordRender implements UIBottomSheetDialog.IRender {
    protected IBuilder mBuilder;

    public PasswordRender(IBuilder pBuilder) {
        mBuilder = pBuilder;
    }

    @Override
    public View getView() {
        return mBuilder != null ? mBuilder.getView() : null;
    }

    @Override
    public void OnDismiss() {
        if (mBuilder == null) {
            return;
        }
        IPinCallBack IFPinListener = mBuilder.getIFPinCallBack();
        if (IFPinListener != null) {
            IFPinListener.onCancel();
        }
    }

}
