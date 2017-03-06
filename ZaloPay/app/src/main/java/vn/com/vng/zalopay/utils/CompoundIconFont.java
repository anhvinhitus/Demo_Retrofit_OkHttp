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
 */

public class CompoundIconFont extends TextView {

    IconFontDrawable mIconFontDrawable;
    final int[] mLeftIcon = {R.styleable.CompoundIconFont_iconLeftName,
            R.styleable.CompoundIconFont_iconLeftSize,
            R.styleable.CompoundIconFont_iconLeftColor};
    final int[] mRightIcon = {R.styleable.CompoundIconFont_iconRightName,
            R.styleable.CompoundIconFont_iconRightSize,
            R.styleable.CompoundIconFont_iconRightColor};
    final int[] mTopIcon = {R.styleable.CompoundIconFont_iconTopName,
            R.styleable.CompoundIconFont_iconTopSize,
            R.styleable.CompoundIconFont_iconTopColor};
    final int[] mBottomIcon = {R.styleable.CompoundIconFont_iconBottomName,
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

        mIconFontDrawable = new IconFontDrawable(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CompoundIconFont);
        Drawable[] drawables = getCompoundDrawables();

        if (typedArray == null) {
            return;
        }

        if (initIcon(typedArray, mLeftIcon[0], mLeftIcon[1], mLeftIcon[2])) {

            setCompoundDrawablesWithIntrinsicBounds(mIconFontDrawable, drawables[1], drawables[2], drawables[3]);
        } else if (initIcon(typedArray, mRightIcon[0], mRightIcon[1], mRightIcon[2])) {

            setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], mIconFontDrawable, drawables[3]);
        } else if (initIcon(typedArray, mTopIcon[0], mTopIcon[1], mTopIcon[2])) {

            setCompoundDrawablesWithIntrinsicBounds(drawables[0], mIconFontDrawable, drawables[2], drawables[3]);
        } else if (initIcon(typedArray, mBottomIcon[0], mBottomIcon[1], mBottomIcon[2])) {

            setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], mIconFontDrawable);
        }

        try {
            typedArray.recycle();
        } catch (RuntimeException e) {
            Timber.d(e, "recycle typedArray throw RuntimeException");
        }
    }

    private boolean initIcon(TypedArray typedArray, int name, int size, int color) {
        boolean isLocation = false;

        try {
            String iconName = typedArray.getString(name);
            if (iconName != null) {
                mIconFontDrawable.setIcon(iconName);
                isLocation = true;
            }
        } catch (RuntimeException e) {
            Timber.d(e, "set font and icon name throw RuntimeException");
        }

        try {
            int iconSize = typedArray.getDimensionPixelSize(size, -1);
            if (iconSize >= 0) {
                mIconFontDrawable.setPxSize(iconSize);
                isLocation = true;
            }
        } catch (UnsupportedOperationException e) {
            Timber.d(e, "get icon size throw UnsupportedOperationException");
        } catch (RuntimeException e) {
            Timber.d(e, "get icon size throw RuntimeException");
        }

        try {
            int iconColor = typedArray.getResourceId(color, -1);
            if (iconColor >= 0) {
                mIconFontDrawable.setResourcesColor(iconColor);
                isLocation = true;
            }
        } catch (UnsupportedOperationException e) {
            Timber.d(e, "get icon color throw UnsupportedOperationException");
        } catch (RuntimeException e) {
            Timber.d(e, "get icon color throw RuntimeException");
        }

        return isLocation;
    }

    public void setIconColor(int color) {
        try {
            mIconFontDrawable.setResourcesColor(color);
        } catch (Resources.NotFoundException e) {
            Timber.w(e, "setIconColor throw NotFoundException");
        }
    }

    public void setIconColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return;
        }
        mIconFontDrawable.setColor(Color.parseColor(color));
    }

    public void setIcon(Integer iconResource) {
        mIconFontDrawable.setIcon(iconResource);
    }

    public void setIcon(String iconName) {
        if (TextUtils.isEmpty(iconName)) {
            setText("");
        } else {
            IconFontInfo iconFontInfo = IconFontHelper.getInstance().getIconFontInfo(iconName);
            if (iconFontInfo != null) {
                mIconFontDrawable.setIcon(iconFontInfo.code);
            }
        }
    }
}