package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.react.base.AbstractReactActivity;
import vn.com.vng.zalopay.react.base.HomePagerAdapter;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;

public class HomeActivity extends AbstractReactActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.txtTitle)
    TextView mTxtTitle;

    @BindView(R.id.edtSearch)
    EditText mEdtSearch;

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    private HomePagerAdapter mHomePagerAdapter;
    private FrameLayout.LayoutParams mToolbarParams;
    private int mMarginBottom;

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

        initToolbar();

        mHomePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mHomePagerAdapter);

        mToolbarParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mMarginBottom = (int) AndroidUtils.dpToPixels(getActivity(), 56);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == HomePagerAdapter.TAB_MAIN_INDEX) {
                    showToolbarSearch();
                } else if (position == HomePagerAdapter.TAB_SHOW_SHOW_INDEX) {
                    hideToolbar();
                } else if (position == HomePagerAdapter.TAB_PROFILE_INDEX) {
                    showToolbarTitle(R.string.title_activity_profile);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    mViewPager.setCurrentItem(HomePagerAdapter.TAB_MAIN_INDEX);
                    break;
                case R.id.menu_nearby:
                    mViewPager.setCurrentItem(HomePagerAdapter.TAB_SHOW_SHOW_INDEX);
                    break;
                case R.id.menu_profile:
                    mViewPager.setCurrentItem(HomePagerAdapter.TAB_PROFILE_INDEX);
                    break;
            }
            return true;
        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void hideToolbar() {
        mToolbarParams.setMargins(0, 0, 0, mMarginBottom);
        mViewPager.setLayoutParams(mToolbarParams);
        getSupportActionBar().hide();
    }

    private void showToolbarSearch() {
        mToolbarParams.setMargins(0, getSupportActionBar().getHeight(), 0, mMarginBottom);
        mViewPager.setLayoutParams(mToolbarParams);
        mEdtSearch.setVisibility(View.VISIBLE);
        mTxtTitle.setVisibility(View.GONE);
        getSupportActionBar().show();
    }

    private void showToolbarTitle(@StringRes int strResource) {
        mEdtSearch.setVisibility(View.GONE);
        mTxtTitle.setVisibility(View.VISIBLE);
        mTxtTitle.setText(strResource);
        getSupportActionBar().show();
        mToolbarParams.setMargins(0, getSupportActionBar().getHeight(), 0, mMarginBottom);
        mViewPager.setLayoutParams(mToolbarParams);
    }
}
