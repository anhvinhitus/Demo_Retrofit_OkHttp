package vn.com.vng.zalopay.webview.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.webapp.WebBottomSheetDialogFragment;
import vn.com.vng.zalopay.webview.widget.ZPWebView;
import vn.com.vng.zalopay.webview.widget.ZPWebViewFromQRScanProcessor;


/**
 * Created by chucvv on 8/28/16.
 * Fragment
 */
public class WebViewFromQRScanFragment extends BaseFragment implements ZPWebViewFromQRScanProcessor.IWebViewFromQRScanListener,
        WebBottomSheetDialogFragment.BottomSheetEventListener {

    protected ZPWebViewFromQRScanProcessor mWebViewProcessor;

    @BindView(R.id.webview)
    ZPWebView mWebView;

    @BindView(R.id.layoutRetry)
    View layoutRetry;

    @BindView(R.id.imgError)
    ImageView imgError;

    @BindView(R.id.tvError)
    TextView tvError;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    private WebBottomSheetDialogFragment mBottomSheetDialog;

    public static WebViewFromQRScanFragment newInstance(Bundle bundle) {
        WebViewFromQRScanFragment fragment = new WebViewFromQRScanFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @OnClick(R.id.btnRetry)
    public void onRetryClicked() {
        onClickRetryWebView();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_fragment_webview;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated start");
        initWebView();
        loadDefaultWebView();
    }

    protected void loadDefaultWebView() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        String originalUrl = bundle.getString(Constants.ARG_URL);
        loadUrl(originalUrl);
    }

    protected void updateWebViewSettings() {

    }

    private void initWebView() {
        hideError();
        updateWebViewSettings();
        mWebViewProcessor = new ZPWebViewFromQRScanProcessor(mWebView, this);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                Timber.d("WebLoading progress: %s", progress);
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
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.setTitle(title);
                }
            }
        });
    }

    public void loadUrl(final String pUrl) {
        if (mWebViewProcessor == null) {
            return;
        }
        mWebViewProcessor.start(pUrl, getActivity());
    }

    protected void onClickRetryWebView() {
        hideError();
        refreshWeb();
    }

    private void showErrorNoConnection() {
        if (layoutRetry == null || imgError == null || tvError == null) {
            return;
        }
        imgError.setImageResource(R.drawable.webapp_ic_noconnect);
        tvError.setText(R.string.exception_no_connection_try_again);
        layoutRetry.setVisibility(View.VISIBLE);
    }

    private void showErrorNoLoad() {
        if (layoutRetry == null || imgError == null || tvError == null) {
            return;
        }
        imgError.setImageResource(R.drawable.webapp_ic_noload);
        tvError.setText(R.string.load_data_error);
        layoutRetry.setVisibility(View.VISIBLE);
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
                        if (mWebViewProcessor != null && mWebViewProcessor.isLoadPageFinished()) {
                            return;
                        }
                        showProgressDialog(timeout);
                    }
                });
    }

    public void hideLoading() {
        super.hideProgressDialog();
    }

    public void showLoading() {
        super.showProgressDialogWithTimeout();
    }

    public void showError(String message) {
        showErrorDialog(message, getString(R.string.txt_close), null);
    }

    protected void hideError() {
        Timber.d("hideError layoutRetry [%s]", layoutRetry);
        if (layoutRetry == null) {
            return;
        }
        layoutRetry.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebViewProcessor = new ZPWebViewFromQRScanProcessor(mWebView, this);
//        if (mWebViewProcessor != null) {
//            mWebViewProcessor.onResume();
//        }
    }

    @Override
    public void onPause() {
        if (mWebViewProcessor != null) {
//            mWebViewProcessor.onPause();
            mWebViewProcessor = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroyView();
        }
        super.onDestroyView();
    }

    public boolean onBackPressed() {
        return mWebViewProcessor != null && mWebViewProcessor.onBackPress() || super.onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.webapp_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.action_settings);
        View view = menuItem.getActionView();
        IconFont mIcon = (IconFont) view.findViewById(R.id.imgSettings);
        mIcon.setIcon(R.string.webapp_3point_android);
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    public void refreshWeb() {
        Timber.d("Request to reload web view");
        hideError();
        mWebViewProcessor.refreshWeb(getActivity());
    }

    private void showBottomSheetDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("current_url", mWebViewProcessor.getCurrentUrl());
        mBottomSheetDialog = WebBottomSheetDialogFragment.newInstance(bundle);
        mBottomSheetDialog.setBottomSheetEventListener(this);
        mBottomSheetDialog.show(getChildFragmentManager(), "bottomsheet");
    }

    @Override
    public void onRequestRefreshPage() {
        refreshWeb();
        mBottomSheetDialog.dismiss();
    }
}
