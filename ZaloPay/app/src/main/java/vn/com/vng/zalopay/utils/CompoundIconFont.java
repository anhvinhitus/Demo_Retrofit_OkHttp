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
import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 3/2/17.
 *
 */

public class CompoundIconFont extends TextView {
    IconFontDrawable mIconFontDrawable;

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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, com.zalopay.ui.widget.R.styleable.IconFont);

        if (typedArray == null) {
            return;
        }
        try {
            String iconName = typedArray.getString(com.zalopay.ui.widget.R.styleable.IconFont_iconName);
            mIconFontDrawable.setIcon(iconName);
        } catch (RuntimeException e) {
            Timber.d(e, "set font and icon name throw RuntimeException");
        }

        try {
            int iconSize = typedArray.getDimensionPixelSize(com.zalopay.ui.widget.R.styleable.IconFont_iconSize, -1);
            if (iconSize >= 0) {
                mIconFontDrawable.setPxSize(iconSize);
            }
        } catch (UnsupportedOperationException e) {
            Timber.d(e, "get icon size throw UnsupportedOperationException");
        } catch (RuntimeException e) {
            Timber.d(e, "get icon size throw RuntimeException");
        }

        try {
            int iconColor = typedArray.getResourceId(com.zalopay.ui.widget.R.styleable.IconFont_iconColor, -1);
            if (iconColor >= 0) {
                mIconFontDrawable.setResourcesColor(iconColor);
            }
        } catch (UnsupportedOperationException e) {
            Timber.d(e, "get icon color throw UnsupportedOperationException");
        } catch (RuntimeException e) {
            Timber.d(e, "get icon color throw RuntimeException");
        }

        try {
            int iconLocation = typedArray.getResourceId(com.zalopay.ui.widget.R.styleable.IconFont_iconLocation, -1);
            Drawable[] drawables = getCompoundDrawables();
            if (iconLocation >= 0) {
                switch (iconLocation) {
                    case R.string.left:
                        setCompoundDrawablesWithIntrinsicBounds(mIconFontDrawable, drawables[1], drawables[2], drawables[3]);
                        break;
                    case R.string.top:
                        setCompoundDrawablesWithIntrinsicBounds(drawables[0], mIconFontDrawable, drawables[2], drawables[3]);
                        break;
                    case R.string.right:
                        setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], mIconFontDrawable, drawables[3]);
                        break;
                    case R.string.bottom:
                        setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], mIconFontDrawable);
                        break;
                }
            }
        } catch (UnsupportedOperationException e) {
            Timber.d(e, "get icon color throw UnsupportedOperationException");
        } catch (RuntimeException e) {
            Timber.d(e, "get icon color throw RuntimeException");
        }

        try {
            typedArray.recycle();
        } catch (RuntimeException e) {
            Timber.d(e, "recycle typedArray throw RuntimeException");
        }
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