package vn.com.vng.zalopay.webapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.MultiSwipeRefreshLayout;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.webapp.framework.IWebViewListener;
import vn.com.vng.webapp.framework.ZPWebViewApp;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.activity.HomeActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;


/**
 * Created by datnt10 on 6/21/17.
 */
public class WebAppPromotionFragment extends BaseFragment implements IWebViewListener, IWebAppPromotionView, SwipeRefreshLayout.OnRefreshListener {
    public static WebAppPromotionFragment newInstance(Bundle bundle) {
        WebAppPromotionFragment fragment = new WebAppPromotionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private WebBottomSheetDialogFragment mBottomSheetDialog;

    @BindView(R.id.layoutRetry)
    View mLayoutRetry;

    @BindView(R.id.imgError)
    ImageView mErrorImageView;

    @BindView(R.id.tvError)
    TextView mErrorTextView;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Inject
    WebAppPromotionPresenter mPresenter;

    @BindView(R.id.webview)
    ZPWebViewApp webView;

//    @BindView(R.id.promotion_btn_back)
//    View btnBack;
//
//    @BindView(R.id.promotion_btn_share)
//    View btnShare;
//
//    @BindView(R.id.promotion_tv_title)
//    TextView tvTitle;

    @BindView(R.id.promotion_refresh_layout)
    MultiSwipeRefreshLayout refreshLayout;

//    @OnClick(R.id.promotion_btn_back)
//    public void onClickBack() {
//        onBackPressed();
//    }
//
//    @OnClick(R.id.promotion_btn_share)
//    public void onClickShare() {
//        showBottomSheetDialog();
//    }

    @OnClick(R.id.btnRetry)
    public void onRetryClicked() {
        onClickRetryWebView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPresenter();
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_fragment_promotion;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(WebAppPromotionFragment.this);
    }

    protected void initPresenter() {
        mPresenter.attachView(WebAppPromotionFragment.this);
        mPresenter.initWebView(webView);
        refreshLayout.setSwipeableChildren(R.id.webview);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.back_ground_blue);
    }

    protected void loadDefaultWebView() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        String originalUrl = bundle.getString(Constants.ARG_URL);
        mPresenter.loadUrl(originalUrl);
        hideError();
    }

    protected void onClickRetryWebView() {
        mPresenter.onRequestRefreshPage();
    }

    private void showErrorNoConnection() {
        if (mLayoutRetry == null || mErrorImageView == null || mErrorTextView == null) {
            return;
        }
        mErrorImageView.setImageResource(R.drawable.webapp_ic_noconnect);
        mErrorTextView.setText(R.string.exception_no_connection_try_again);
        mLayoutRetry.setVisibility(View.VISIBLE);
    }

    private void showErrorNoLoad() {
        if (mLayoutRetry == null || mErrorImageView == null || mErrorTextView == null) {
            return;
        }
        mErrorImageView.setImageResource(R.drawable.webapp_ic_noload);
        mErrorTextView.setText(R.string.load_data_error);
        mLayoutRetry.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(int errorCode) {
        Timber.d("showError errorCode [%s]", errorCode);
        if (NetworkHelper.isNetworkAvailable(getContext())) {
            showErrorNoLoad();
        } else {
            showErrorNoConnection();
        }
        hideLoading();
    }

    @Override
    protected void onTimeoutLoading(long timeout) {
        super.onTimeoutLoading(timeout);
        showConfirmExitDialog(timeout);
    }

    private void showConfirmExitDialog(final long timeout) {
        DialogHelper.showConfirmDialog(getActivity(),
                getActivity().getResources().getString(R.string.appgame_waiting_loading),
                getActivity().getResources().getString(R.string.btn_wait_loading),
                getActivity().getResources().getString(R.string.btn_exit),
                new ZPWOnEventConfirmDialogListener
                        () {
                    @Override
                    public void onCancelEvent() {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onOKEvent() {
                        if (mPresenter.isLoadPageFinished()) {
                            return;
                        }
                        showProgressDialog(timeout);
                    }
                });
    }

    @Override
    public void onReceivedTitle(String title) {
//        tvTitle.setText(title);
        getActivity().setTitle(title);
    }

    @Override
    public void setHiddenBackButton(boolean hide) {
//        if (btnBack == null) {
//            return;
//        }
//
//        if (hide) {
//            btnBack.setVisibility(View.GONE);
//        } else {
//            btnBack.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void setHiddenShareButton(boolean hide) {
//        if (btnShare == null) {
//            return;
//        }
//
//        if (hide) {
//            btnShare.setVisibility(View.GONE);
//        } else {
//            btnShare.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void setHiddenTabBar(boolean hide) {
        AndroidUtils.runOnUIThread(() -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).setHiddenTabbar(hide);
            }
        });
    }

    @Override
    public void setRefreshing(boolean setRefresh) {
        refreshLayout.setRefreshing(setRefresh);
    }

    @Override
    public void clearCached() {
        webView.clearCache(true);
    }

    public void showError(String message) {
        showErrorDialog(message, getString(R.string.txt_close), null);
    }

    public void hideError() {
        Timber.d("hideError layoutRetry [%s]", mLayoutRetry);
        if (mLayoutRetry == null) {
            return;
        }
        mLayoutRetry.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDefaultWebView();
        mPresenter.resume();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    public boolean onBackPressed() {
        return mPresenter.onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.webapp_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.action_settings);
        View view = menuItem.getActionView();
        IconFont mIcon = (IconFont) view.findViewById(R.id.imgSettings);
        mIcon.setIcon(R.string.webapp_3point_android);
        mIcon.setIconColor(R.color.white);
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finishActivity() {
        if (getActivity() == null) {
            return;
        }
        getActivity().finish();
    }

    @Override
    public Fragment getFragment() {
        return WebAppPromotionFragment.this;
    }

    private void showBottomSheetDialog() {
        mBottomSheetDialog = mPresenter.createBottomSheetFragment();
        mBottomSheetDialog.show(getChildFragmentManager(), "bottomsheet");
    }

    public void showLoading() {
        AndroidUtils.runOnUIThread(this::showProgressDialogWithTimeout);
    }

    public void hideLoading() {
        AndroidUtils.runOnUIThread(this::hideProgressDialog);
    }

    @Override
    public void updateLoadProgress(int progress) {
        if (mProgressBar == null) {
            return;
        }

        if (progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        }
        mProgressBar.setProgress(progress);
        if (progress >= 100) {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    @Override
    public void dismissBottomSheet() {
        if (mBottomSheetDialog == null) {
            return;
        }

        mBottomSheetDialog.dismiss();
        mBottomSheetDialog = null;
    }

    @Override
    public void onRefresh() {
        mPresenter.onRequestRefreshPage();
    }
}