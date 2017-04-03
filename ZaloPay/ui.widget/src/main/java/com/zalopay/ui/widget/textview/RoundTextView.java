package com.zalopay.ui.widget.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by AnhHieu on 1/11/16.
 */
public class RoundTextView extends AppCompatTextView {
    private RoundViewDelegate delegate;
    private float density = 0;

    public RoundTextView(Context context) {
        this(context, null);
    }

    public RoundTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        delegate = new RoundViewDelegate(this, context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (delegate.isWidthHeightEqual() && getWidth() > 0 && getHeight() > 0) {
            int max = Math.max(getWidth(), getHeight());
            int measureSpec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
            super.onMeasure(measureSpec, measureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (delegate.isRadiusHalfHeight()) {
            delegate.setCornerRadius(getHeight() / 2);
        } else {
            delegate.setBgSelector();
        }
    }

    public void show(int num) {
        show(num, GONE);
    }

    @SuppressLint("all")
    public void show(int num, int typeHide) {
        RoundTextView rtv = this;
        ViewGroup.LayoutParams lp = rtv.getLayoutParams();
        rtv.setVisibility(View.VISIBLE);
        if (num <= 0) {
            setVisibility(typeHide);
        } else {
            lp.height = (int) (18 * density);
            if (num > 0 && num < 10) {//
                lp.width = (int) (18 * density);
                rtv.setText(String.valueOf(num));
            } else if (num > 9 && num < 100) {
                lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                rtv.setPadding((int) (6 * density), 0, (int) (6 * density), 0);
                rtv.setText("9+");
            } else {
                lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                rtv.setPadding((int) (6 * density), 0, (int) (6 * density), 0);
                rtv.setText("99+");
            }
            rtv.setLayoutParams(lp);
        }
    }

}