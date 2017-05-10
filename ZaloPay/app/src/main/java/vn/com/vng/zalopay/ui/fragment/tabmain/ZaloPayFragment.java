package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.IconFontTextView;
import com.zalopay.ui.widget.textview.RoundTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.ui.fragment.HomeListAppFragment;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.toolbar.HeaderView;
import vn.com.vng.zalopay.ui.toolbar.HeaderViewTop;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 4/11/16.
 * Display PaymentApps in Grid layout
 */
public class ZaloPayFragment extends RuntimePermissionFragment implements IZaloPayView,
        AppBarLayout.OnOffsetChangedListener, FragmentLifecycle {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ZaloPayPresenter mPresenter;

    /*
    * Collapse toolbar view
    * */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.toolbar_header_view)
    HeaderViewTop mToolbarHeaderView;

    @BindView(R.id.float_header_view)
    HeaderView mHeaderView;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.home_tv_balance)
    IconFontTextView mBalanceView;

//    @BindView(R.id.tv_balance)
//    TextView mBalanceView;

    @BindView(R.id.tvNotificationCount)
    RoundTextView mNotifyView;

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_zalopay;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        mPresenter.attachView(this);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.initialize();
        initListAppFragment();
    }

    @Override
    public void onResume() {
        mPresenter.resume();
        // Set collapsing behavior
        mAppBarLayout.addOnOffsetChangedListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        mAppBarLayout.removeOnOffsetChangedListener(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }


    @Override
    public void showNetworkError() {

    }

    @Override
    public void setBalance(long balance) {
//        mBalanceView.setText(CurrencyUtil.spanFormatCurrency(balance, false));
        mBalanceView.setText(CurrencyUtil.formatCurrency(balance, false));
    }

    @Override
    public void setTotalNotify(int total) {
        if (mNotifyView == null) {
            return;
        }

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

    /*
    * Click event for main buttons on collapse toolbar
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
    public void onClickQRCodeOnToolbar() {
        navigator.startScanToPayActivity(getActivity());
    }

    @OnClick(R.id.header_view_top_linkbank)
    public void onClickLinkBankOnToolbar() {
        navigator.startLinkCardActivity(getActivity());
    }

    @OnClick(R.id.header_view_top_search)
    public void onClickSearchOnToolbar() {
        navigator.startSearchCategoryActivity(getContext());
    }

    @OnClick(R.id.header_top_rl_search_view)
    public void onClickSearchViewOnToolbar() {
        navigator.startSearchCategoryActivity(getContext());
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
        boolean isCollaped;

        mHeaderView.setAlpha(1 - percentage);

        if (percentage == 0f) {
            isCollaped = true;
            mHeaderView.setVisibility(View.VISIBLE);
        } else {
            isCollaped = false;
            if (percentage > 0.5f) {
                mHeaderView.setVisibility(View.GONE);
            } else {
                mHeaderView.setVisibility(View.VISIBLE);
            }
        }
        mToolbarHeaderView.setHeaderTopStatus(isCollaped, percentage);
    }

    @Override
    public void onStartFragment() {

    }

    @Override
    public void onStopFragment() {

    }

    private void initListAppFragment() {
        if (getFragmentManager().findFragmentById(R.id.home_fl_list_app_content) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            HomeListAppFragment homeListAppFragment = HomeListAppFragment.newInstance();
            ft.replace(R.id.home_fl_list_app_content, homeListAppFragment);
            ft.commit();
        }
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {

    }
}