package vn.com.vng.zalopay.webapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.Collections;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.webapp.framework.IWebViewListener;
import vn.com.vng.webapp.framework.ZPWebViewApp;
import vn.com.vng.webapp.framework.ZPWebViewAppProcessor;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.event.RefreshWebEvent;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.webbottomsheetdialog.WebBottomSheetDialogFragment;


/**
 * Created by chucvv on 8/28/16.
 * Fragment
 */
public class WebAppFragment extends BaseFragment implements IWebViewListener, IProcessMessageListener, IWebAppView {

    public static WebAppFragment newInstance(Bundle bundle) {
        WebAppFragment fragment = new WebAppFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webapp_fragment_mainview;
    }

    protected ZPWebViewAppProcessor mWebViewProcessor;

    private View layoutRetry;
    private ImageView imgError;
    private TextView tvError;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Inject
    WebAppPresenter mPresenter;

    @Inject
    EventBus mEventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated start");
        initPresenter(view);
        initRetryView(view);
        initWebView(view);
        loadDefaultWebView();
    }

    protected void initPresenter(View view) {
        mPresenter.attachView(this);
    }

    protected void loadDefaultWebView() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        String originalUrl = bundle.getString(Constants.ARG_URL);
        loadUrl(originalUrl);
    }

    protected String getCurrentUrl() {
        if (mWebViewProcessor == null) {
            return null;
        }
        return mWebViewProcessor.getCurrentUrl();
    }

    private void initWebView(View rootView) {
        ZPWebViewApp webView = (ZPWebViewApp) rootView.findViewById(R.id.webview);
        mWebViewProcessor = new ZPWebViewAppProcessor(webView, this);
        mWebViewProcessor.registerNativeModule(new ProcessNativeModule(this));

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                Timber.d("WebLoading progress: %s", progress);
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

                getActivity().setTitle(title);
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
        imgError.setImageResource(R.drawable.webapp_ic_noload);
        tvError.setText(R.string.load_data_error);
        layoutRetry.setVisibility(View.VISIBLE);
    }

    @Override
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
                    public void onOKevent() {
                        if (mWebViewProcessor != null && mWebViewProcessor.isLoadPageFinished()) {
                            return;
                        }
                        showProgressDialog(timeout);
                    }
                });
    }

    @Override
    public void hideLoading() {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
            }
        });
    }

    @Override
    public void writeLog(String type, long time, String data) {
        switch (type) {
            case "info":
                Timber.i("time: %s, data: %s", time, data);
                break;
            case "warn":
                Timber.w("time: %s, data: %s", time, data);
                break;
            case "error":
                Timber.e("time: %s, data: %s", time, data);
                break;
            default:
                Timber.d("type: %s, time: %s, data: %s", type, time, data);
        }
    }

    @Override
    public void onReceivedTitle(String title) {

    }

    @Override
    public void showDialog(final int dialogType, final String title, final String message, final String buttonLabel) {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                DialogHelper.showCustomDialog(getActivity(),
                        dialogType,
                        title,
                        message,
                        null,
                        Collections.singletonList(buttonLabel).toArray(new String[0]));
            }
        });
    }

    @Override
    public void showLoading() {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialogWithTimeout();
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
        mPresenter.resume();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onResume();
        }
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onPause();
        }
        mEventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroy();
        }
        super.onDestroy();
    }

    public boolean onBackPressed() {
        if (mWebViewProcessor != null && mWebViewProcessor.onBackPress()) {
            return true;
        } else {
            return super.onBackPressed();
        }
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

    public void refreshWeb() {
        Timber.d("Request to reload web view");
        hideError();
        mWebViewProcessor.refreshWeb(getActivity());
    }

    @Override
    public void payOrder(JSONObject jsonObject, IPaymentListener listener) {
        mPresenter.pay(jsonObject, listener);
    }

    @Override
    public void logout() {
        getAppComponent().eventBus().postSticky(new TokenPaymentExpiredEvent());
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
        return this;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshWeb(RefreshWebEvent event) {
        refreshWeb();
    }

    private void showBottomSheetDialog() {
        final WebBottomSheetDialogFragment dialog = WebBottomSheetDialogFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("currenturl", mWebViewProcessor.getCurrentUrl());
        dialog.setArguments(bundle);
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }
}

