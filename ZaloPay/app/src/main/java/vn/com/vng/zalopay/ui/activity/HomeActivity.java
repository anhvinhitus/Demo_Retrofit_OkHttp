package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.base.ExternalReactFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;

public class HomeActivity extends BaseToolBarActivity {

    private Fragment mHomeFragment;
    private Fragment mShowShowFragment;
    private Fragment mProfileFragment;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_home_new;
  }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHomeFragment = ZaloPayFragment.newInstance();
        mShowShowFragment = ExternalReactFragment.newInstance(PaymentAppConfig.getAppResource(22));
        mProfileFragment = ProfileFragment.newInstance();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, mHomeFragment);
        fragmentTransaction.commit();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.menu_home:
                    fragment = mHomeFragment;
                    break;
                case R.id.menu_nearby:
                    fragment = mShowShowFragment;
                    break;
                case R.id.menu_profile:
                    fragment = mProfileFragment;
                    break;
            }
            if (fragment != null) {
                FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.frameLayout, fragment);
                fragmentTransaction1.commit();
            }
            return true;
        });
    }

}
