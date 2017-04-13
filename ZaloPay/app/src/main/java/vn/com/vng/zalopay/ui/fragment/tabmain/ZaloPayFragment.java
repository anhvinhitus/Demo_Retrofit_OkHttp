package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseIntArray;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

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
import vn.com.vng.zalopay.ui.adapter.HomeAdapter;
import vn.com.vng.zalopay.ui.adapter.ListAppRecyclerAdapter;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.HomeSpacingItemDecoration;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by AnhHieu on 4/11/16.
 * Display PaymentApps in Grid layout
 */
public class ZaloPayFragment extends RuntimePermissionFragment implements
        IZaloPayView, SwipeRefreshLayout.OnRefreshListener, HomeAdapter.OnClickItemListener {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final static int SPAN_COUNT_APPLICATION = 3;

    @Inject
    ZaloPayPresenter presenter;

    private HomeAdapter mAdapter;

    @BindView(R.id.listView)
    RecyclerView listView;

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
        mAdapter = new HomeAdapter(getContext(), this, SPAN_COUNT_APPLICATION);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        presenter.attachView(this);

        listView.setHasFixedSize(true);
        HomeSpacingItemDecoration itemDecoration = new HomeSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false);
        listView.addItemDecoration(itemDecoration);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION);
        gridLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        listView.setLayoutManager(gridLayoutManager);
        listView.setAdapter(mAdapter);

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
        presenter.initialize();
    }

    @Override
    public void onResume() {
        presenter.resume();
        mAdapter.pauseBanner();
        super.onResume();
    }

    @Override
    public void onPause() {
        presenter.pause();
        mAdapter.resumeBanner();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        listView.setAdapter(null);
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void setAppItems(List<AppResource> list) {
        mAdapter.setAppItems(list);
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
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public void setBanner(List<DBanner> lists) {
        mAdapter.setBanners(lists);
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

    @Override
    public void onClickBanner(DBanner banner, int index) {
        presenter.launchBanner(banner, index);
    }

    @Override
    public void onClickAppItem(AppResource app, int position) {
        presenter.launchApp(app, position);
    }

    @Override
    public void setBalance(long balance) {
        
    }
}