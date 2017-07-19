package com.zalopay.ui.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by hieuvm on 7/12/17.
 * *
 */

public class HorizontalDividerDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;
    private int mPaddingLeft;

    public HorizontalDividerDecoration(Context context, int paddingLeft, @DrawableRes int line_divider) {
        mDivider = ContextCompat.getDrawable(context, line_divider);
        mPaddingLeft = paddingLeft;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();

        for (int position = 0; position < childCount; position++) {
            View child = parent.getChildAt(position);

            int left = child.getLeft();

            if (position != childCount - 1) {
                left += mPaddingLeft;
            }

            int right = child.getRight();
            int top = child.getBottom();
            int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
