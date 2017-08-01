package vn.com.vng.zalopay.scanners.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnPageChange;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class ScanToPayActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.SCANQR, -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    private ScanToPayPagerAdapter mSectionsPagerAdapter;

    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    private int mCurrentPosition = 0;

    private ImageView mRadarView;

    private UserComponent mUserComponent;

    @Override
    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        mUserComponent = userComponent;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_scan_to_pay;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isUserSessionStarted()) {
            return;
        }

        mSectionsPagerAdapter = new ScanToPayPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOffscreenPageLimit(2);

        setupTabIcons();
    }

    @OnPageChange(value = R.id.container, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int newPosition) {
        if (mCurrentPosition != newPosition) {
            trackEvent(newPosition);
        }
        radarAnimation(newPosition);
        Fragment fragmentToShow = mSectionsPagerAdapter.getPage(newPosition);
        if (fragmentToShow instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToShow).onStartFragment();
        }

        Fragment fragmentToHide = mSectionsPagerAdapter.getPage(mCurrentPosition);
        if (fragmentToHide instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToHide).onStopFragment();
        }

        mCurrentPosition = newPosition;
    }

    private void radarAnimation(int newPosition) {
        if (mRadarView != null) {
            if (newPosition == ScanToPayPagerAdapter.TAB_BEACON) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                mRadarView.startAnimation(animation);
            } else {
                mRadarView.clearAnimation();
            }
        }
    }

    private void trackEvent(int position) {
        switch (position) {
            case ScanToPayPagerAdapter.TAB_QR:
                // ZPAnalytics.trackEvent(ZPEvents.SCANQR_TOUCH_NFC);
                break;
            case ScanToPayPagerAdapter.TAB_NFC:
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_TOUCH_NFC);
                break;
            case ScanToPayPagerAdapter.TAB_BEACON:
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_TOUCH_BLUETOOTH);
                break;
        }
    }

    private void setupTabIcons() {
        mTabLayout.setupWithViewPager(mViewPager);

        TabLayout.Tab nfcTab = mTabLayout.getTabAt(ScanToPayPagerAdapter.TAB_NFC);
        if (nfcTab != null) {
            nfcTab.setCustomView(genTabView("NFC", R.drawable.ic_pay_tab_nfc));
        }

        TabLayout.Tab qrTab = mTabLayout.getTabAt(ScanToPayPagerAdapter.TAB_QR);
        if (qrTab != null) {
            qrTab.setCustomView(genTabView("QR", R.drawable.ic_pay_tab_qr));
        }

        TabLayout.Tab blTab = mTabLayout.getTabAt(ScanToPayPagerAdapter.TAB_BEACON);
        if (blTab != null) {
            blTab.setCustomView(genTabBeacon("Bluetooth"));
        }

//        if (ScanToPayPagerAdapter.TAB_TOTAL > ScanToPayPagerAdapter.TAB_SOUND) {
//            TabLayout.Tab soundTab = mTabLayout.getTabAt(ScanToPayPagerAdapter.TAB_SOUND);
//            if (soundTab != null) {
//                soundTab.setCustomView(genTabView("Âm thanh", R.drawable.ic_pay_tab_sound));
//            }
//        }

        if (qrTab != null && qrTab.getCustomView() != null) {
            qrTab.getCustomView().setSelected(true);
        }
    }

    private View genTabView(String tabName, int resIcon) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tablayout, null);
        TextView tabNameView = (TextView) view.findViewById(R.id.tabName);
        ImageView tabIconView = (ImageView) view.findViewById(R.id.tabIcon);
        tabNameView.setText(tabName);
        tabIconView.setImageResource(resIcon);
        return view;
    }

    private View genTabBeacon(String tabName) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab_beacon, null);
        TextView tabNameView = (TextView) view.findViewById(R.id.tabName);
        tabNameView.setText(tabName);
        mRadarView = (ImageView) view.findViewById(R.id.radar);
        return view;
    }


    @Override
    protected void onDestroy() {

        if (!isUserSessionStarted()) {
            super.onDestroy();
            return;
        }
        mUserComponent = null;
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.NFC_SCANNING);
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.BLE_SCANNING);
//        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.SOUND_SCANNING);
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.QR_SCANNING);
        super.onDestroy();
    }

    private void trackBackEvent(int position) {
        switch (position) {
            case ScanToPayPagerAdapter.TAB_QR:
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_TOUCH_BACK);
                break;
            case ScanToPayPagerAdapter.TAB_NFC:
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_NFC_TOUCH_BACK);
                break;
            case ScanToPayPagerAdapter.TAB_BEACON:
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_BLUETOOTH_TOUCH_BACK);
                break;

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */

        Fragment fragment = mSectionsPagerAdapter.getPage(mCurrentPosition);
        if (fragment instanceof ScanNFCFragment) {
            ((ScanNFCFragment) fragment).onNewIntent(intent);
        }
    }

    @Override
    public UserComponent getUserComponent() {
        UserComponent userComponent = super.getUserComponent();
        if (userComponent != null) {
            return userComponent;
        }
        Timber.d("Get Activity UserComponent");
        return mUserComponent;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        trackBackEvent(mCurrentPosition);
    }

    @Override
    protected boolean clearUserSession(@Nullable String message) {
        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }

        return super.clearUserSession(message);
    }
}
