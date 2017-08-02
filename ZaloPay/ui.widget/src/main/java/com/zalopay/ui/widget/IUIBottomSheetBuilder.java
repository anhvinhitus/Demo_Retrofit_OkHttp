package com.zalopay.ui.widget;

/*
 * Created by chucvv on 8/2/17.
 */

import android.view.View;

public interface IUIBottomSheetBuilder<T> {
    View getView();

    T setView(View pView);

    UIBottomSheetDialog.IRender build();

    void release();
}
