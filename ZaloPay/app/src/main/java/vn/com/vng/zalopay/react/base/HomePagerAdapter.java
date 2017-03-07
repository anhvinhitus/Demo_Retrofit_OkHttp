package vn.com.vng.zalopay.react.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

/**
 * Created by hieuvm on 2/28/17.
 */

public class HomePagerAdapter extends AbsFragmentPagerAdapter {

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
