package vn.com.vng.zalopay.webview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.webview.interfaces.ITimeoutLoadingListener;
import vn.com.vng.zalopay.webview.widget.ZPWebView;
import vn.com.vng.zalopay.webview.widget.ZPWebViewProcessor;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnProgressDialogTimeoutListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by chucvv on 8/28/16.
 * Fragment
 */
public class WebViewFragment extends BaseFragment implements IWebView, ZPWebViewProcessor.IWebViewListener {
    protected ITimeoutLoadingListener mTimeOutListener;

    protected ZPWebViewProcessor mWebViewProcessor;
    private View layoutRetry;
    private ImageView imgError;
    private TextView tvError;

    protected String mCurrentUrl = "";
    private boolean mIsPageValid = false;

    public static WebViewFragment newInstance(Bundle bundle) {
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(bundle);
        return fragment;
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
        mTimeOutListener = new ITimeoutLoadingListener() {
            @Override
            public void onTimeoutLoading() {
                Timber.d("onTimeoutLoading");
                //load website timeout, show confirm dialog: continue to load or exit.
                if (getActivity() == null) {
                    return;
                }
                DialogManager.showSweetDialogConfirm(getActivity(),
                        getActivity().getResources().getString(R.string.appgame_waiting_loading),
                        getActivity().getResources().getString(R.string.btn_exit),
                        getActivity().getResources().getString(R.string.btn_wait_loading),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {
                                getActivity().finish();
                            }

                            @Override
                            public void onOKevent() {
                            }
                        });
            }
        };
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated start");
        initPresenter(view);
        initRetryView(view);
        initWebView(view);
        loadWebView();
    }

    protected void initPresenter(View view) {

    }

    protected void loadWebView() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        String originalUrl = bundle.getString(Constants.ARG_URL);
        loadUrl(originalUrl);
    }

    private void initWebView(View rootView) {
        ZPWebView webView = (ZPWebView) rootView.findViewById(R.id.webview);
        mWebViewProcessor = new ZPWebViewProcessor(webView, mTimeOutListener, this);
    }

    @Override
    public void showInputErrorDialog() {
        DialogManager.showSweetDialogCustom(getActivity(),
                getContext().getString(R.string.appgame_alert_input_error),
                getContext().getString(R.string.txt_close),
                SweetAlertDialog.WARNING_TYPE, new ZPWOnEventDialogListener() {
                    @Override
                    public void onOKevent() {
                        getActivity().finish();
                    }
                });
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    public void loadUrl(final String pUrl) {
        if (mWebViewProcessor == null) {
            return;
        }
        mCurrentUrl = pUrl;
        mWebViewProcessor.start(mCurrentUrl, getActivity());
    }

    protected void onClickRetryWebView() {
        hideError();
        refreshWeb();
    }

    private void initRetryView(View rootView) {
        layoutRetry = rootView.findViewById(R.id.layoutRetry);
        imgError = (ImageView) rootView.findViewById(R.id.imgError);
        tvError = (TextView) rootView.findViewById(R.id.tvError);
        View btnRetry = rootView.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRetryWebView();
            }
        });
        hideError();
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
        imgError.setImageResource(R.drawable.webapp_ic_noconnect);
        tvError.setText(R.string.exception_no_connection_try_again);
        layoutRetry.setVisibility(View.VISIBLE);
    }

    public void showError(int errorCode) {
        Timber.d("showError errorCode [%s]", errorCode);
        if (errorCode == WebViewClient.ERROR_CONNECT) {
            if (NetworkHelper.isNetworkAvailable(getContext())) {
                showErrorNoLoad();
            } else {
                showErrorNoConnection();
            }
        } else {
            showErrorNoLoad();
        }
        hideLoading();
    }

    public void hideLoading() {
        DialogManager.closeProcessDialog();
    }

    public void showLoading() {
        DialogManager.showProcessDialog(getActivity(), new ZPWOnProgressDialogTimeoutListener() {
            @Override
            public void onProgressTimeout() {
                if (mTimeOutListener != null) {
                    mTimeOutListener.onTimeoutLoading();
                }
            }
        });
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroy();
        }
        super.onDestroy();
    }

    public boolean onBackPressed() {
        if (mWebViewProcessor == null) {
            return false;
        }
        if (mWebViewProcessor.hasError()) {
            return false;
        }

        boolean canBack = mWebViewProcessor.canBack();
        Timber.d("Can WebApp navigate back: %s", canBack);
        if (canBack) {
            mWebViewProcessor.runScript("utils.back()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Timber.d("navigation back: %s", value);
                }
            });
        }
        return canBack;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.webapp_menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Timber.d("onOptionsItemSelected: %s", id);
        if (id == R.id.webapp_action_refresh) {
            refreshWeb();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void refreshWeb() {
        Timber.d("Request to reload web view");
        hideError();
        loadUrl(mCurrentUrl);
    }

    private void hideWebView() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.hideWebView();
        }
    }

    private void showWebView() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.showWebView();
        }
    }

    @Override
    public void onReceivedError(int errorCode, CharSequence description) {
        Timber.d("onReceivedError errorCode [%s] description [%s]", errorCode, description);
        hideWebView();
        showError(errorCode);
    }

    @Override
    public void onPageFinished(String url) {
        Timber.d("onPageFinished url [%s]", url);
        mCurrentUrl = url;
        showWebView();
    }

    @Override
    public void payOrder(final String url) {
    }

    @Override
    public void logout() {
        getAppComponent().applicationSession().clearUserSession();
        getActivity().finish();
    }

    @Override
    public void finishActivity() {
        if (getActivity() == null) {
            return;
        }
        getActivity().finish();
    }

    @Override
    public void setTitleAndLogo(String title, String url) {
        if (getActivity() instanceof WebViewActivity) {
            ((WebViewActivity) getActivity()).setTitleAndLogo(title, url);
        }
    }

    public boolean isPageValid() {
        return mIsPageValid;
    }

    @Override
    public void setPageValid(boolean valid) {
        mIsPageValid = valid;
    }
}
