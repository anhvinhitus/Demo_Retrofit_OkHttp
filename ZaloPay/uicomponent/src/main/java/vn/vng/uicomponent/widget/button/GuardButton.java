package vn.vng.uicomponent.widget.button;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Project:  ZingMobile
 * Author:   Khuong Vo
 * Since:    8/13/2015
 * Time:     2:16 AM
 */
public class GuardButton extends Button {

    private ClickGuard mGuard;

    public GuardButton(Context context) {
        super(context);
    }

    public GuardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GuardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GuardButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mGuard != null) {
            mGuard.rest();
            mGuard = null;
        }
    }

    public void registerAvoidMultipleRapidClicks(){
        if (mGuard == null)
            mGuard = ClickGuard.newGuard();

        mGuard.add(GuardButton.this);
        mGuard.watch();
    }
}

