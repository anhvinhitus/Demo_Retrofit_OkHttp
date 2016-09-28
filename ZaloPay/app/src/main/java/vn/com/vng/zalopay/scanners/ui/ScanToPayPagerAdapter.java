package vn.com.vng.zalopay.scanners.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.scanners.beacons.CounterBeaconFragment;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.scanners.sound.ScanSoundFragment;

/**
 * Created by AnhHieu on 9/27/16.
 * *
 */

final class ScanToPayPagerAdapter extends AbsFragmentPagerAdapter {

    static final int TAB_NFC = 1;
    static final int TAB_BEACON = 2;
    static final int TAB_SOUND = 3;
    static final int TAB_QR = 0;

    static final int TAB_TOTAL = 3;

    ScanToPayPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_QR:
                return QRCodeFragment.newInstance();
            case TAB_NFC:
                return ScanNFCFragment.newInstance();
            case TAB_BEACON:
                return CounterBeaconFragment.newInstance();
            case TAB_SOUND:
                return ScanSoundFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return TAB_TOTAL;
    }
}
