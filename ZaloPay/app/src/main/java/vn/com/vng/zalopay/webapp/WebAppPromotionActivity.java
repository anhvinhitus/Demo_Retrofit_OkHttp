package vn.com.vng.zalopay.webapp;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class WebAppPromotionActivity extends UserBaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return WebAppPromotionFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_activity_promotion;
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.PROMOTION_BANNERDETAIL;
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_TOUCH_BACK);
    }
}
