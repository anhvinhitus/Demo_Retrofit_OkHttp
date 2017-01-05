package com.zalopay.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

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
        try {
            String fontAsset = typedArray.getString(R.styleable.IconFont_typefaceAsset);
            String iconName = typedArray.getString(R.styleable.IconFont_iconName);
            Log.d("IconFont", "icon name: " + iconName);

            setTypefaceFromAsset(fontAsset);
            setText(iconName);
        } catch (RuntimeException e) {
            Log.d("IconFont", "set font and icon name throw RuntimeException: " + e.getMessage());
        }

        try {
            int iconSize = typedArray.getDimensionPixelSize(R.styleable.IconFont_iconSize, -1);
            if (iconSize >= 0) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSize);
            }
        } catch (UnsupportedOperationException e) {
            Log.d("IconFont", "get icon size throw UnsupportedOperationException: " + e.getMessage());
        } catch (RuntimeException e) {
            Log.d("IconFont", "get icon size throw RuntimeException: " + e.getMessage());
        }

        try {
            typedArray.recycle();
        } catch (RuntimeException e) {
            Log.d("IconFont", "recycle typedArray throw RuntimeException: " + e.getMessage());
        }
    }

    public void setTypefaceFromAsset(String fontAsset) {
        if (TextUtils.isEmpty(fontAsset)) {
            return;
        }
        Typeface typeface = FontHelper.getmInstance().getFontFromAsset(fontAsset);
        if (typeface == null) {
            Log.d("IconFont", String.format("Could not create a typeface from asset: %s", fontAsset));
        } else {
            setTypefaceWithoutStyle(typeface);
        }
    }

    public void setTypefaceFromFile(String filePath) {
        Typeface typeface = FontHelper.getmInstance().getFontFromFileName(filePath);
        if (typeface == null) {
            Log.d("IconFont", String.format("Could not create a typeface from file: %s", filePath));
        } else {
            setTypefaceWithoutStyle(typeface);
        }
    }

    private void setTypefaceWithoutStyle(Typeface typeface) {
        if (typeface == null) {
            return;
        }
        int style = Typeface.NORMAL;

        if (getTypeface() != null) {
            style = getTypeface().getStyle();
        }
        setTypeface(typeface, style);
    }
}