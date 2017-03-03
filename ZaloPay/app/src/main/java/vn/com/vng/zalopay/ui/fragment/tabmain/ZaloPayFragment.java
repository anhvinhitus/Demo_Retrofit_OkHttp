package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.ui.adapter.ListAppRecyclerAdapter;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.Constants;
import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.getAppResource;

/**
 * Created by AnhHieu on 4/11/16.
 * Display PaymentApps in Grid layout
 */
public class ZaloPayFragment extends RuntimePermissionFragment implements ListAppRecyclerAdapter.OnClickAppListener,
        IZaloPayView, SwipeRefreshLayout.OnRefreshListener {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final static int SPAN_COUNT_APPLICATION = 3;
    private boolean isEnableShowShow;


    @Inject
    ZaloPayPresenter presenter;

    @BindView(R.id.home_top_layout)
    View mTopLayout;

    /* Advertisement START */
    @BindView(R.id.tvAdsSubContent)
    TextView mTvAdsSubContent;
    /* Advertisement END */

    private ListAppRecyclerAdapter mAdapter;
    private ListAppRecyclerAdapter mAdapterBottomApp;

    @BindView(R.id.listView)
    RecyclerView listView;

    @BindView(R.id.listViewBottom)
    RecyclerView listViewBottom;

    @BindView(R.id.tv_balance)
    TextView mBalanceView;

    @BindView(R.id.tvInternetConnection)
    TextView mTvInternetConnection;

    /*
    * View của menu
    * */
    RoundTextView mNotifyView;

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
        mAdapter = new ListAppRecyclerAdapter(getContext(), this);
        mAdapterBottomApp = new ListAppRecyclerAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        presenter.attachView(this);

        listView.setHasFixedSize(true);
        listView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION));
        listView.setNestedScrollingEnabled(false);
        listView.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false));
        listView.setAdapter(mAdapter);
        listView.setFocusable(false);


        listViewBottom.setHasFixedSize(true);
        listViewBottom.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION));
        listViewBottom.setNestedScrollingEnabled(false);
        listViewBottom.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false));
        listViewBottom.setAdapter(mAdapterBottomApp);
        listViewBottom.setFocusable(false);

        setInternetConnectionError(getString(R.string.exception_no_connection_tutorial),
                getString(R.string.check_internet));
        mSwipeRefreshLayout.setSwipeableChildren(R.id.scrollView);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        hideTextAds();
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

    private MenuItem mShowShowMenuItem;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_2, menu);
        MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View view = menuItem.getActionView();
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        mNotifyView = (RoundTextView) view.findViewById(R.id.tvNotificationCount);

        mShowShowMenuItem = menu.findItem(R.id.action_showshow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_showshow) {
            presenter.startPaymentApp(getAppResource(Constants.SHOW_SHOW));
            return true;
        } else if (id == R.id.action_notifications) {
            navigator.startMiniAppActivity(getActivity(), ModuleName.NOTIFICATIONS);
            ZPAnalytics.trackEvent(ZPEvents.TAPNOTIFICATIONBUTTON);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("activity created");
        presenter.initialize();
    }

    @Override
    public void onResume() {
        presenter.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        presenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    private void hideTextAds() {
        if (mTvAdsSubContent != null) {
            mTvAdsSubContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickAppListener(AppResource app, int position) {
        presenter.handleLaunchApp(app);
        this.logActionApp(position);
    }

    private void logActionApp(int position) {
        Timber.d("Tap on app at position %d", position);

        ZPAnalytics.trackEvent(sActionMap.get(position));
    }

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

    public void refreshIconFont() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if (mAdapterBottomApp != null) {
            mAdapterBottomApp.notifyDataSetChanged();
        }
        if (mTopLayout != null) {
            mTopLayout.invalidate();
        }
    }

    @Override
    public void refreshInsideApps(List<AppResource> list) {
        Timber.d("refreshInsideApps list: [%s]", list.size());
        if (mAdapter == null || mAdapterBottomApp == null) {
            return;
        }
        mAdapter.setData(presenter.getTopAndBottomApp(list,true));
        if(list.size() > presenter.mNumberTopApp) {
            mAdapterBottomApp.setData(presenter.getTopAndBottomApp(list, false));
        }
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
    public void enableShowShow(boolean isEnableShowShow) {
        if (mShowShowMenuItem != null) {
            mShowShowMenuItem.setVisible(isEnableShowShow);
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
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public void showErrorDialog(String error) {
        if (TextUtils.isEmpty(error)) {
            return;
        }
        super.showErrorDialog(error);
    }

    @Override
    public int getAppCount() {
        return mAdapter.getItemCount() + mAdapterBottomApp.getItemCount();
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
        presenter.getListAppResource();
    }

    @Override
    public void setRefreshing(boolean val) {
        mSwipeRefreshLayout.setRefreshing(val);
    }

    static SparseIntArray sActionMap;

    static {
        sActionMap = new SparseIntArray(15);
        sActionMap.put(0, ZPEvents.TAPAPPICON_1_1);
        sActionMap.put(1, ZPEvents.TAPAPPICON_1_2);
        sActionMap.put(2, ZPEvents.TAPAPPICON_1_3);
        sActionMap.put(3, ZPEvents.TAPAPPICON_2_1);
        sActionMap.put(4, ZPEvents.TAPAPPICON_2_2);
        sActionMap.put(5, ZPEvents.TAPAPPICON_2_3);
        sActionMap.put(6, ZPEvents.TAPAPPICON_3_1);
        sActionMap.put(7, ZPEvents.TAPAPPICON_3_2);
        sActionMap.put(8, ZPEvents.TAPAPPICON_3_3);
        sActionMap.put(9, ZPEvents.TAPAPPICON_4_1);
        sActionMap.put(10, ZPEvents.TAPAPPICON_4_2);
        sActionMap.put(11, ZPEvents.TAPAPPICON_4_3);
        sActionMap.put(12, ZPEvents.TAPAPPICON_5_1);
        sActionMap.put(13, ZPEvents.TAPAPPICON_5_2);
        sActionMap.put(14, ZPEvents.TAPAPPICON_5_3);
    }
}
