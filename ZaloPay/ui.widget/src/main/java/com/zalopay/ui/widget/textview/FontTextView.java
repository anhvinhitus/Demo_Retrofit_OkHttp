package com.zalopay.ui.widget.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.util.FontLoader;

/**
 * Created by hieuvm on 7/4/17.
 * *
 */

public class FontTextView extends android.support.v7.widget.AppCompatTextView {
    public FontTextView(Context context) {
        this(context, null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode()) {
            return;
        }

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);

        if (ta != null) {
            String fontAsset = ta.getString(R.styleable.FontTextView_typefaceAsset);
            if (!TextUtils.isEmpty(fontAsset)) {
                Typeface tf = FontLoader.getFont(getContext().getAssets(), fontAsset);
                setTypeface(tf);
            }
            ta.recycle();
        }


    }
}