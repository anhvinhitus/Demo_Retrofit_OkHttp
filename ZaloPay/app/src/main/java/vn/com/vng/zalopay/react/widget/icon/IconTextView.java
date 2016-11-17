package vn.com.vng.zalopay.react.widget.icon;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by hieuvm on 11/16/16.
 */

public class IconTextView extends TextView {

    public IconTextView(Context context) {
        super(context);
        init(context, null);
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        setTransformationMethod(null);
    }

}
