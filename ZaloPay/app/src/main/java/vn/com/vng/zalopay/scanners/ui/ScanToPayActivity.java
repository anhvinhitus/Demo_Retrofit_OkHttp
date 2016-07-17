package vn.com.vng.zalopay.scanners.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.beacons.CounterBeaconFragment;
import vn.com.vng.zalopay.scanners.nfc.NFCReaderPresenter;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.scanners.sound.ScanSoundFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

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

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static final int TAB_TOTAL = 4;
    private static final int TAB_NFC = 0;
    private static final int TAB_BEACON = 2;
    private static final int TAB_SOUND = 3;
    private static final int TAB_QR = 1;

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
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Timber.e("Select page: %d", position);
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
                        } else {
                            mScanBeaconFragment.stopScanning();
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

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tablayout, null);
        tabOne.setText("NFC");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pay_tab_nfc, 0, 0);
        mTabLayout.getTabAt(TAB_NFC).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tablayout, null);
        tabTwo.setText("QR");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pay_tab_qr, 0, 0);
        mTabLayout.getTabAt(TAB_QR).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tablayout, null);
        tabThree.setText("Bluetooth");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pay_tab_bluetooth, 0, 0);
        mTabLayout.getTabAt(TAB_BEACON).setCustomView(tabThree);

        TextView tabFour = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tablayout, null);
        tabFour.setText("Âm thanh");
        tabFour.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pay_tab_sound, 0, 0);
        mTabLayout.getTabAt(TAB_SOUND).setCustomView(tabFour);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkBluetoothPermission() {
        // Android M Permission check 
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
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
                    mScanBeaconFragment = CounterBeaconFragment.newInstance(1);
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
