package vn.com.vng.zalopay.banner.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.zalopay.apploader.internal.ModuleName;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.banner.model.BannerInternalFunction;
import vn.com.vng.zalopay.banner.model.BannerType;
import vn.com.vng.zalopay.banner.ui.adapter.BannerPagerAdapter;
import vn.com.vng.zalopay.banner.ui.presenter.BannerPresenter;
import vn.com.vng.zalopay.banner.ui.view.IBannerView;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.SmoothViewPager;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BannerFragment extends BaseFragment implements IBannerView,
        BannerPagerAdapter.IBannerClick {

    @Inject
    BannerPresenter mBannerPresenter;

    @BindView(R.id.layoutBannerFullScreen)
    View mLayoutBannerFullScreen;

    @BindView(R.id.viewpager)
    SmoothViewPager mBannerViewpager;

    BannerPagerAdapter mBannerPagerAdapter;

    @BindView(R.id.indicator)
    SmartTabLayout mBannerIndicator;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BannerFragment newInstance() {
        return new BannerFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_banner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        measureBannerSize();
        return view;
    }

    private void measureBannerSize() {
        //If device is tablet then image style is center inside.
        if (AndroidUtils.isTablet(getContext())) {
            return;
        }
        //If device is mobile then calculate width & height of image and style is fitXY.
        int screenWidth = AndroidUtils.getScreenWidth(getActivity());
        int bannerHeight = Math.round(screenWidth * getResources().getInteger(R.integer.banner_height)
                / getResources().getInteger(R.integer.banner_width));
        Timber.d("measure banner size width[%s] height[%s]", screenWidth, bannerHeight);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, bannerHeight);
        params.height = bannerHeight;
        mBannerViewpager.setLayoutParams(params);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBannerPresenter.attachView(this);
    }

    @Override
    public void showError(String msg) {
        super.showErrorDialog(msg);
    }

    @Override
    public void showBannerAds(List<DBanner> banners) {
        if (banners == null || banners.size() <= 0) {
            Timber.d("Hide banner ads bannerView[%s]", mLayoutBannerFullScreen);
            if (mLayoutBannerFullScreen != null) {
                mLayoutBannerFullScreen.setVisibility(View.GONE);
            }
        } else {
            Timber.d("Show banner ads, banners size [%s]", banners.size());
            if (mBannerPagerAdapter == null) {
                mBannerPagerAdapter = new BannerPagerAdapter(getContext(), banners, this);
                mBannerViewpager.setAdapter(mBannerPagerAdapter);
                mBannerIndicator.setViewPager(mBannerViewpager);
                mBannerViewpager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Timber.d("Touch banner, action [%s]", event.getAction());
                        if (mBannerPresenter != null) {
                            mBannerPresenter.onTouchBanner(event);
                        }
                        return false;
                    }
                });
                if (mLayoutBannerFullScreen != null) {
                    mLayoutBannerFullScreen.setVisibility(View.VISIBLE);
                }
            } else {
                mBannerPagerAdapter.setData(banners);
                mBannerIndicator.setViewPager(mBannerViewpager);
            }
        }
    }

    @Override
    public void changeBanner() {
        if (mBannerViewpager == null || mBannerViewpager.getAdapter() == null) {
            return;
        }
        int count = mBannerViewpager.getAdapter().getCount();
        if (count <= 0) {
            return;
        }
        int currentItem = mBannerViewpager.getCurrentItem();
        if (currentItem >= count - 1) {
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
            }
        } else if (banner.bannertype == BannerType.PaymentApp.getValue()) {
            mBannerPresenter.startPaymentApp(new AppResource(banner.appid));
        } else if (banner.bannertype == BannerType.ServiceWebView.getValue()) {
            mBannerPresenter.startServiceWebViewActivity(banner.appid, banner.webviewurl);
        } else if (banner.bannertype == BannerType.WebPromotion.getValue()) {
            navigator.startWebViewActivity(getContext(), banner.webviewurl);
        }
        trackBannerEvent(position);
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

    @Override
    public void onResume() {
        super.onResume();
        mBannerPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBannerPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        mBannerPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mBannerPresenter.destroy();
        super.onDestroy();
    }
}
