package com.example.duke.stickyviewapp.ui.toolbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.duke.stickyviewapp.R;

/**
 * Created by anton on 11/12/15.
 */

public class HeaderView extends LinearLayout {
    private ImageView ivBarcode;

    public HeaderView(Context context) {
        super(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        ButterKnife.bind(this);
        init(getContext());
    }

    protected void init(Context context) {
        ivBarcode = (ImageView) findViewById(R.id.header_view_iv_barcode);
    }

    public void setOnBarcodeClickListener(OnClickListener listener) {
        ivBarcode.setOnClickListener(listener);
    }
}
