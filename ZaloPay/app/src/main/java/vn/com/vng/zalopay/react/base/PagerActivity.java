/*
package vn.com.vng.zalopay.react.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.scanners.ui.ScanToPayPagerAdapter;
import vn.com.vng.zalopay.ui.fragment.IntroAppFragment;

*/
/**
 * Created by hieuvm on 2/23/17.
 *//*


public class PagerActivity extends AbstractReactActivity {

    @Override
    public Fragment getReactFragment() {
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new HomeAdapter(getSupportFragmentManager());
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_scan_to_pay;
    }

    private HomeAdapter mAdapter;

    private static class HomeAdapter extends AbsFragmentPagerAdapter {

        public HomeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return IntroAppFragment.newInstance();
                case 1:
                    return InternalReactFragment.newInstance();
                case 2:
                    return ExternalReactFragment.newInstance();
                case 3:
                    return IntroAppFragment.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
*/
