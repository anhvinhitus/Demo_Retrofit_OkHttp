package vn.com.vng.zalopay.passport;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.jaeger.library.StatusBarUtil;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.analytics.ZPScreens;


/**
 * Created by hieuvm on 6/9/17.
 * *
 */
public class OnboardingActivity extends BaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.ONBOARDING, -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Drawable navigationIcon = getToolbar().getNavigationIcon();

        if (navigationIcon != null) {
            DrawableCompat.setTint(navigationIcon, ContextCompat.getColor(this, R.color.blue_008fe5));
            navigationIcon.setAlpha(255);
        }

        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, getToolbar());
    }

    /**
     * https://fabric.io/zalo-pay/android/apps/vn.com.vng.zalopay/issues/59751e45be077a4dcc05f9d8?time=last-seven-days
     */

    public BaseFragment getFragmentToHost() {
        if (getIntent() == null) {
            Timber.e("Intent onboarding activity is null");
            return null;
        }

        return OnboardingFragment.newInstance(getIntent().getExtras());
    }

    public int getResLayoutId() {
        return R.layout.activity_onboarding;
    }
}