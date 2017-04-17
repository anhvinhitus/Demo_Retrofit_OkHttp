package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.MultiSwipeRefreshLayout;
import com.zalopay.ui.widget.textview.RoundTextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.vng.zalopay.ui.adapter.HomeAdapter;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.toolbar.HeaderView;
import vn.com.vng.zalopay.ui.toolbar.HeaderViewTop;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.HomeSpacingItemDecoration;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 4/11/16.
 * Display PaymentApps in Grid layout
 */
public class ZaloPayFragment extends RuntimePermissionFragment implements IZaloPayView,
        SwipeRefreshLayout.OnRefreshListener,
        HomeAdapter.OnClickItemListener,
        AppBarLayout.OnOffsetChangedListener, FragmentLifecycle {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final static int SPAN_COUNT_APPLICATION = 3;

    @Inject
    ZaloPayPresenter mPresenter;

    private HomeAdapter mHomeAdapter;

    @BindView(R.id.listView)
    RecyclerView mAppListView;

    @BindView(R.id.tvInternetConnection)
    TextView mTvInternetConnection;

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

    @BindView(R.id.tv_balance)
    TextView mBalanceView;

    @BindView(R.id.tvNotificationCount)
    RoundTextView mNotifyView;

    /*
    * View của menu
    * */
//    RoundTextView mNotifyView;

    @BindView(R.id.swipeRefresh)
    MultiSwipeRefreshLayout mSwipeRefreshLayout;

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
        mHomeAdapter = new HomeAdapter(getContext(), this, SPAN_COUNT_APPLICATION);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        mPresenter.attachView(this);

        mAppListView.setHasFixedSize(true);
        HomeSpacingItemDecoration itemDecoration = new HomeSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false);
        mAppListView.addItemDecoration(itemDecoration);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION);
        gridLayoutManager.setSpanSizeLookup(mHomeAdapter.getSpanSizeLookup());
        mAppListView.setLayoutManager(gridLayoutManager);
        mAppListView.setAdapter(mHomeAdapter);

        setInternetConnectionError(getString(R.string.exception_no_connection_tutorial),
                getString(R.string.check_internet));
        mSwipeRefreshLayout.setSwipeableChildren(R.id.listView);
        mSwipeRefreshLayout.setOnRefreshListener(this);

    }

    private void setInternetConnectionError(String message, String spannedMessage) {
        AndroidUtils.setSpannedMessageToView(mTvInternetConnection,
                message,
                spannedMessage,
                false, false, R.color.txt_check_internet,
                new ClickableSpanNoUnderline(ContextCompat.getColor(getContext(), R.color.txt_check_internet)) {
                    @Override
                    public void onClick(View widget) {
                        navigator.startTutorialConnectInternetActivity(getContext());
                    }
                });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.initialize();
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
        mAppListView.setAdapter(null);
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void setAppItems(List<AppResource> list) {
        mHomeAdapter.setAppItems(list);
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
    }

    @Override
    public void showWsConnectError() {
        if (mTvInternetConnection == null ||
                mTvInternetConnection.getVisibility() == View.VISIBLE) {
            return;
        }
        setInternetConnectionError(getString(R.string.exception_no_ws_connection),
                getString(R.string.check_internet));
        mTvInternetConnection.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNetworkError() {
        if (mTvInternetConnection == null) {
            return;
        }
        setInternetConnectionError(getString(R.string.exception_no_connection_tutorial),
                getString(R.string.check_internet));
        mTvInternetConnection.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNetworkError() {
        if (mTvInternetConnection == null ||
                mTvInternetConnection.getVisibility() == View.GONE) {
            return;
        }
        mTvInternetConnection.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        mPresenter.getListAppResource();
    }

    @Override
    public void setRefreshing(boolean val) {
        mSwipeRefreshLayout.setRefreshing(val);
    }

    @Override
    public void onClickAppItem(AppResource app, int position) {
        mPresenter.launchApp(app, position);
    }

    @Override
    public void setBalance(long balance) {
        mBalanceView.setText(CurrencyUtil.spanFormatCurrency(balance, false));
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
        showToast("Event search clicked!");
    }

    @OnClick(R.id.header_top_rl_search_view)
    public void onClickSearchViewOnToolbar() {
        showToast("Event search clicked");
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
}
