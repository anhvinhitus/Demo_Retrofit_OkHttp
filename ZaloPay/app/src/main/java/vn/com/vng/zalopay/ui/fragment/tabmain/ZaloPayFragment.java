package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.ReactAppConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.adapter.BannerPagerAdapter;
import vn.com.vng.zalopay.ui.adapter.ListAppRecyclerAdapter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;

/**
 * Created by AnhHieu on 4/11/16.
 */
public class ZaloPayFragment extends BaseMainFragment implements ListAppRecyclerAdapter.OnClickAppListener, IZaloPayView, BannerPagerAdapter.IBannerClick {

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
    Navigator navigator;

    @Inject
    ZaloPayPresenter presenter;

    /* Advertisement START */
    @BindView(R.id.layoutBannerFullScreen)
    View mLayoutBannerFullScreen;

    @BindView(R.id.viewpager)
    ViewPager mBannerViewpager;
    BannerPagerAdapter mBannerPagerAdapter;

    @BindView(R.id.indicator)
    SmartTabLayout mBannerIndicator;

    @BindView(R.id.tvAdsSubContent)
    TextView mTvAdsSubContent;
    /* Advertisement END */

    private ListAppRecyclerAdapter mAdapter;

    @BindView(R.id.listView)
    RecyclerView listView;

    /*
    * View của menu
    * */
    TextView mNotifyView;

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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);

        listView.setHasFixedSize(true);
        listView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION));
        listView.setNestedScrollingEnabled(false);
        listView.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false));
        listView.setAdapter(mAdapter);

        showBannerAds();
//        showTextAds("Mobi khuyến mại <b>50%. Nạp ngay hôm nay!</b>");
        hideTextAds();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View view = menuItem.getActionView();
        mNotifyView = (TextView) view.findViewById(R.id.tvNotificationCount);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigator.startMiniAppActivity(getActivity(), Constants.ModuleName.NOTIFICATIONS);
                zpAnalytics.trackEvent(ZPEvents.TAPNOTIFICATIONBUTTON);
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter.setData(getListData());
        presenter.initialize();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    /* Show|Hide Banner START */
    public void showBannerAds() {
        //Glide.with(this).load(url).asBitmap().into(mImgAdsBanner);
        List<Integer> bannerResource = new ArrayList<>();
        bannerResource.add(R.drawable.bn_1);
        bannerResource.add(R.drawable.bn_2);
        bannerResource.add(R.drawable.bn_4);
        mBannerPagerAdapter = new BannerPagerAdapter(getContext(), bannerResource, this);
        mBannerViewpager.setAdapter(mBannerPagerAdapter);
        mBannerIndicator.setViewPager(mBannerViewpager);
        if (mLayoutBannerFullScreen != null) {
            mLayoutBannerFullScreen.setVisibility(View.VISIBLE);
        }
    }

    public void hideBannerAds() {
        if (mLayoutBannerFullScreen != null) {
            mLayoutBannerFullScreen.setVisibility(View.GONE);
        }
    }

    public void showTextAds(String content) {
        if (TextUtils.isEmpty(content)) {
            hideTextAds();
        } else {
            mTvAdsSubContent.setText(Html.fromHtml(content));
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
        if (app.appid == 1) {
            navigator.startTransferMoneyActivity(getActivity());
        } else {
            navigator.startPaymentApplicationActivity(getActivity(), app, Constants.ModuleName.PAYMENT_MAIN);
        }

        this.logActionApp(position);
    }

    // FIXME: 6/28/16 
    private void logActionApp(int position) {
        int action = ZPEvents.TAPAPPICON_1_1;

        switch (position) {
            case 0:
                action = ZPEvents.TAPAPPICON_1_1;
                break;
            case 1:
                action = ZPEvents.TAPAPPICON_1_2;
                break;
            case 2:
                action = ZPEvents.TAPAPPICON_1_3;
                break;
        }
        zpAnalytics.trackEvent(action);
    }

    @OnClick(R.id.btn_deposit)
    public void onBtnDepositClick(View view) {
        navigator.startDepositActivity(getActivity());
        zpAnalytics.trackEvent(ZPEvents.TAPADDCASH);
    }

    @OnClick(R.id.btn_link_card)
    public void onBtnLinkCardClick(View view) {
        navigator.startLinkCardActivity(getActivity());
        zpAnalytics.trackEvent(ZPEvents.TAPMANAGECARDS);
    }

    @OnClick(R.id.btn_qr_code)
    public void onBtnQrCodeClick(View view) {
        startQRCodeActivity();
        zpAnalytics.trackEvent(ZPEvents.TAPSCANQR);
    }

    private void startQRCodeActivity() {
        if (checkAndRequestPermission(Manifest.permission.CAMERA, 100)) {
            navigator.startQrCodeActivity(getActivity());
        }
    }

    //Test
    List<AppResource> mListApps = null;

    private List<AppResource> getListData() {
        if (Lists.isEmptyOrNull(mListApps)) {
            mListApps = new ArrayList<>(ReactAppConfig.APP_RESOURCE_LIST);
        }
        return mListApps;
    }

    @Override
    public void insertApps(List<AppResource> list) {
        mAdapter.insertItems(list);
    }

    @Override
    public void setTotalNotify(int total) {
        if (mNotifyView != null) {
            if (total > 0) {
                mNotifyView.setText(String.valueOf(total));
                mNotifyView.setVisibility(View.VISIBLE);
            } else {
                mNotifyView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        if (position == 0) {

            AppResource app = getListData().get(1);
            navigator.startPaymentApplicationActivity(getActivity(), app, Constants.ModuleName.PAYMENT_MAIN);

            zpAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION1);
        } else if (position == 1) {
            navigator.startLinkCardProcedureActivity(getActivity());
            zpAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION2);
        } else if (position == 2) {

            AppResource app = getListData().get(2);
            navigator.startPaymentApplicationActivity(getActivity(), app, Constants.ModuleName.PAYMENT_MAIN);

            zpAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigator.startQrCodeActivity(getActivity());
            } else {
                zpAnalytics.trackEvent(ZPEvents.SCANQR_ACCESSDENIED);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
