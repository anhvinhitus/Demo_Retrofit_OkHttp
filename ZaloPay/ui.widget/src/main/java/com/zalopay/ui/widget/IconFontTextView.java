package com.zalopay.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.shamanland.fonticon.FontIconDrawable;
import com.shamanland.fonticon.FontIconTextView;
import com.zalopay.ui.widget.util.IconFontLoader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

/**
 * Created by khattn on 3/2/17.
 */

public class IconFontTextView extends FontIconTextView {

    private static final boolean DEBUG = true;

    private static final int[] LEFT_VALUE = {R.styleable.IconFontTextView_iconLeftName,
            R.styleable.IconFontTextView_iconLeftSize,
            R.styleable.IconFontTextView_iconLeftColor};
    private static final int[] RIGHT_VALUE = {R.styleable.IconFontTextView_iconRightName,
            R.styleable.IconFontTextView_iconRightSize,
            R.styleable.IconFontTextView_iconRightColor};
    private static final int[] TOP_VALUE = {R.styleable.IconFontTextView_iconTopName,
            R.styleable.IconFontTextView_iconTopSize,
            R.styleable.IconFontTextView_iconTopColor};
    private static final int[] BOTTOM_VALUE = {R.styleable.IconFontTextView_iconBottomName,
            R.styleable.IconFontTextView_iconBottomSize,
            R.styleable.IconFontTextView_iconBottomColor};

    public static final int Left = 0;
    public static final int Top = 1;
    public static final int Right = 2;
    public static final int Bottom = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {Left, Top, Right, Bottom})
    public @interface Gravity {
    }

    private static final String ICON_DEFAULT = "general_icondefault";

    public IconFontTextView(Context context) {
        this(context, null);
    }

    public IconFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconFontTextView);

        if (typedArray == null) {
            return;
        }

        initCompound(Left, typedArray, LEFT_VALUE[0], LEFT_VALUE[1], LEFT_VALUE[2]);
        initCompound(Top, typedArray, TOP_VALUE[0], TOP_VALUE[1], TOP_VALUE[2]);
        initCompound(Right, typedArray, RIGHT_VALUE[0], RIGHT_VALUE[1], RIGHT_VALUE[2]);
        initCompound(Bottom, typedArray, BOTTOM_VALUE[0], BOTTOM_VALUE[1], BOTTOM_VALUE[2]);

        typedArray.recycle();
    }

    private void initCompound(@Gravity int gravity, TypedArray typedArray, int name, int size, int color) {

        String iconName = typedArray.getString(name);
        if (TextUtils.isEmpty(iconName)) {
            return;
        }

        int iconSize = typedArray.getDimensionPixelSize(size, -1);
        int iconColor = typedArray.getColor(color, -1);

        setIcon(gravity, iconName, ICON_DEFAULT, iconColor, iconSize);
    }

    @NonNull
    private FontIconDrawable getDrawable(@Gravity int gravity) {
        Drawable[] compound = getDrawables();
        Drawable drawable = compound[gravity];
        if (!(drawable instanceof FontIconDrawable)) {
            compound[gravity] = new FontIconDrawable();
            drawable = compound[gravity];

            Drawable left = compound[0];
            Drawable top = compound[1];
            Drawable right = compound[2];
            Drawable bottom = compound[3];

            setCompoundDrawables(left, top, right, bottom);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setCompoundDrawablesRelative(left, top, right, bottom);
            }
        }
        return (FontIconDrawable) drawable;
    }

    private Drawable[] getDrawables() {
        Drawable[] compound = getCompoundDrawables();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            compound = getCompoundDrawablesRelative();
        }

        return compound;
    }

    public void setIcon(@Gravity int gravity, String iconName) {
        setIcon(gravity, iconName, ICON_DEFAULT, -1);
    }

    public void setIcon(@Gravity int gravity, String iconName, @ColorInt int color) {
        setIcon(gravity, iconName, ICON_DEFAULT, color);
    }

    public void setIcon(@Gravity int gravity, String iconName, String iconDefault, @ColorInt int color) {
        setIcon(gravity, iconName, iconDefault, color, -1);
    }

    public void setIcon(@Gravity int gravity, String iconName, String iconDefault, @ColorInt int color, float size) {
        FontIconDrawable drawable = getDrawable(gravity);
        if (TextUtils.isEmpty(iconName)) {
            return;
        }

        String code = IconFontLoader.getCode(iconName, iconDefault);

        //if (color >= 0) {
        drawable.setTextColor(color);
        //}

        if (size >= 0) {
            drawable.setTextSize(size);
        }

        drawable.setText(code);

        if (DEBUG) {
            Timber.d("initCompound: [iconName:%s iconSize:%s iconColor:%s gravity:%s]", iconName, size, color, gravity);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            updateCompoundDrawablesRelative();
        } else {
            updateCompoundDrawables();
        }
    }


    @Deprecated
    public void setLeftIcon(String iconName) {
        setIcon(Left, iconName);
    }

    @Deprecated
    public void setLeftIcon(@StringRes int icon) {
        String iconName = getContext().getString(icon);
        setLeftIcon(iconName);
    }

    @Deprecated
    public void setLeftIcon(String iconName, String colorHex) {
        int color = Color.BLACK;

        try {
            color = Color.parseColor(colorHex);
        } catch (Exception ignore) {
        }

        setIcon(Left, iconName, color);
    }

    @Deprecated
    public void setTopIcon(String iconName, @ColorInt int color) {
        setIcon(Top, iconName, color);
    }

    @Deprecated
    public void setTopIcon(@StringRes int icon, String colorHex) {
        int color = Color.BLACK;

        try {
            color = Color.parseColor(colorHex);
        } catch (Exception ignore) {
        }

        String iconName = getContext().getString(icon);
        setTopIcon(iconName, color);
    }

    @Deprecated
    public void setTopIcon(String iconName, String colorHex) {
        int color = Color.BLACK;

        try {
            color = Color.parseColor(colorHex);
        } catch (Exception ignore) {
        }

        setTopIcon(iconName, color);
    }

}