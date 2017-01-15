package vn.com.vng.zalopay.account.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.account.ui.fragment.ChangePinFragment;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinVerifyFragment;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public class ChangePinPagerAdapter extends AbsFragmentPagerAdapter {

    public ChangePinPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChangePinFragment.newInstance();
            case 1:
                return ChangePinVerifyFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
