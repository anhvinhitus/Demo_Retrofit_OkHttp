package vn.vng.uicomponent.widget.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import vn.vng.uicomponent.widget.image.TouchImageView;

/**
 * Created by trung on 15/02/2016.
 */

public class ExtendedViewPager extends ViewPager {
    public ExtendedViewPager(Context context) {
        super(context);
    }

    public ExtendedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof TouchImageView) {
            //
            // canScrollHorizontally is not supported for Api < 14. To get around this issue,
            // ViewPager is extended and canScrollHorizontallyFroyo, a wrapper around
            // canScrollHorizontally supporting Api >= 8, is called.
            //
            return ((TouchImageView) v).canScrollHorizontallyFroyo(-dx);

        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }
}
