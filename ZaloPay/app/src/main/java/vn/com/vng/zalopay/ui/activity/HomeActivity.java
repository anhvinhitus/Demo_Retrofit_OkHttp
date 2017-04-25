package vn.com.vng.zalopay.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnPageChange;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.react.base.AbstractReactActivity;
import vn.com.vng.zalopay.react.base.HomePagerAdapter;
import vn.com.vng.zalopay.ui.widget.HomeBottomNavigationView;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.ui.widget.BottomNavigationDrawable;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.BottomNavigationViewHelper;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

public class HomeActivity extends AbstractReactActivity implements IHomeView {

    public static final String TAG = "HomeActivity";

    public static boolean EVENT_FIRST_TOUCH_HOME = true;
    public static boolean EVENT_FIRST_TOUCH_NEARBY = true;
    public static boolean EVENT_FIRST_TOUCH_PROMOTION = true;
    public static boolean EVENT_FIRST_TOUCH_ME = true;

    @Inject
    HomePresenter mPresenter;

    @BindView(R.id.pager)
    ViewPager mViewPager;

    private int mCurrentPosition = 0;

    @BindView(R.id.navigation)
    HomeBottomNavigationView mBottomNavigationView;

    HomePagerAdapter mHomePagerAdapter;

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
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.attachView(this);
        mPresenter.initialize();
        mHomePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mViewPager.getLayoutParams();
        params.setMargins(0, AndroidUtils.getStatusBarHeight(this), 0, (int) AndroidUtils.dpToPixels(this, 56));
        mViewPager.setAdapter(mHomePagerAdapter);
        mViewPager.setOffscreenPageLimit(mHomePagerAdapter.getCount() - 1);
        mBottomNavigationView.setViewPager(mViewPager);
        selectTabMenu(getIntent());
    }


    @OnPageChange(value = R.id.pager, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int newPosition) {

        Fragment fragmentToShow = mHomePagerAdapter.getPage(newPosition);
        if (fragmentToShow instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToShow).onStartFragment();
        }

        Fragment fragmentToHide = mHomePagerAdapter.getPage(mCurrentPosition);
        if (fragmentToHide instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToHide).onStopFragment();
        }

        mCurrentPosition = newPosition;
    }


    private void trackEvent(int position) {
        switch (position) {
            case HomePagerAdapter.TAB_MAIN_INDEX:
                if (EVENT_FIRST_TOUCH_HOME) {
                    ZPAnalytics.trackEvent(ZPEvents.TOUCHTABHOMEFIRST);
                    EVENT_FIRST_TOUCH_HOME = false;
                }
                ZPAnalytics.trackEvent(ZPEvents.TOUCHTABHOME);
                break;
            case HomePagerAdapter.TAB_SHOW_SHOW_INDEX:
                if (EVENT_FIRST_TOUCH_NEARBY) {
                    ZPAnalytics.trackEvent(ZPEvents.TOUCHTABNEARBYFIRST);
                    EVENT_FIRST_TOUCH_NEARBY = false;
                }
                ZPAnalytics.trackEvent(ZPEvents.TOUCHTABNEARBY);
                break;
            case HomePagerAdapter.TAB_PROMOTION_INDEX:
                if (EVENT_FIRST_TOUCH_PROMOTION) {
                    ZPAnalytics.trackEvent(ZPEvents.TOUCHTABPROMOTIONFIRST);
                    EVENT_FIRST_TOUCH_PROMOTION = false;
                }
                ZPAnalytics.trackEvent(ZPEvents.TOUCHTABPROMOTION);
                break;
            case HomePagerAdapter.TAB_PERSONAL_INDEX:
                if (EVENT_FIRST_TOUCH_ME) {
                    ZPAnalytics.trackEvent(ZPEvents.TOUCHTABMEFIRST);
                    EVENT_FIRST_TOUCH_ME = false;
                }
                ZPAnalytics.trackEvent(ZPEvents.TOUCHTABME);
                break;
        }
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        DialogHelper.showLoading(this);
    }

    @Override
    public void hideLoading() {
        DialogHelper.hideLoading();
    }

    @Override
    public void showError(String message) {
        DialogHelper.showNotificationDialog(this, message);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void refreshIconFont() {
        if (mBottomNavigationView != null) {
            mBottomNavigationView.initTabIconFont();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        selectTabMenu(intent);
    }

    private void selectTabMenu(Intent intent) {
        int tab_menu = intent.getIntExtra("tab_menu", -1);
        if (tab_menu >= 0 && tab_menu < mHomePagerAdapter.getCount()) {
            mBottomNavigationView.setSelected(tab_menu);
        }
    }
}
