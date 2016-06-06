package vn.com.vng.zalopay.scanners.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.controller.NFCReaderPresenter;
import vn.com.vng.zalopay.scanners.ui.beacon.BeaconDevice;
import vn.com.vng.zalopay.scanners.ui.fragment.ScanSoundFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class ScanToPayActivity extends BaseToolBarActivity implements CounterBeaconFragment.OnListFragmentInteractionListener {

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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        // should enable NFC reader handler
                        mNFCTabActivated = true;
                        mNFCReader.setupForegroundDispatch();
                    } else {
                        // should disable NFC reader handler
                        mNFCReader.stopForegroundDispatch();
                        mNFCTabActivated = false;
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
        } catch (NullPointerException e) {
            Timber.e(e, "Null exception");
        }

        mNFCReader.initialize();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        mNFCReader.handleDispatch(intent);
    }

    @Override
    protected void onResume() {
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
    protected void onPause() {
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

    @Override
    public void onListFragmentInteraction(BeaconDevice item) {
        Timber.i("Select item: %s", item);
    }

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
            switch (position) {
                case 0: {
                    ScanNFCFragment fragment = ScanNFCFragment.newInstance("Hello NFC", "abc");
                    fragment.setReaderPresenter(mNFCReader);
                    return fragment;
                }
                case 2: {
                    CounterBeaconFragment fragment = CounterBeaconFragment.newInstance(1);
                    return fragment;
                }
                default:
                    return ScanSoundFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NFC";
                case 1:
                    return "SOUND";
                case 2:
                    return "BEACON";
            }
            return null;
        }
    }
}
