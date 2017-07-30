package vn.com.vng.zalopay.webview.ui.service;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.webview.ui.WebViewActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class ServiceWebViewActivity extends WebViewActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.SERVICE, -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ServiceWebViewFragment.newInstance(getIntent().getExtras());
    }
}
