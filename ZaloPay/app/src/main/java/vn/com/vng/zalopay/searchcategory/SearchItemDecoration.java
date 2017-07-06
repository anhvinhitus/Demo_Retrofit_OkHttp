package vn.com.vng.zalopay.searchcategory;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zalopay.ui.widget.IconFontTextView;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 7/6/17.
 * *
 */

class SearchItemDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;
    private int mPaddingLeft;

    SearchItemDecoration(int paddingLeft) {
        mDivider = ContextCompat.getDrawable(AndroidApplication.instance(), R.drawable.line_divider);
        mPaddingLeft = paddingLeft;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();

        for (int position = 0; position < childCount; position++) {
            View child = parent.getChildAt(position);
            if (!(child instanceof IconFontTextView)) {
                continue;
            }

            View nextChild = parent.getChildAt(position + 1);
            if (nextChild != null && !(nextChild instanceof IconFontTextView)) {
                continue;
            }

            int left = child.getLeft();

            if (nextChild != null) {
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
