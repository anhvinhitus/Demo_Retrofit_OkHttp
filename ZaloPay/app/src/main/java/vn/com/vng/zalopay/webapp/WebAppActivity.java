package vn.com.vng.zalopay.webapp;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

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

        final Drawable backArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        backArrow.setColorFilter(getResources().getColor(R.color.colorWebAppPrimaryText), PorterDuff.Mode.SRC_ATOP);
        getToolbar().setNavigationIcon(backArrow);
    }
}
