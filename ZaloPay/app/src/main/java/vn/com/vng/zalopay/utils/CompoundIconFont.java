package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zalopay.ui.widget.iconfont.IconFontHelper;
import com.zalopay.ui.widget.iconfont.IconFontInfo;

import timber.log.Timber;

import com.zalopay.ui.widget.R;

/**
 * Created by khattn on 3/2/17.
 *
 */

public class CompoundIconFont extends TextView {

    IconFontDrawable mLeftIcon, mRightIcon, mTopIcon, mBottomIcon;
    final int[] mLeftValue = {R.styleable.CompoundIconFont_iconLeftName,
            R.styleable.CompoundIconFont_iconLeftSize,
            R.styleable.CompoundIconFont_iconLeftColor};
    final int[] mRightValue = {R.styleable.CompoundIconFont_iconRightName,
            R.styleable.CompoundIconFont_iconRightSize,
            R.styleable.CompoundIconFont_iconRightColor};
    final int[] mTopValue = {R.styleable.CompoundIconFont_iconTopName,
            R.styleable.CompoundIconFont_iconTopSize,
            R.styleable.CompoundIconFont_iconTopColor};
    final int[] mBottomValue = {R.styleable.CompoundIconFont_iconBottomName,
            R.styleable.CompoundIconFont_iconBottomSize,
            R.styleable.CompoundIconFont_iconBottomColor};

    public CompoundIconFont(Context context) {
        this(context, null);
    }

    public CompoundIconFont(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompoundIconFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }

        mLeftIcon = new IconFontDrawable(context);
        mRightIcon = new IconFontDrawable(context);
        mTopIcon = new IconFontDrawable(context);
        mBottomIcon = new IconFontDrawable(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CompoundIconFont);

        if (typedArray == null) {
            return;
        }

        initIcon(mLeftIcon, typedArray, mLeftValue[0], mLeftValue[1], mLeftValue[2]);
        initIcon(mRightIcon, typedArray, mRightValue[0], mRightValue[1], mRightValue[2]);
        initIcon(mTopIcon, typedArray, mTopValue[0], mTopValue[1], mTopValue[2]);
        initIcon(mBottomIcon, typedArray, mBottomValue[0], mBottomValue[1], mBottomValue[2]);
        setCompoundDrawablesWithIntrinsicBounds(mLeftIcon, mTopIcon, mRightIcon, mBottomIcon);

        try {
            typedArray.recycle();
        } catch (RuntimeException e) {
            Timber.d(e, "recycle typedArray throw RuntimeException");
        }
    }

    private void initIcon(IconFontDrawable icon, TypedArray typedArray, int name, int size, int color) {
        if(icon == null) {
            return;
        }

        String iconName = typedArray.getString(name);
        int iconSize = typedArray.getDimensionPixelSize(size, -1);
        int iconColor = typedArray.getResourceId(color, -1);

        if (iconName != null) {
            icon.setIcon(iconName);
        }

        if (iconSize >= 0) {
            icon.setPxSize(iconSize);
        }

        if (iconColor >= 0) {
            icon.setResourcesColor(iconColor);
        }
    }


    public IconFontDrawable getLeftIcon() {
        return mLeftIcon;
    }

    public IconFontDrawable getRightIcon() {
        return mRightIcon;
    }

    public IconFontDrawable getTopIcon() {
        return mTopIcon;
    }

    public IconFontDrawable getBottomIcon() {
        return mBottomIcon;
    }
}