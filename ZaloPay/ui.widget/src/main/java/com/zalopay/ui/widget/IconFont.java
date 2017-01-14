package com.zalopay.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.zalopay.ui.widget.iconfont.IconFontHelper;
import com.zalopay.ui.widget.iconfont.IconFontInfo;
import com.zalopay.ui.widget.util.FontHelper;

import timber.log.Timber;

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
//            Log.d("IconFont", "icon name: " + iconName);

            if (!TextUtils.isEmpty(fontAsset)) {
                setTypefaceFromAsset(fontAsset);
            } else {
                setTypefaceWithoutStyle(IconFontHelper.getInstance().getCurrentTypeface());
            }
            setIcon(iconName);
        } catch (RuntimeException e) {
            Timber.d(e, "set font and icon name throw RuntimeException");
        }

        try {
            int iconSize = typedArray.getDimensionPixelSize(R.styleable.IconFont_iconSize, -1);
            if (iconSize >= 0) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSize);
            }
        } catch (UnsupportedOperationException e) {
            Timber.d(e, "get icon size throw UnsupportedOperationException");
        } catch (RuntimeException e) {
            Timber.d(e, "get icon size throw RuntimeException");
        }

        try {
            typedArray.recycle();
        } catch (RuntimeException e) {
            Timber.d(e, "recycle typedArray throw RuntimeException");
        }
    }

    public void setIconColor(int color) {
        try {
            super.setTextColor(ResourcesCompat.getColor(getResources(), color, null));
        } catch (Resources.NotFoundException e) {
            Timber.w(e, "setIconColor throw NotFoundException");
        }
    }

    public void setIconColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return;
        }
        super.setTextColor(Color.parseColor(color));
    }

    public void setIcon(Integer iconResource) {
        String iconName = getContext().getString(iconResource);
        setIcon(iconName);
    }

    public void setIcon(String iconName) {
        if (TextUtils.isEmpty(iconName)) {
            setText("");
        } else {
            IconFontInfo iconFontInfo = IconFontHelper.getInstance().getIconFontInfo(iconName);
            if (iconFontInfo == null) {
                Timber.w("setIcon fail, not found info of iconName: %s", iconName);
                setText("");
            } else {
                Timber.d("setIcon success, iconName: %s code: %s", iconName, iconFontInfo.code);
                setText(iconFontInfo.code);
            }
        }
    }

    public void setTypefaceFromAsset(String fontAsset) {
        if (TextUtils.isEmpty(fontAsset)
                || getContext() == null
                || getContext().getAssets() == null) {
            return;
        }
        Typeface typeface = FontHelper.getInstance().getFontFromAsset(getContext().getAssets(), fontAsset);
        if (typeface == null) {
            Timber.d("Could not create a typeface from asset: %s", fontAsset);
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