package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.Manifest;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
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
public class ZaloPayFragment extends BaseMainFragment implements ListAppRecyclerAdapter.OnClickAppListener, IZaloPayView {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onScreenVisible() {
    }

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
        listView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        listView.setNestedScrollingEnabled(false);
        listView.addItemDecoration(new GridSpacingItemDecoration(3, 2, false));
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
        bannerResource.add(R.drawable.bn_3);
        mBannerPagerAdapter = new BannerPagerAdapter(getContext(), bannerResource);
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
    public void onClickAppListener(AppResource app) {

        if (app == null) {
            return;
        }

        Timber.d("onclick app %s %s ", app.appid, app.appname);

        if (app.appid == 1) {
            navigator.startTransferMoneyActivity(getActivity());
        } else {
            navigator.startPaymentApplicationActivity(getActivity(), app, Constants.ModuleName.PAYMENT_MAIN);
        }
    }

    @OnClick(R.id.btn_deposit)
    public void onBtnDepositClick(View view) {
        navigator.startDepositActivity(getActivity());
    }

    @OnClick(R.id.btn_link_card)
    public void onBtnLinkCardClick(View view) {
        navigator.startLinkCardActivity(getActivity());
    }

    @OnClick(R.id.btn_qr_code)
    public void onBtnQrCodeClick(View view) {
        startQRCodeActivity();
    }

    private void startQRCodeActivity() {
        if (checkAndRequestPermission(Manifest.permission.CAMERA, 100)) {
            navigator.startQrCodeActivity(getActivity());
        }
    }

    //Test
    private List<AppResource> getListData() {
        return Arrays.asList(
                new AppResource(1, getString(R.string.transfer_money), String.valueOf(R.drawable.ic_chuyentien)),
                new AppResource(11, getString(R.string.recharge_money_phone), String.valueOf(R.drawable.ic_naptiendt)),
                new AppResource(12, getString(R.string.buy_phone_card), String.valueOf(R.drawable.ic_muathedt))
//                new AppResource(13, getString(R.string.buy_game_card), String.valueOf(R.drawable.ic_muathegame)),
//                new AppResource(3, getString(R.string.electric_bill), String.valueOf(R.drawable.ic_tiendien), 1),
//                new AppResource(4, getString(R.string.internet_bill), String.valueOf(R.drawable.ic_internet), 1),
//                new AppResource(5, getString(R.string.red_envelope), String.valueOf(R.drawable.ic_lixi), 1),
//                new AppResource(6, getString(R.string.water_bill), String.valueOf(R.drawable.ic_tiennuoc), 1)
        );
    }

    @Override
    public void insertApps(List<AppResource> list) {
        mAdapter.insertItems(list);
    }

    @Override
    public void setTotalNotify(int total) {
        mNotifyView.setText(String.valueOf(total));
    }
}
