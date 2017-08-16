package com.zalopay.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;

import timber.log.Timber;

public class UIBottomSheetDialog extends BottomSheetDialog {
    IRender mRender;
    boolean mPreventDrag = false;

    public UIBottomSheetDialog(@NonNull Context context, @StyleRes int theme, @NonNull IRender pRender) {
        super(context, theme);
        mRender = pRender;
        try {
            setContentView(mRender.getView());
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) mRender.getView().getParent());
            configureBottomSheetBehavior(bottomSheetBehavior);
            pRender.render(context);
            setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    try {
                        if (mRender != null) {
                            mRender.OnDismiss();
                        }
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                }
            });
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void setState(int state) throws Exception {
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) mRender.getView().getParent());
        bottomSheetBehavior.setState(state);
    }

    public void preventDrag(boolean pPrevent) {
        mPreventDrag = pPrevent;
    }

    private void configureBottomSheetBehavior(final BottomSheetBehavior pBottomSheetBehavior) {
        if (pBottomSheetBehavior == null) {
            return;
        }
        pBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
                        if (mPreventDrag) {
                            pBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
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

    public interface IRender {
        void render(Context context) throws Exception;

        View getView() throws Exception;

        void OnDismiss() throws Exception;
    }
}
