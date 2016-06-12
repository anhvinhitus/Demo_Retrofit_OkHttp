package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by AnhHieu on 6/12/16.
 */
public class WaveView extends FrameLayout {

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
       // setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }
}
