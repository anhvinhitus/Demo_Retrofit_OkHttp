package vn.com.vng.zalopay.ui.toolbar;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Set custom behavior for collapsing action (collapse toolbar)
 * Define view and child's view actions
 */

public class HeaderBehavior extends CoordinatorLayout.Behavior<HeaderView> {

    private Context mContext;

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public HeaderBehavior(Context context, AttributeSet attrs, Context mContext) {
        super(context, attrs);
        this.mContext = mContext;
    }

    public static int getToolbarHeight(Context context) {
        int result = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return result;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, HeaderView child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, HeaderView child, View dependency) {
        int maxScroll = ((AppBarLayout) dependency).getTotalScrollRange() - getToolbarHeight(mContext);
        float percentage = Math.abs(dependency.getY()) / (float) maxScroll;
        float childPosition = dependency.getHeight()
                + dependency.getY()
                - child.getHeight()
                - (getToolbarHeight(mContext) - child.getHeight()) * percentage / 2;

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        child.setLayoutParams(lp);
        child.setY(childPosition);
//        child.setAlpha(1f - (percentage));
//
//        if (isHide && percentage < 1) {
//            child.setVisibility(View.VISIBLE);
//            isHide = false;
//        } else if (!isHide && percentage == 1) {
//            child.setVisibility(View.GONE);
//            isHide = true;
//        }
        return true;
    }
}