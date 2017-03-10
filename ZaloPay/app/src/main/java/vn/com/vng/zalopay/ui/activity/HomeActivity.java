package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.base.AbstractReactActivity;
import vn.com.vng.zalopay.react.base.ExternalReactFragment;
import vn.com.vng.zalopay.react.base.HomePagerAdapter;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;

public class HomeActivity extends AbstractReactActivity {

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    private HomePagerAdapter mHomePagerAdapter;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_home_new;
  }

    @Override
    public Fragment getReactFragment() {
        return null;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHomePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mHomePagerAdapter);

        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    mViewPager.setCurrentItem(0);
                    break;
                case R.id.menu_nearby:
                    mViewPager.setCurrentItem(1);
                    break;
                case R.id.menu_profile:
                    mViewPager.setCurrentItem(2);
                    break;
            }
            return true;
        });
    }

}
