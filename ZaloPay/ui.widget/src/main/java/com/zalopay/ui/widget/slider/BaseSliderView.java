package com.zalopay.ui.widget.slider;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;

public abstract class BaseSliderView {

    public abstract View getView();

    public interface OnSliderClickListener {
        void onSliderClick(BaseSliderView slider);
    }

    protected final WeakReference<Context> mContext;

    public Bundle mBundle;

    public String mUrl;

    public String mDescription;

    protected OnSliderClickListener mOnSliderClickListener;

    protected BaseSliderView(Context context) {
        mContext = new WeakReference<>(context);
    }

    public Context getContext() {
        return mContext.get();
    }

    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
        return this;
    }

    protected void bindEventAndShow(@NonNull View parent, @NonNull SimpleDraweeView targetImageView) {
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(BaseSliderView.this);
                }
            }
        });

        targetImageView.setImageURI(mUrl);
    }
}
