package vn.com.vng.zalopay.ui.toolbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Header top view (this view snap and change layout when toolbar collapsed)
 * Define
 */

public class HeaderViewTop extends LinearLayout {
    @BindView(R.id.header_top_rl_normal)
    View rlHeaderNormal;

    @BindView(R.id.header_top_rl_collapsed)
    View rlHeaderCollapsed;

    @BindView(R.id.header_top_rl_personal)
    View rlHeaderPersonal;

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
        ButterKnife.bind(this);
    }

    public void setHeaderTopStatus(int code, float alpha) {

        switch (code) {
            case 0:
                rlHeaderCollapsed.setVisibility(View.GONE);
                rlHeaderNormal.setVisibility(View.VISIBLE);
                rlHeaderPersonal.setVisibility(View.GONE);
                rlHeaderCollapsed.setAlpha(alpha);
                rlHeaderNormal.setAlpha(1 - alpha);
                break;
            case 1:
                rlHeaderPersonal.setVisibility(View.GONE);
                if (alpha > 0.3f) {
                    rlHeaderNormal.setVisibility(View.GONE);
                    rlHeaderCollapsed.setVisibility(View.VISIBLE);
                }
                rlHeaderCollapsed.setAlpha(alpha);
                rlHeaderNormal.setAlpha(1 - alpha);
                break;
            case 2:
                rlHeaderPersonal.setVisibility(View.VISIBLE);
                rlHeaderCollapsed.setVisibility(View.GONE);
                rlHeaderNormal.setVisibility(View.GONE);
                break;
        }
    }
}
