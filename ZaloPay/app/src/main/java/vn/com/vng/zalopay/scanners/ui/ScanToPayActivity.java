package vn.com.vng.zalopay.scanners.ui;

import android.content.Intent;
import android.os.Bundle;
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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class ScanToPayActivity extends BaseToolBarActivity {

    private ScanToPayPagerAdapter mSectionsPagerAdapter;

    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    int currentPosition = 0;

    private ImageView mRadarView;

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

        mSectionsPagerAdapter = new ScanToPayPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOffscreenPageLimit(2);

        setupTabIcons();
    }

    @OnPageChange(value = R.id.container, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int newPosition) {

        radarAnimation(newPosition);

        Fragment fragmentToShow = mSectionsPagerAdapter.getItem(newPosition);
        if (fragmentToShow instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToShow).onStartFragment();
        }

        Fragment fragmentToHide = mSectionsPagerAdapter.getItem(currentPosition);
        if (fragmentToHide instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToHide).onStopFragment();
        }

        currentPosition = newPosition;
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

        if (ScanToPayPagerAdapter.TAB_TOTAL > ScanToPayPagerAdapter.TAB_SOUND) {
            TabLayout.Tab soundTab = mTabLayout.getTabAt(ScanToPayPagerAdapter.TAB_SOUND);
            if (soundTab != null) {
                soundTab.setCustomView(genTabView("Âm thanh", R.drawable.ic_pay_tab_sound));
            }
        }

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
        super.onDestroy();
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.NFC_SCANNING);
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.BLE_SCANNING);
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.SOUND_SCANNING);
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.QR_SCANNING);
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

        Fragment fragment = mSectionsPagerAdapter.getPage(currentPosition);
        if (fragment instanceof ScanNFCFragment) {
            ((ScanNFCFragment) fragment).onNewIntent(intent);
        }
    }
}
