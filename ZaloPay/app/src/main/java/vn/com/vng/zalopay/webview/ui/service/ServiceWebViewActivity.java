package vn.com.vng.zalopay.webview.ui.service;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.webview.ui.WebViewActivity;

public class ServiceWebViewActivity extends WebViewActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ServiceWebViewFragment.newInstance(getIntent().getExtras());
    }
}
