package vn.vng.uicomponent.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by AnhHieu on 10/15/15.
 */
public class FrameLayoutBounded extends FrameLayout {

//    private int mMaxWidth = -1; default
//    private int mMaxHeight = -1;


    private int mMaxWidth = 0;
    private int mMaxHeight = 0;

    public FrameLayoutBounded(final Context context) {
        this(context, null);
    }

    public FrameLayoutBounded(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameLayoutBounded(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FrameLayoutBounded, defStyle, 0);

        mMaxWidth = a.getDimensionPixelSize(R.styleable.FrameLayoutBounded_maxWidth, -1);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.FrameLayoutBounded_maxHeight, -1);

        a.recycle();

    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        int newWidthMeasureSpec = widthMeasureSpec;
        int newHeightMeasureSpec = heightMeasureSpec;

        int measuredWidth = 0;
        int measuredHeight = 0;

        boolean widthWrapContent = getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean heightWrapContent = getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        if (!widthWrapContent) {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        if (!heightWrapContent) {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        if (widthWrapContent || heightWrapContent) {

            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);

                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                if (widthWrapContent) {
                    int margins = lp.leftMargin + lp.rightMargin;
                    measuredWidth = Math.max(measuredWidth, child.getMeasuredWidth() + margins);
                }

                if (heightWrapContent) {
                    int margins = lp.topMargin + lp.bottomMargin;
                    measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight() + margins);
                }
            }

        }

        // Adjust width as necessary

        if (mMaxWidth > -1 && mMaxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measureMode);
        }
        // Adjust height as necessary

        if (mMaxHeight > -1 && mMaxHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, measureMode);
        }


        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}
