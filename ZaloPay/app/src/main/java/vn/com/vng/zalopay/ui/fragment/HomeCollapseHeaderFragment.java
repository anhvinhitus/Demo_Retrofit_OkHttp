package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.ui.presenter.HomeCollapseHeaderPresenter;
import vn.com.vng.zalopay.ui.view.IHomeCollapseHeaderView;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by Datnt10 on 5/10/17.
 * Display PaymentApps in Grid layout
 * Handle ui and events of list payment app
 */

public class HomeCollapseHeaderFragment extends BaseFragment implements IHomeCollapseHeaderView
        , FragmentLifecycle {
    @Inject
    HomeCollapseHeaderPresenter presenter;

//    @BindView(R.id.home_tv_balance)
//    IconFontTextView mBalanceView;

    @BindView(R.id.tv_balance)
    TextView mBalanceView;

    public static HomeCollapseHeaderFragment newInstance() {
        Bundle args = new Bundle();
        HomeCollapseHeaderFragment fragment = new HomeCollapseHeaderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        presenter.attachView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.initialize();
    }

    @Override
    public void setBalance(long balance) {
//        mBalanceView.setText(CurrencyUtil.spanFormatCurrency(balance, false));
        mBalanceView.setText(CurrencyUtil.formatCurrency(balance, false));
    }

    @Override
    public void onStartFragment() {

    }

    @Override
    public void onStopFragment() {

    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.zp_control_top_layout;
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
}
