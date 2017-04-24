package vn.com.vng.zalopay.ui.widget;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 5/25/16.
 * Divider for home page
 */
public class HomeSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;
    private Drawable mDivider;

    public HomeSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
        this.mDivider = ContextCompat.getDrawable(AndroidApplication.instance(), R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }


    private void drawVertical(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int position = 0; position < childCount; position++) {
            View child = parent.getChildAt(position);

            int left = child.getRight();
            int right = left + mDivider.getIntrinsicHeight();
            int top = child.getTop();
            int bottom = child.getBottom();
            //    int column = position % spanCount; // item column
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {

        int childCount = parent.getChildCount();
        for (int position = 0; position < childCount; position++) {
            View child = parent.getChildAt(position);

            int left = child.getLeft();
            int right = child.getRight();
            int top = child.getBottom();
            int bottom = top + mDivider.getIntrinsicHeight();
            // int column = position % spanCount; // item column
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}