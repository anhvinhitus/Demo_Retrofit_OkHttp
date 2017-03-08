package vn.com.vng.zalopay.webapp;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import com.zalopay.ui.widget.IconFontDrawable;

public class WebAppActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return WebAppFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_common_actionbar_white;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getToolbar().setNavigationIcon(
                new IconFontDrawable(this)
                        .setIcon(R.string.general_backandroid)
                        .setResourcesColor(R.color.colorWebAppPrimaryText)
                        .setDpSize(18)
        );
    }
}
