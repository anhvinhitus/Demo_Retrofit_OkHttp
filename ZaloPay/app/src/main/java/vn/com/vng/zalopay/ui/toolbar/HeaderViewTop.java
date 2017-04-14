package vn.com.vng.zalopay.ui.toolbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Header top view (this view snap and change layout when toolbar collapsed)
 * Define view and child's view actions
 */

public class HeaderViewTop extends LinearLayout {
    @BindView(R.id.header_top_rl_search_view)
    View rlHeaderSearchView;

    @BindView(R.id.header_top_rl_collapsed)
    View rlHeaderCollapsed;

    public HeaderViewTop(Context context) {
        super(context);
    }

    public HeaderViewTop(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderViewTop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setHeaderTopStatus(boolean collapsed, float alpha) {

        if(collapsed) {
            rlHeaderCollapsed.setVisibility(View.GONE);
            rlHeaderSearchView.setVisibility(View.VISIBLE);
            rlHeaderCollapsed.setAlpha(alpha);
            rlHeaderSearchView.setAlpha(1 - alpha);
        } else {
            if (alpha > 0.3f) {
                rlHeaderSearchView.setVisibility(View.GONE);
                rlHeaderCollapsed.setVisibility(View.VISIBLE);
            }
            rlHeaderCollapsed.setAlpha(alpha);
            rlHeaderSearchView.setAlpha(1 - alpha);
        }

//        switch (code) {
//            case 0:
//                rlHeaderCollapsed.setVisibility(View.GONE);
//                rlHeaderSearchView.setVisibility(View.VISIBLE);
//                rlHeaderPersonal.setVisibility(View.GONE);
//                rlHeaderCollapsed.setAlpha(alpha);
//                rlHeaderSearchView.setAlpha(1 - alpha);
//                break;
//            case 1:
//                rlHeaderPersonal.setVisibility(View.GONE);
//                if (alpha > 0.3f) {
//                    rlHeaderSearchView.setVisibility(View.GONE);
//                    rlHeaderCollapsed.setVisibility(View.VISIBLE);
//                }
//                rlHeaderCollapsed.setAlpha(alpha);
//                rlHeaderSearchView.setAlpha(1 - alpha);
//                break;
//            case 2:
//                rlHeaderPersonal.setVisibility(View.VISIBLE);
//                rlHeaderCollapsed.setVisibility(View.GONE);
//                rlHeaderSearchView.setVisibility(View.GONE);
//                break;
//        }
    }
}
