package com.zalopay.ui.widget.pinlayout.bottomsheet;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.pinlayout.interfaces.IBuilder;
import com.zalopay.ui.widget.pinlayout.interfaces.IFPinCallBack;

public abstract class PinRender implements UIBottomSheetDialog.IRender {
    protected IBuilder mBuilder;

    public PinRender(IBuilder pBuilder) {
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
        IFPinCallBack IFPinListener = mBuilder.getIFPinCallBack();
        if (IFPinListener != null) {
            IFPinListener.onCancel();
        }
    }

}
