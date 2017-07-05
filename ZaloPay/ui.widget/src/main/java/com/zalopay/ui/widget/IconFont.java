package com.zalopay.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.shamanland.fonticon.FontIconView;
import com.zalopay.ui.widget.util.IconFontLoader;

public class IconFont extends FontIconView {

    private static final String ICON_DEFAULT = "general_icondefault";

    private String mIconDefault = ICON_DEFAULT;

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

        String iconName = typedArray.getString(R.styleable.IconFont_iconName);
        mIconDefault = typedArray.getString(R.styleable.IconFont_iconDefault);
        int iconSize = typedArray.getDimensionPixelSize(R.styleable.IconFont_iconSize, -1);
        if (iconSize >= 0) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSize);
        }

        typedArray.recycle();
        setIcon(iconName);
    }

    public void setIconColor(@ColorRes int color) {
        int colorRes = ContextCompat.getColor(getContext(), color);
        super.setTextColor(colorRes);
    }

    public void setIconColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return;
        }
        int colorRes = Color.BLACK;
        try {
            colorRes = Color.parseColor(color);
        } catch (Exception ignore) {
        }

        super.setTextColor(colorRes);
    }

    public void setIcon(@StringRes int iconResource) {
        String iconName = getContext().getString(iconResource);
        setIcon(iconName);
    }

    public void setIcon(String iconName) {
        String code = IconFontLoader.getCode(iconName, mIconDefault == null ? ICON_DEFAULT : mIconDefault);
        if (!TextUtils.isEmpty(code)) {
            setText(code);
            return;
        }

        setText("");
    }
}