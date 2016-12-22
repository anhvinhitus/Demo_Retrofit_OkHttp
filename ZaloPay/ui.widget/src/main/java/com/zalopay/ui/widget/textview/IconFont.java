package com.zalopay.ui.widget.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.util.FontHelper;

/**
 * Created by longlv on 11/17/16.
 * TextView subclass which allows the user to define a truetype font file to use as the view's typeface.
 */

public class IconFont extends TextView {
    public IconFont(Context context) {
        this(context, null);
    }

    public IconFont(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconFont);

        if (typedArray == null) {
            return;
        }
        String fontAsset = typedArray.getString(R.styleable.IconFont_typefaceAsset);

        if (TextUtils.isEmpty(fontAsset)) {
            return;
        }
        Typeface typeface = FontHelper.getmInstance().getFont(fontAsset);
        int style = Typeface.NORMAL;
        float size = getTextSize();

        if (getTypeface() != null)
            style = getTypeface().getStyle();

        if (typeface != null) {
            setTypeface(typeface, style);
        } else {
            Log.d("IconFont", String.format("Could not create a font from asset: %s", fontAsset));
        }
    }
}