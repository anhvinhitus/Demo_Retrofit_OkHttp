package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.textview.RoundTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.tabmain.UserBaseTabFragment;
import vn.com.vng.zalopay.ui.presenter.HomeTopHeaderPresenter;
import vn.com.vng.zalopay.ui.view.IHomeTopHeaderView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by Duke on 5/11/17.
 * Top header view (toolbar)
 */

public class HomeTopHeaderFragment extends UserBaseTabFragment implements IHomeTopHeaderView {

    @Inject
    HomeTopHeaderPresenter presenter;

    @BindView(R.id.header_top_rl_search_view)
    View rlHeaderSearchView;

    @BindView(R.id.header_top_rl_collapsed)
    View rlHeaderCollapsed;

    @BindView(R.id.tvNotificationCount)
    RoundTextView mNotifyView;

    public static HomeTopHeaderFragment newInstance() {
        Bundle args = new Bundle();
        HomeTopHeaderFragment fragment = new HomeTopHeaderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
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

    @Override
    public void setHeaderTopStatus(boolean isCollapsed, float alpha) {
        if (isCollapsed) {
            rlHeaderCollapsed.setVisibility(View.GONE);
            rlHeaderSearchView.setVisibility(View.VISIBLE);
            rlHeaderCollapsed.setAlpha(alpha);
            rlHeaderSearchView.setAlpha(1 - alpha);
        } else {
            if (alpha > 0.3f) {
                rlHeaderSearchView.setVisibility(View.GONE);
                rlHeaderCollapsed.setVisibility(View.VISIBLE);
            }
            rlHeaderCollapsed.setAlpha(alpha);
            rlHeaderSearchView.setAlpha(1 - alpha);
        }
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.zp_header_top_layout;
    }

    @OnClick(R.id.header_top_rl_notification)
    public void onBtnNotificationClick() {
        navigator.startMiniAppActivity(getActivity(), ModuleName.NOTIFICATIONS);
        ZPAnalytics.trackEvent(ZPEvents.HOME_TOUCH_NOTIFICATION);
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
        ZPAnalytics.trackEvent(ZPEvents.HOME_TOUCH_SEARCH);
    }

    @OnClick(R.id.header_top_rl_search_view)
    public void onClickSearchViewOnToolbar() {
        navigator.startSearchCategoryActivity(getContext());
    }
}
