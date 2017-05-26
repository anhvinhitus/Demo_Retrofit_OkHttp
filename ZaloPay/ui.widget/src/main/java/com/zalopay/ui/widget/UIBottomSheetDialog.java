package com.zalopay.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
import android.view.View;

public class UIBottomSheetDialog extends BottomSheetDialog {
    private IRender mRender;

    public UIBottomSheetDialog(@NonNull Context context, @StyleRes int theme, @NonNull IRender pRender) {
        super(context, theme);
        mRender = pRender;
        setContentView(mRender.getView());
        configureBottomSheetBehavior(mRender.getView());
        pRender.render(context);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mRender != null) {
                    mRender.OnDismiss();
                }
                Log.d(getClass().getSimpleName(), "on dismiss UIBottomSheetDialog");
            }
        });
    }

    private void configureBottomSheetBehavior(View contentView) {
        BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    //showing the different states
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            dismiss(); //if you want the modal to be dismissed when user drags the bottomsheet down
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        }
    }

    public interface IRender {
        void render(Context context);

        View getView();

        void OnDismiss();
    }
}
