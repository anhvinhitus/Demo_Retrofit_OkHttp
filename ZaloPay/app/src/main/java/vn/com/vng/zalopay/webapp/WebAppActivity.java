package vn.com.vng.zalopay.webapp;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class WebAppActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return WebAppFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar_white;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Drawable backArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back);
        backArrow.setColorFilter(ContextCompat.getColor(this, R.color.colorWebAppPrimaryText), PorterDuff.Mode.SRC_ATOP);
        getToolbar().setNavigationIcon(backArrow);
    }
}
