package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;

/**
 * Created by AnhHieu on 9/10/16.
 * *
 */
public class GridPasswordViewFitWidth extends GridPasswordView {

    public GridPasswordViewFitWidth(Context context) {
        super(context);
    }

    public GridPasswordViewFitWidth(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridPasswordViewFitWidth(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        Timber.d("onMeasure:  %s", getChildCount());


        int widthSpecSize = Math.min(AndroidUtils.dp(258), MeasureSpec.getSize(widthMeasureSpec));

        Timber.d("onMeasure:  %s %s", widthMeasureSpec, widthSpecSize);

        int newHeight = widthSpecSize / getContext().getResources().getInteger(R.integer.pin_length);

        int newWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.getMode(widthMeasureSpec));
        int newHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.getMode(heightMeasureSpec));

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}
