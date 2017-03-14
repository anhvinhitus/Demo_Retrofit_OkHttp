package com.example.duke.stickyviewapp.ui.toolbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.duke.stickyviewapp.R;

import static android.R.attr.animation;

/**
 * Created by anton on 11/12/15.
 */

public class HeaderViewTop extends LinearLayout {
    private LinearLayout headerSearch, headerAnother;
    private Animation animShow, animHide;
    private ImageView ivBarcode2;

    public HeaderViewTop(Context context) {
        super(context);
    }

    public HeaderViewTop(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderViewTop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderViewTop(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        ButterKnife.bind(this);
        init(getContext());
    }

    protected void init(Context context) {
        headerSearch = (LinearLayout) findViewById(R.id.header_view_top_search);
        headerAnother = (LinearLayout) findViewById(R.id.header_view_top_another);
        ivBarcode2 = (ImageView) findViewById(R.id.header_view_top_barcode2);

        animHide = new AlphaAnimation(1.0f, 0.0f);
        animHide.setDuration(400);
        animHide.setFillAfter(true);

        animShow = new AlphaAnimation(0.0f, 1.0f);
        animShow.setDuration(400);
        animShow.setFillAfter(true);
    }

    public void setTopView(boolean isSearch) {
        if (isSearch == true) {
            headerAnother.setVisibility(View.GONE);
            headerAnother.startAnimation(animHide);

            headerSearch.startAnimation(animShow);
            headerSearch.setVisibility(View.VISIBLE);
        } else {
            headerSearch.startAnimation(animHide);
            headerSearch.setVisibility(View.GONE);
            headerAnother.startAnimation(animShow);
            headerAnother.setVisibility(View.VISIBLE);
        }
    }

    public void setTopView(boolean isSearch, float alpha) {
        if (isSearch == true) {
            headerAnother.setVisibility(View.GONE);
            headerSearch.setVisibility(View.VISIBLE);
        } else {
            if (alpha > 0.3f) {
                headerSearch.setVisibility(View.GONE);
                headerAnother.setVisibility(View.VISIBLE);
            }
        }
        headerAnother.setAlpha(alpha);
        headerSearch.setAlpha(1 - alpha);
    }

    public void setOnBarcodeClickListener(OnClickListener listener) {
        ivBarcode2.setOnClickListener(listener);
    }
}
