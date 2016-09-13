package vn.com.zalopay.game.ui.component.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;
import vn.com.zalopay.game.ui.webview.AppGameWebView;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;

/**
 * Created by chucvv on 8/28/16.
 * Fragment
 */
public abstract class AppGameFragment extends Fragment
        implements AppGameWebViewProcessor.IWebViewListener {
    private AppGameWebView mWebview;
    private AppGameWebViewProcessor mWebViewProcessor;
    protected String mCurrentUrl = "";

    private View layoutRetry;
    private ImageView imgError;
    private TextView tvError;
    private View btnRetry;

    protected abstract int getResLayoutId();

    protected abstract String getWebViewUrl();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getResLayoutId(), container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Timber.d("onViewCreated start");
        initViewWebView(view);
        initRetryView(view);
        loadUrl(getWebViewUrl());
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViewWebView(View rootView) {
        mWebview = (AppGameWebView) rootView.findViewById(R.id.webview);
        mWebViewProcessor = new AppGameWebViewProcessor(mWebview, this);
    }

    private void initRetryView(View rootView) {
        layoutRetry = rootView.findViewById(R.id.layoutRetry);
        imgError = (ImageView) rootView.findViewById(R.id.imgError);
        tvError = (TextView) rootView.findViewById(R.id.tvError);
        btnRetry = rootView.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideError();
                refreshWeb();
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
            if (AppGameGlobal.getNetworking().isOnline(getContext())) {
                showErrorNoLoad();
            } else {
                showErrorNoConnection();
            }
        } else {
            showErrorNoLoad();
        }

    }

    public void hideError() {
        Timber.d("hideError layoutRetry [%s]", layoutRetry);
        if (layoutRetry == null) {
            return;
        }
        layoutRetry.setVisibility(View.GONE);
    }

    public boolean canBack() {
        boolean canBack = false;

        if (mWebview != null) {
            canBack = mWebview.canGoBack();
        }

        return canBack;
    }

    public void goBack() {
        if (mWebview != null)
            mWebview.goBack();
    }

    public void loadUrl(final String pUrl) {
        if (mWebViewProcessor == null) {
            return;
        }
        mCurrentUrl = pUrl;
        mWebViewProcessor.start(mCurrentUrl, getActivity(), new ITimeoutLoadingListener() {
            @Override
            public void onTimeoutLoading() {
                Timber.d("onProgressTimeout-%s", pUrl);
                //load website timeout, show confirm dialog: continue to load or exit.
                if (AppGameGlobal.getDialog() != null)
                    AppGameGlobal.getDialog().showConfirmDialog(AppGameBaseActivity.getCurrentActivity(), getResources().getString(R.string.appgame_waiting_loading),
                            getResources().getString(R.string.appgame_button_left), getResources().getString(R.string.appgame_button_right), new IDialogListener() {
                                @Override
                                public void onClose() {
                                    AppGameBaseActivity.getCurrentActivity().finish();
                                }
                            });
            }
        });
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
        mWebview.runScript("utils.back()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Timber.d("navigation back: %s", value);
            }
        });
        return true;
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


    private void refreshWeb() {
        Timber.d("Request to reload web view");
        hideError();
        loadUrl(mCurrentUrl);
    }

    @Override
    public void onReceivedError(int errorCode, CharSequence description) {
        Timber.d("onReceivedError errorCode [%s] description [%s]", errorCode, description);
        showError(errorCode);
    }

    @Override
    public void onPageFinished(String url) {
        Timber.d("onPageFinished url [%s]", url);
    }
}
