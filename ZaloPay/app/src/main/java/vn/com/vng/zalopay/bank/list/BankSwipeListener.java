package vn.com.vng.zalopay.bank.list;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;

import timber.log.Timber;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 7/11/17.
 * *
 */

final class BankSwipeListener extends SimpleSwipeListener {

    private float border = 0;

    BankSwipeListener(Context context) {
        border = context.getResources().getDimension(R.dimen.border_link_card);
    }

    @Override
    public void onStartOpen(SwipeLayout layout) {

        View foreground = layout.getSurfaceView();
        if (!(foreground instanceof ViewGroup)) {
            return;
        }

        View card = ((ViewGroup) foreground).getChildAt(0);

        if (card == null) {
            return;
        }

        GradientDrawable drawable = (GradientDrawable) card.getBackground();
        if (drawable != null) {
            drawable.setCornerRadii(new float[]{border, border, 0, 0, 0, 0, 0, 0});
        }
    }

    @Override
    public void onClose(SwipeLayout layout) {
        View foreground = layout.getSurfaceView();
        if (!(foreground instanceof ViewGroup)) {
            return;
        }

        View card = ((ViewGroup) foreground).getChildAt(0);

        if (card == null) {
            return;
        }

        GradientDrawable drawable = (GradientDrawable) card.getBackground();
        if (drawable != null) {
            drawable.setCornerRadii(new float[]{border, border, border, border, 0, 0, 0, 0});
        }
    }

    @Override
    public void onViewPositionChanged(SwipeLayout layout, View changedView, int left, int top, int dx, int dy) {

        if (layout.mDragDistance == 0) {
            return;
        }

        View background = layout.getCurrentBottomView();

        if (background == null) {
            return;
        }

        float alpha = Math.abs((float) left / layout.mDragDistance);

        if (alpha > 1) {
            alpha = 1;
        } else if (alpha < 0) {
            alpha = 0;
        }

       /* if (alpha == 0 || alpha == 1) {
            Timber.d("onViewPositionChanged: %s", alpha);
        }*/

        background.setAlpha(alpha);
    }
}
