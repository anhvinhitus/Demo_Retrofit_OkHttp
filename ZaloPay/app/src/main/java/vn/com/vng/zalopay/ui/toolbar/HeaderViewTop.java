package vn.com.vng.zalopay.ui.toolbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import vn.com.vng.zalopay.R;

/**
 * Created by anton on 11/12/15.
 */

public class HeaderViewTop extends LinearLayout {
    private RelativeLayout rlHeaderNormal, rlHeaderCollapsed;

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
        init();
    }

    protected void init() {
        rlHeaderNormal = (RelativeLayout) findViewById(R.id.header_top_rl_normal);
        rlHeaderCollapsed = (RelativeLayout) findViewById(R.id.header_top_rl_collapsed);
    }

    public void setTopView(boolean isNormal, float alpha) {
        if (isNormal == true) {
            rlHeaderCollapsed.setVisibility(View.GONE);
            rlHeaderNormal.setVisibility(View.VISIBLE);
        } else {
            if (alpha > 0.3f) {
                rlHeaderNormal.setVisibility(View.GONE);
                rlHeaderCollapsed.setVisibility(View.VISIBLE);
            }
        }
        rlHeaderCollapsed.setAlpha(alpha);
        rlHeaderNormal.setAlpha(1 - alpha);
    }
}
