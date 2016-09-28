package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.textview.RoundTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.banner.model.BannerInternalFunction;
import vn.com.vng.zalopay.banner.model.BannerType;
import vn.com.vng.zalopay.banner.ui.adapter.BannerPagerAdapter;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.ui.adapter.ListAppRecyclerAdapter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;
import vn.com.vng.zalopay.ui.widget.SmoothViewPager;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;


/**
 * Created by AnhHieu on 4/11/16.
 * Display PaymentApps in Grid layout
 */
public class ZaloPayFragment extends BaseMainFragment implements ListAppRecyclerAdapter.OnClickAppListener,
        IZaloPayView, BannerPagerAdapter.IBannerClick {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onScreenVisible() {
    }


    private final static int SPAN_COUNT_APPLICATION = 3;

    @Inject
    ZaloPayPresenter presenter;

    /* Advertisement START */
    @BindView(R.id.layoutBannerFullScreen)
    View mLayoutBannerFullScreen;

    @BindView(R.id.viewpager)
    SmoothViewPager mBannerViewpager;

    BannerPagerAdapter mBannerPagerAdapter;

    @BindView(R.id.indicator)
    SmartTabLayout mBannerIndicator;

    @BindView(R.id.tvAdsSubContent)
    TextView mTvAdsSubContent;
    /* Advertisement END */

    private ListAppRecyclerAdapter mAdapter;

    @BindView(R.id.listView)
    RecyclerView listView;

    @BindView(R.id.tv_balance)
    TextView mBalanceView;

    @BindView(R.id.tvInternetConnection)
    TextView mTvInternetConnection;

    /*
    * View của menu
    * */
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
        mAdapter = new ListAppRecyclerAdapter(getContext(), this);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        presenter.setView(this);

        listView.setHasFixedSize(true);
        listView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION));
        listView.setNestedScrollingEnabled(false);
        listView.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false));
        listView.setAdapter(mAdapter);
        listView.setFocusable(false);

        AndroidUtils.setSpannedMessageToView(mTvInternetConnection, R.string.exception_no_connection_tutorial, R.string.check_internet,
                false, false, R.color.txt_check_internet,
                new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startTutorialConnectInternetActivity(ZaloPayFragment.this.getContext());
                    }
                });

        hideTextAds();
    }

    public void getInsideApp() {
        presenter.listAppResource();
    }

    public void getBanners() {
        presenter.getBanners();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View view = menuItem.getActionView();
        mNotifyView = (RoundTextView) view.findViewById(R.id.tvNotificationCount);
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                navigator.startMiniAppActivity(getActivity(), ModuleName.NOTIFICATIONS);
                ZPAnalytics.trackEvent(ZPEvents.TAPNOTIFICATIONBUTTON);
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("activity created");
        mAdapter.setData(getListData());
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
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void showBannerAds(List<DBanner> banners) {
        Timber.d("showBannerAds banners [%s]", banners);
        if (banners == null || banners.size() <= 0) {
            if (mLayoutBannerFullScreen != null) {
                mLayoutBannerFullScreen.setVisibility(View.GONE);
            }
        } else {
            Timber.d("showBannerAds banners.size [%s]", banners.size());
            mBannerPagerAdapter = new BannerPagerAdapter(getContext(), banners, this);
            mBannerViewpager.setAdapter(mBannerPagerAdapter);
            mBannerIndicator.setViewPager(mBannerViewpager);
            mBannerViewpager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Timber.d("Banner viewpager onTouch action [%s]", event.getAction());
                    if (presenter != null) {
                        presenter.onTouchBanner(v, event);
                    }
                    return false;
                }
            });
            if (mLayoutBannerFullScreen != null) {
                mLayoutBannerFullScreen.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideTextAds() {
        if (mTvAdsSubContent != null) {
            mTvAdsSubContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickAppListener(AppResource app, int position) {
        Timber.d("onclick app %s %s ", app.appid, app.appname);
        if (app.appType == PaymentAppTypeEnum.NATIVE.getValue()) {
            if (app.appid == PaymentAppConfig.Constants.TRANSFER_MONEY) {
                navigator.startTransferMoneyActivity(getActivity());
            } else if (app.appid == PaymentAppConfig.Constants.RED_PACKET) {
                navigator.startMiniAppActivity(getActivity(), ModuleName.RED_PACKET);
            } else if (app.appid == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                navigator.startMyQrCode(getContext());
            } else {
                AppResource appResource = PaymentAppConfig.getAppResource(app.appid);
                if (appResource == null) {
                    appResource = new AppResource(app.appid);
                }
                navigator.startPaymentApplicationActivity(getActivity(), appResource);
            }
        } else if (app.appType == PaymentAppTypeEnum.WEBVIEW.getValue()) {
            presenter.startWebViewActivity(app);
        } else {
            showToast(getString(R.string.need_update_to_use));
        }

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

    List<AppResource> mListApps = null;

    private List<AppResource> getListData() {
        if (Lists.isEmptyOrNull(mListApps)) {
            mListApps = new ArrayList<>(PaymentAppConfig.APP_RESOURCE_LIST);
        }
        List<AppResource> appResourceList = presenter.getListAppResourceFromDB();
        if (!Lists.isEmptyOrNull(appResourceList)) {
            mListApps.addAll(appResourceList);
        }
        return mListApps;
    }

    @Override
    public void refreshInsideApps(List<AppResource> list) {
        Timber.d("refreshInsideApps list: [%s]", list.size());
        if (!Lists.isEmptyOrNull(mListApps)) {
            mListApps.clear();
        }
        mListApps = new ArrayList<>(PaymentAppConfig.APP_RESOURCE_LIST);
        if (!Lists.isEmptyOrNull(list)) {
            mListApps.addAll(list);
        }
        mAdapter.setData(mListApps);
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
        showErrorDialog(error, getString(R.string.txt_close), null);
    }

    @Override
    public void onSessionExpired() {
        getAppComponent().applicationSession().setMessageAtLogin(getString(R.string.exception_token_expired_message));
        getAppComponent().applicationSession().clearUserSession();
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
    public void changeBanner() {
        if (mBannerViewpager == null) {
            return;
        }
        int count = mBannerViewpager.getChildCount();
        if (count <= 0) {
            return;
        }
        int currentItem = mBannerViewpager.getCurrentItem();
        if (currentItem >= count) {
            currentItem = 0;
            mBannerViewpager.setCurrentItem(currentItem, false);
        } else {
            currentItem++;
            mBannerViewpager.setCurrentItem(currentItem, true);
        }
    }

    @Override
    public void onBannerItemClick(DBanner banner, int position) {
        if (banner == null) {
            return;
        }
        if (banner.bannertype == BannerType.InternalFunction.getValue()) {
            if (banner.function == BannerInternalFunction.Deposit.getValue()) {
                navigator.startDepositActivity(getActivity());
            } else if (banner.function == BannerInternalFunction.WithDraw.getValue()) {
                navigator.startBalanceManagementActivity(getActivity());
            } else if (banner.function == BannerInternalFunction.SaveCard.getValue()) {
                navigator.startLinkCardActivity(getActivity());
            } else if (banner.function == BannerInternalFunction.Pay.getValue()) {
                navigator.startScanToPayActivity(getActivity());
            } else if (banner.function == BannerInternalFunction.TransferMoney.getValue()) {
                navigator.startTransferMoneyActivity(getActivity());
            } else if (banner.function == BannerInternalFunction.RedPacket.getValue()) {
                navigator.startMiniAppActivity(getActivity(), ModuleName.RED_PACKET);
            } else {
                //showToast(getString(R.string.update_to_use));
            }
        } else if (banner.bannertype == BannerType.PaymentApp.getValue()) {
            navigator.startPaymentApplicationActivity(getActivity(), new AppResource(banner.appid));
        } else {
            //showToast(getString(R.string.update_to_use));
        }
        trackBannerEvent(position);
    }

    @Override
    public void showNetworkError() {
        if (mTvInternetConnection == null ||
                mTvInternetConnection.getVisibility() == View.VISIBLE) {
            return;
        }
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

    private void trackBannerEvent(int position) {
        if (position == 0) {
            ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION1);
        } else if (position == 1) {
            ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION2);
        } else if (position == 2) {
            ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION3);
        } else if (position == 3) {
            ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION4);
        } else {
            ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION4);
        }
    }

    static Map<Integer, Integer> sActionMap;

    static {
        sActionMap = new HashMap<>();
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
    }
}
