package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.ui.fragment.tabmain.UserBaseTabFragment;
import vn.com.vng.zalopay.ui.presenter.HomeCollapseHeaderPresenter;
import vn.com.vng.zalopay.ui.view.IHomeCollapseHeaderView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.utility.CurrencyUtil;

/**
 * Created by Datnt10 on 5/10/17.
 * Display PaymentApps in Grid layout
 * Handle ui and events of list payment app
 */

public class HomeCollapseHeaderFragment extends UserBaseTabFragment implements IHomeCollapseHeaderView {
    @Inject
    HomeCollapseHeaderPresenter presenter;

    @BindView(R.id.tv_balance)
    TextView mBalanceView;

    private boolean mClickMore = true;

    public static HomeCollapseHeaderFragment newInstance() {
        Bundle args = new Bundle();
        HomeCollapseHeaderFragment fragment = new HomeCollapseHeaderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
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
        presenter.attachView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.initialize();
    }

    @Override
    public void setBalance(long balance) {
        mBalanceView.setText(CurrencyUtil.formatCurrency(balance, false));
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
        if (mClickMore) {
            mClickMore = false;
            navigator.startLinkCardActivity(getActivity());
            ZPAnalytics.trackEvent(ZPEvents.HOME_TOUCH_LINKBANK);
        }
        AndroidUtils.runOnUIThread(() -> mClickMore = true, 200);
    }

    @OnClick(R.id.btn_scan_to_pay)
    public void onScanToPayClick() {
        getAppComponent().monitorTiming().startEvent(MonitorEvents.NFC_SCANNING);
        getAppComponent().monitorTiming().startEvent(MonitorEvents.SOUND_SCANNING);
        getAppComponent().monitorTiming().startEvent(MonitorEvents.BLE_SCANNING);
        navigator.startScanToPayActivity(getActivity());
        ZPAnalytics.trackEvent(ZPEvents.HOME_TOUCH_SCANQR);
    }

    @OnClick(R.id.btn_balance)
    public void onClickBalance() {
        navigator.startBalanceManagementActivity(getContext());
        ZPAnalytics.trackEvent(ZPEvents.HOME_TOUCH_BALANCE);
    }
}
