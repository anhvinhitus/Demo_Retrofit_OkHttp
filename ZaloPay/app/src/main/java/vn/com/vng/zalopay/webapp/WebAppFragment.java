package vn.com.vng.zalopay.webapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
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
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;


/**
 * Created by chucvv on 8/28/16.
 * WebAppFragment
 */
public class WebAppFragment extends BaseFragment implements IWebViewListener, IWebAppView {

    public static WebAppFragment newInstance(Bundle bundle) {
        WebAppFragment fragment = new WebAppFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(WebAppFragment.this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_fragment_mainview;
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
    WebAppPresenter mPresenter;

    @BindView(R.id.webview)
    ZPWebViewApp webView;

    @OnClick(R.id.btnRetry)
    public void onRetryClick() {
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

    protected void initPresenter() {
        mPresenter.attachView(WebAppFragment.this);
        mPresenter.initWebView(webView);
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
        getActivity().setTitle(title);
    }

    @Override
    public void setRefreshing(boolean setRefresh) {

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
        mIcon.setIconColor(R.color.colorWebAppPrimaryText);
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
        return WebAppFragment.this;
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
}

