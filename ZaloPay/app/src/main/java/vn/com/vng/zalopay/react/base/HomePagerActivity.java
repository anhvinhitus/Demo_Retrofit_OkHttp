package vn.com.vng.zalopay.react.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.Gravity;

import butterknife.BindView;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 2/28/17.
 */

public class HomePagerActivity extends AbstractReactActivity {

    @Override
    public Fragment getReactFragment() {
        return null;
    }

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.pagerTitle)
    PagerTitleStrip mPagerTitle;

    HomePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mPagerTitle.setGravity(Gravity.CENTER);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_home_pager;
    }
}
