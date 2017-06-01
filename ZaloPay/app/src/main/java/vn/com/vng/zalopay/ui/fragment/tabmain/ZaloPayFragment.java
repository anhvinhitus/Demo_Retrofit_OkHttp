package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.HomeCollapseHeaderFragment;
import vn.com.vng.zalopay.ui.fragment.HomeListAppFragment;
import vn.com.vng.zalopay.ui.fragment.HomeTopHeaderFragment;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.toolbar.HeaderView;
import vn.com.vng.zalopay.widget.FragmentLifecycle;

/**
 * Created by AnhHieu on 4/11/16.
 * Display PaymentApps in Grid layout
 */
public class ZaloPayFragment extends RuntimePermissionFragment implements
        AppBarLayout.OnOffsetChangedListener, FragmentLifecycle {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /*
    * Collapse toolbar view
    * */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.float_header_view)
    HeaderView mHeaderView;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    private HomeTopHeaderFragment homeTopHeaderFragment;

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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        initListAppFragment();
        initCollapseHeaderFragment();
        initTopHeaderFragment();
    }

    @Override
    public void onResume() {
        // Set collapsing behavior
        mAppBarLayout.addOnOffsetChangedListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mAppBarLayout.removeOnOffsetChangedListener(this);
        super.onPause();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if(homeTopHeaderFragment == null) {
            return;
        }

        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
        boolean isCollapsed;

        mHeaderView.setAlpha(1 - percentage);

        if (percentage == 0f) {
            isCollapsed = true;
            mHeaderView.setVisibility(View.VISIBLE);
        } else {
            isCollapsed = false;
            if (percentage > 0.5f) {
                mHeaderView.setVisibility(View.GONE);
            } else {
                mHeaderView.setVisibility(View.VISIBLE);
            }
        }

        homeTopHeaderFragment.setHeaderTopStatus(isCollapsed, percentage);
    }

    @Override
    public void onStartFragment() {

    }

    @Override
    public void onStopFragment() {

    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {

    }

    /*
    * Child fragments initialization
    * */
    private void initListAppFragment() {
        if (getFragmentManager().findFragmentById(R.id.home_fl_list_app_content) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            HomeListAppFragment homeListAppFragment = HomeListAppFragment.newInstance();
            ft.replace(R.id.home_fl_list_app_content, homeListAppFragment);
            ft.commit();
        }
    }

    private void initCollapseHeaderFragment() {
        if (getFragmentManager().findFragmentById(R.id.home_fl_collapse_header_view) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            HomeCollapseHeaderFragment homeCollapseHeaderFragment = HomeCollapseHeaderFragment.newInstance();
            ft.replace(R.id.home_fl_collapse_header_view, homeCollapseHeaderFragment);
            ft.commit();
        }
    }

    private void initTopHeaderFragment() {
        if (getFragmentManager().findFragmentById(R.id.home_fl_top_header_content) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            homeTopHeaderFragment = HomeTopHeaderFragment.newInstance();
            ft.replace(R.id.home_fl_top_header_content, homeTopHeaderFragment);
            ft.commit();
        }
    }
}