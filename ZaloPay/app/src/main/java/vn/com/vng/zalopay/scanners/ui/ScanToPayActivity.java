package vn.com.vng.zalopay.scanners.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.beacons.CounterBeaconFragment;
import vn.com.vng.zalopay.scanners.nfc.NFCReaderPresenter;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.scanners.sound.ScanSoundFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

public class ScanToPayActivity extends BaseToolBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private NFCReaderPresenter mNFCReader;
    private boolean mNFCTabActivated = false;

    private static final int TAB_TOTAL = 3;
    private static final int TAB_NFC = 1;
    private static final int TAB_BEACON = 2;
    private static final int TAB_SOUND = 3;
    private static final int TAB_QR = 0;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

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

        mNFCReader = new NFCReaderPresenter(this);
        try {
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            // Set up the ViewPager with the sections adapter.
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setOffscreenPageLimit(2);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Timber.d("Select page: %d", position);
                    if (position == TAB_NFC) {
                        // should enable NFC reader handler
                        mNFCTabActivated = true;
                        mNFCReader.setupForegroundDispatch();
                    } else {
                        // should disable NFC reader handler
                        mNFCReader.stopForegroundDispatch();
                        mNFCTabActivated = false;
                    }

                    if (mScanBeaconFragment != null) {
                        if (position == TAB_BEACON) {
                            mScanBeaconFragment.startScanning();
                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                            mRadarView.startAnimation(animation);
                        } else {
                            mScanBeaconFragment.stopScanning();
                            mRadarView.clearAnimation();
                        }
                    }

                    if (mScanSoundFragment != null) {
                        if (position == TAB_SOUND) {
                            mScanSoundFragment.startRecording();
                        } else {
                            mScanSoundFragment.stopRecording();
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


            mTabLayout.setupWithViewPager(mViewPager);
            setupTabIcons();

        } catch (NullPointerException e) {
            Timber.e(e, "Null exception");
        }

        mNFCReader.initialize();
        handleIntent(getIntent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBluetoothPermission();
        }
    }

    private void setupTabIcons() {

        try {

            TabLayout.Tab nfcTab = mTabLayout.getTabAt(TAB_NFC);
            if (nfcTab != null) {
                nfcTab.setCustomView(genTabView("NFC", R.drawable.ic_pay_tab_nfc));
            }

            TabLayout.Tab qrTab = mTabLayout.getTabAt(TAB_QR);
            if (qrTab != null) {
                qrTab.setCustomView(genTabView("QR", R.drawable.ic_pay_tab_qr));
            }

            TabLayout.Tab blTab = mTabLayout.getTabAt(TAB_BEACON);
            if (blTab != null) {
                blTab.setCustomView(genTabBeacon("Bluetooth"));
            }

            if (TAB_TOTAL > TAB_SOUND) {

                mTabLayout.getTabAt(TAB_SOUND).setCustomView(genTabView("Âm thanh", R.drawable.ic_pay_tab_sound));
            }

            if (qrTab != null && qrTab.getCustomView() != null) {
                qrTab.getCustomView().setSelected(true);
            }
        } catch (NullPointerException e) {
            Timber.w(e, "Should not happened in ScanToPayActivity");
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


    private ImageView mRadarView;

    private View genTabBeacon(String tabName) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab_beacon, null);
        TextView tabNameView = (TextView) view.findViewById(R.id.tabName);
        tabNameView.setText(tabName);
        mRadarView = (ImageView) view.findViewById(R.id.radar);
        return view;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkBluetoothPermission() {
        // Android M Permission check 
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogManager.showSweetDialogConfirm(getActivity(), "Xin hãy cho phép Zalo Pay sử dụng thông tin vị trí để hỗ trợ tốt thanh toán bằng Bluetooth", getString(R.string.ok), getString(R.string.cancel), new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                }

                @Override
                public void onOKevent() {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            Constants.Permission.REQUEST_COARSE_LOCATION);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        Timber.d("onRequestPermissionsResult: requestCode %s grantResults %s permission %s", requestCode, grantResults.length, grantResults[0]);

        switch (requestCode) {
            case Constants.Permission.REQUEST_COARSE_LOCATION: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("coarse location permission granted");
                } else {
                    DialogManager.showSweetDialogCustom(getActivity(), "Do Zalo Pay chưa được cấp quyền lấy thông tin vị trí, Zalo Pay chưa thể hỗ trợ thanh toán bằng Bluetooth", getString(R.string.ok), DialogManager.NORMAL_TYPE, new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                        }

                        @Override
                        public void onOKevent() {
                        }
                    });
                }
                return;
            }
        }
    }

    private void handleIntent(Intent intent) {
        mNFCReader.handleDispatch(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        if (mNFCTabActivated) {
            mNFCReader.setupForegroundDispatch();
        }
    }

    @Override
    public void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
//        stopForegroundDispatch(this, mNfcAdapter);
        mNFCReader.stopForegroundDispatch();

        super.onPause();
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
        handleIntent(intent);
    }

    private ScanNFCFragment mScanNFCFragment;
    private ScanSoundFragment mScanSoundFragment;
    private CounterBeaconFragment mScanBeaconFragment;
    private QRCodeFragment mScanQRCodeFragment;


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Timber.d("SectionsPagerAdapter getItem: %d", position);
            switch (position) {
                case TAB_NFC: {
                    mScanNFCFragment = ScanNFCFragment.newInstance();
                    mScanNFCFragment.setReaderPresenter(mNFCReader);
                    return mScanNFCFragment;
                }
                case TAB_BEACON: {
//                    return ScanSoundFragment.newInstance();
                    mScanBeaconFragment = CounterBeaconFragment.newInstance();
                    return mScanBeaconFragment;
                }

                case TAB_QR:
                    mScanQRCodeFragment = QRCodeFragment.newInstance();
                    return mScanQRCodeFragment;

                case TAB_SOUND:
                    mScanSoundFragment = ScanSoundFragment.newInstance();
                    return mScanSoundFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return TAB_TOTAL;
        }
    }
}
