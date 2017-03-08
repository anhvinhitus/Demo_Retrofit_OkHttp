package com.zalopay.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import timber.log.Timber;

/**
 * Created by khattn on 3/2/17.
 *
 */

public class IconFontTextView extends AppCompatTextView {

    IconFontDrawable mLeftIcon, mRightIcon, mTopIcon, mBottomIcon;
    final int[] mLeftValue = {R.styleable.IconFontTextView_iconLeftName,
            R.styleable.IconFontTextView_iconLeftSize,
            R.styleable.IconFontTextView_iconLeftColor,
            R.styleable.IconFontTextView_iconLeftPadding};
    final int[] mRightValue = {R.styleable.IconFontTextView_iconRightName,
            R.styleable.IconFontTextView_iconRightSize,
            R.styleable.IconFontTextView_iconRightColor,
            R.styleable.IconFontTextView_iconRightPadding};
    final int[] mTopValue = {R.styleable.IconFontTextView_iconTopName,
            R.styleable.IconFontTextView_iconTopSize,
            R.styleable.IconFontTextView_iconTopColor,
            R.styleable.IconFontTextView_iconTopPadding};
    final int[] mBottomValue = {R.styleable.IconFontTextView_iconBottomName,
            R.styleable.IconFontTextView_iconBottomSize,
            R.styleable.IconFontTextView_iconBottomColor,
            R.styleable.IconFontTextView_iconBottomPadding};

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

        mLeftIcon = new IconFontDrawable(context);
        mRightIcon = new IconFontDrawable(context);
        mTopIcon = new IconFontDrawable(context);
        mBottomIcon = new IconFontDrawable(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconFontTextView);

        if (typedArray == null) {
            return;
        }

        initIcon(mLeftIcon, typedArray, mLeftValue[0], mLeftValue[1], mLeftValue[2]);
        initIcon(mRightIcon, typedArray, mRightValue[0], mRightValue[1], mRightValue[2]);
        initIcon(mTopIcon, typedArray, mTopValue[0], mTopValue[1], mTopValue[2]);
        initIcon(mBottomIcon, typedArray, mBottomValue[0], mBottomValue[1], mBottomValue[2]);

        initPadding(typedArray, mLeftValue[3], mTopValue[3], mRightValue[3], mBottomValue[3]);

        setCompoundDrawablesWithIntrinsicBounds(mLeftIcon, mTopIcon, mRightIcon, mBottomIcon);
        setPadding(getPaddingLeft() + mLeftIcon.getLeftPxPadding(),
                getPaddingTop() + mTopIcon.getTopPxPadding(),
                getPaddingRight() + mRightIcon.getRightPxPadding(),
                getPaddingBottom() + mBottomIcon.getBottomPxPadding());

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

    private void initPadding(TypedArray typedArray, int left, int top, int right, int bottom) {
        int leftPad = typedArray.getResourceId(left, -1);
        int topPad = typedArray.getResourceId(top, -1);
        int rightPad = typedArray.getResourceId(right, -1);
        int bottomPad = typedArray.getResourceId(bottom, -1);

        if (leftPad >= 0) {
            leftPad = getContext().getResources().getDimensionPixelSize(leftPad);
            mLeftIcon.setPxPadding(leftPad, 0 , 0, 0);
        }
        if (topPad >= 0) {
            topPad = getContext().getResources().getDimensionPixelSize(topPad);
            mTopIcon.setPxPadding(0, topPad , 0, 0);
        }
        if (rightPad >= 0) {
            rightPad = getContext().getResources().getDimensionPixelSize(rightPad);
            mRightIcon.setPxPadding(0, 0 , rightPad, 0);
        }
        if (bottomPad >= 0) {
            bottomPad = getContext().getResources().getDimensionPixelSize(bottomPad);
            mBottomIcon.setPxPadding(0, 0 , 0, bottomPad);
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