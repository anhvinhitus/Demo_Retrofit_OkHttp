package vn.com.vng.zalopay.webview.ui.service;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.webview.ui.WebViewActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class ServiceWebViewActivity extends WebViewActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ServiceWebViewFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.SERVICE;
    }
}
