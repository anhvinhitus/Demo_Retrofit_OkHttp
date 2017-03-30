package vn.com.vng.zalopay.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.textview.RoundTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.react.base.AbstractReactActivity;
import vn.com.vng.zalopay.react.base.HomePagerAdapter;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;
import vn.com.vng.zalopay.ui.toolbar.HeaderView;
import vn.com.vng.zalopay.ui.toolbar.HeaderViewTop;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

public class HomeActivity extends AbstractReactActivity implements IHomeView, AppBarLayout.OnOffsetChangedListener {

    @Inject
    HomePresenter mPresenter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.toolbar_header_view)
    HeaderViewTop toolbarHeaderView;

    @BindView(R.id.float_header_view)
    HeaderView headerView;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.tv_balance)
    TextView mBalanceView;

    @BindView(R.id.tvNotificationCount)
    RoundTextView mNotifyView;

//    @BindView(R.id.txtTitle)
//    TextView mTxtTitle;
//
//    @BindView(R.id.edtSearch)
//    EditText mEdtSearch;

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    private static boolean isToolbarExpanded = true;

    /*
    * Click event for 3 main button on collapse toolbar
    */
    @OnClick(R.id.btn_link_card)
    public void onBtnLinkCardClick() {
        navigator.startLinkCardActivity(getActivity());
        ZPAnalytics.trackEvent(ZPEvents.TAPMANAGECARDS);
    }

    @OnClick(R.id.btn_scan_to_pay)
    public void onScanToPayClick() {
        getAppComponent().monitorTiming().startEvent(MonitorEvents.NFC_SCANNING);
        getAppComponent().monitorTiming().startEvent(MonitorEvents.SOUND_SCANNING);
        getAppComponent().monitorTiming().startEvent(MonitorEvents.BLE_SCANNING);
        navigator.startScanToPayActivity(getActivity());
    }

    @OnClick(R.id.btn_balance)
    public void onClickBalance() {
        navigator.startBalanceManagementActivity(getContext());
    }

    @OnClick(R.id.header_top_rl_notification)
    public void onBtnNotificationClick() {
        navigator.startMiniAppActivity(getActivity(), ModuleName.NOTIFICATIONS);
        ZPAnalytics.trackEvent(ZPEvents.TAPNOTIFICATIONBUTTON);
    }

    @OnClick(R.id.header_view_top_qrcode)
    public void onClickQRCodeInToolbar() {
        navigator.startScanToPayActivity(this);
    }

    @OnClick(R.id.header_view_top_linkbank)
    public void onClickLinkBankInToolbar() {
        navigator.startLinkCardActivity(this);
    }

    @OnClick(R.id.header_view_top_search)
    public void onClickSearchInToolbar() {
        showToast("Event search clicked!");
    }

//    private FrameLayout.LayoutParams mToolbarParams;
//    private int mMarginBottom;

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

        initToolbar();

        HomePagerAdapter mHomePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mHomePagerAdapter);
        mViewPager.setOffscreenPageLimit(mHomePagerAdapter.getCount() - 1);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == HomePagerAdapter.TAB_MAIN_INDEX) {
                    setToolbarViewStatus(HomePagerAdapter.TAB_MAIN_INDEX);
                    mPresenter.subscibeBusComponent();

                } else if (position == HomePagerAdapter.TAB_SHOW_SHOW_INDEX) {
                    setToolbarViewStatus(HomePagerAdapter.TAB_SHOW_SHOW_INDEX);
                    mPresenter.unregisterBusComponent();

                } else if (position == HomePagerAdapter.TAB_PERSONAL_INDEX) {
                    setToolbarViewStatus(HomePagerAdapter.TAB_PERSONAL_INDEX);
                    mPresenter.unregisterBusComponent();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Set collapsing behavior
        appBarLayout.addOnOffsetChangedListener(this);

        updateIconFontState(HomePagerAdapter.TAB_MAIN_INDEX);

        //Use this function to show titles of all menu elements when bottomNavigationBar has 4 tabs.
        //BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);

        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    updateIconFontState(HomePagerAdapter.TAB_MAIN_INDEX);
                    mViewPager.setCurrentItem(HomePagerAdapter.TAB_MAIN_INDEX);
                    break;
                case R.id.menu_nearby:
                    updateIconFontState(HomePagerAdapter.TAB_SHOW_SHOW_INDEX);
                    mViewPager.setCurrentItem(HomePagerAdapter.TAB_SHOW_SHOW_INDEX);
                    break;
                case R.id.menu_profile:
                    updateIconFontState(HomePagerAdapter.TAB_PERSONAL_INDEX);
                    mViewPager.setCurrentItem(HomePagerAdapter.TAB_PERSONAL_INDEX);
                    break;
            }
            return true;
        });
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
        appBarLayout.removeOnOffsetChangedListener(this);
        super.onDestroy();
    }

    private void updateIconFontState(int currentIndex) {
        Menu menu = mBottomNavigationView.getMenu();
        switch (currentIndex) {
            case HomePagerAdapter.TAB_MAIN_INDEX:
                menu.getItem(HomePagerAdapter.TAB_MAIN_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_home_active)
                                .setResourcesColor(R.color.colorPrimary)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                menu.getItem(HomePagerAdapter.TAB_SHOW_SHOW_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_showshow)
                                .setResourcesColor(R.color.txt_item_sub)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                menu.getItem(HomePagerAdapter.TAB_PERSONAL_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_personal)
                                .setResourcesColor(R.color.txt_item_sub)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                break;
            case HomePagerAdapter.TAB_SHOW_SHOW_INDEX:
                menu.getItem(HomePagerAdapter.TAB_MAIN_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_home)
                                .setResourcesColor(R.color.txt_item_sub)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                menu.getItem(HomePagerAdapter.TAB_SHOW_SHOW_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_showshow_active)
                                .setResourcesColor(R.color.colorPrimary)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                menu.getItem(HomePagerAdapter.TAB_PERSONAL_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_personal)
                                .setResourcesColor(R.color.txt_item_sub)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                break;
            case HomePagerAdapter.TAB_PERSONAL_INDEX:
                menu.getItem(HomePagerAdapter.TAB_MAIN_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_home)
                                .setResourcesColor(R.color.txt_item_sub)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                menu.getItem(HomePagerAdapter.TAB_SHOW_SHOW_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_showshow)
                                .setResourcesColor(R.color.txt_item_sub)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                menu.getItem(HomePagerAdapter.TAB_PERSONAL_INDEX)
                        .setIcon(new IconFontDrawable(this)
                                .setIcon(R.string.tab_personal_active)
                                .setResourcesColor(R.color.colorPrimary)
                                .setResourcesSize(R.dimen.font_size_tab_icon));
                break;
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getToolbarHeight() {
        int result = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return result;
    }

    private void setToolbarViewStatus(int status) {
        CoordinatorLayout.LayoutParams coordinatorLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        switch (status) {
            case HomePagerAdapter.TAB_MAIN_INDEX:
                coordinatorLayoutParams.height = (int) AndroidUtils.dpToPixels(getActivity(), 180);
                headerView.setVisibility(View.VISIBLE);

                toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);

                collapsingToolbarLayout.setLayoutParams(toolbarLayoutParams);
                appBarLayout.setExpanded(isToolbarExpanded, false);
                appBarLayout.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
                break;

            case HomePagerAdapter.TAB_SHOW_SHOW_INDEX:
                coordinatorLayoutParams.height = getStatusBarHeight();
                headerView.setVisibility(View.GONE);

                toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);

                appBarLayout.setVisibility(View.GONE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                break;

            case HomePagerAdapter.TAB_PERSONAL_INDEX:
                coordinatorLayoutParams.height = getStatusBarHeight() + getToolbarHeight();
                headerView.setVisibility(View.GONE);

                toolbarHeaderView.setHeaderTopStatus(2, 0.0f);

                toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);

                appBarLayout.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
                break;
        }
    }

//    private void hideToolbar() {
//        mToolbarParams.setMargins(0, 0, 0, mMarginBottom);
//        mViewPager.setLayoutParams(mToolbarParams);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().hide();
//        }
//    }

//    private void showToolbarSearch() {
//        if (getSupportActionBar() != null) {
//            mToolbarParams.setMargins(0, getSupportActionBar().getHeight(), 0, mMarginBottom);
//            mViewPager.setLayoutParams(mToolbarParams);
//            mEdtSearch.setVisibility(View.VISIBLE);
//            mTxtTitle.setVisibility(View.GONE);
//            getSupportActionBar().show();
//        }
//    }

//    private void showToolbarTitle(@StringRes int strResource) {
//        if (getSupportActionBar() != null) {
////            mEdtSearch.setVisibility(View.GONE);
////            mTxtTitle.setVisibility(View.VISIBLE);
////            mTxtTitle.setText(strResource);
//            getSupportActionBar().show();
////            mToolbarParams.setMargins(0, getSupportActionBar().getHeight(), 0, mMarginBottom);
////            mViewPager.setLayoutParams(mToolbarParams);
//        }
//    }

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
        if (mViewPager != null) {
            updateIconFontState(mViewPager.getCurrentItem());
        }
    }

    @Override
    public void setBalance(long balance) {
        String _temp = CurrencyUtil.formatCurrency(balance, true);

        SpannableString span = new SpannableString(_temp);
        span.setSpan(new RelativeSizeSpan(0.8f), _temp.indexOf(CurrencyUtil.CURRENCY_UNIT), _temp.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mBalanceView.setText(span);
    }

    @Override
    public void setTotalNotify(int total) {
        if (mNotifyView != null) {
            if (mNotifyView.isShown()) {
                mNotifyView.show(total);
            } else {
                mNotifyView.show(total);
                if (total > 0) {
                    Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_grow);
                    mNotifyView.startAnimation(animation);
                }
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

        headerView.setAlpha(1 - percentage);

        if (percentage == 0f) {
            headerView.setVisibility(View.VISIBLE);
            toolbarHeaderView.setHeaderTopStatus(0, percentage);
            isToolbarExpanded = true;
        } else if (percentage > 0f && percentage <= 1f) {
            if(percentage > 0.5f) {
                headerView.setVisibility(View.GONE);
            } else {
                headerView.setVisibility(View.VISIBLE);
            }
            toolbarHeaderView.setHeaderTopStatus(1, percentage);
            isToolbarExpanded = false;
        }
    }
}
