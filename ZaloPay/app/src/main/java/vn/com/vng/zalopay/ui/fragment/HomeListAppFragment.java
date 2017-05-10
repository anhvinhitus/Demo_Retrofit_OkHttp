package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zalopay.ui.widget.MultiSwipeRefreshLayout;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.adapter.HomeAdapter;
import vn.com.vng.zalopay.ui.presenter.HomeListAppPresenter;
import vn.com.vng.zalopay.ui.view.IHomeListAppView;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.HomeSpacingItemDecoration;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.widget.FragmentLifecycle;

/**
 * Created by Datnt10 on 5/10/17.
 * Display PaymentApps in Grid layout
 * Handle ui and events of list payment app
 */

public class HomeListAppFragment extends BaseFragment implements IHomeListAppView
        , FragmentLifecycle
        , HomeAdapter.OnClickItemListener
        , SwipeRefreshLayout.OnRefreshListener {

    private final static int SPAN_COUNT_APPLICATION = 3;
    private HomeAdapter mHomeAdapter;

    @Inject
    HomeListAppPresenter presenter;

    @BindView(R.id.home_rcv_list_app)
    RecyclerView mAppListView;

    @BindView(R.id.home_tv_internet_connection)
    TextView mTvInternetConnection;

    @BindView(R.id.home_swipe_refresh_view)
    MultiSwipeRefreshLayout mSwipeRefreshLayout;

    public static HomeListAppFragment newInstance() {
        Bundle args = new Bundle();
        HomeListAppFragment fragment = new HomeListAppFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHomeAdapter = new HomeAdapter(getContext(), this, SPAN_COUNT_APPLICATION);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        presenter.attachView(this);

        mAppListView.setHasFixedSize(true);
        HomeSpacingItemDecoration itemDecoration = new HomeSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false);
        mAppListView.addItemDecoration(itemDecoration);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION);
        gridLayoutManager.setSpanSizeLookup(mHomeAdapter.getSpanSizeLookup());
        mAppListView.setLayoutManager(gridLayoutManager);
        mAppListView.setAdapter(mHomeAdapter);

        setInternetConnectionError(getString(R.string.exception_no_connection_tutorial),
                getString(R.string.check_internet));
        mSwipeRefreshLayout.setSwipeableChildren(R.id.home_rcv_list_app);
        mSwipeRefreshLayout.setOnRefreshListener(this);
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
        mAppListView.setAdapter(null);
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_home_list_app;
    }


    @Override
    public void onStartFragment() {

    }

    @Override
    public void onStopFragment() {

    }

    @Override
    public void setAppItems(List<AppResource> list) {
        mHomeAdapter.setAppItems(list);
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
    public void setRefreshing(boolean val) {
        mSwipeRefreshLayout.setRefreshing(val);
    }

    @Override
    public void onClickAppItem(AppResource app, int position) {
        presenter.launchApp(app, position);
    }

    @Override
    public void onRefresh() {
        presenter.getListAppResource();
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
}
