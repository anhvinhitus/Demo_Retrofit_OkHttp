package vn.com.zalopay.game.webview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.game.config.AppGameConfig;

public class AppGameWebViewProcessor extends WebViewClient {
    private final String JAVA_SCRIPT_INTERFACE_NAME = "zalopay_appgame";

    private boolean hasError = false;

    private AppGameWebView mWebView = null;
    private ITimeoutLoadingListener mTimeOutListener;
    private IWebViewListener mWebViewListener;
    private IDialog mDialog;

    public AppGameWebViewProcessor(AppGameWebView pWebView, IDialog dialog,
                                   ITimeoutLoadingListener timeoutLoadingListener,
                                   IWebViewListener webViewListener) {
        mWebView = pWebView;
        mWebViewListener = webViewListener;
        mDialog = dialog;
        mTimeOutListener = timeoutLoadingListener;
        mWebView.setWebViewClient(this);
        //ensure the method is called only when running on Android 4.2 or later for secure
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        }
    }

    public void start(final String pUrl, final Activity pActivity) {
        if (pActivity == null || TextUtils.isEmpty(pUrl)) {
            return;
        }
        if (mDialog != null) {
            mDialog.showLoadingDialog(pActivity, mTimeOutListener);
        }
        hasError = false;
        mWebView.loadUrl(pUrl);
    }

    public boolean hasError() {
        return hasError;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Timber.d("onPageFinished url [%s]", url);
        if (hasError) {
            return;
        }

        if (mDialog != null) {
            mDialog.hideLoadingDialog();
        }

        mWebView.runScript("utils.getNav()", new GetNavigationCallback(mWebViewListener));

        super.onPageFinished(view, url);

        if (mWebViewListener != null) {
            mWebViewListener.onPageFinished(url);
        }

    }

    private void onReceivedError(int errorCode, CharSequence description) {
        if (mWebViewListener != null) {
            mWebViewListener.onReceivedError(errorCode, description);
        }
    }

    public void runScript(String script, ValueCallback<String> callback) {
        if (mWebView == null) {
            return;
        }
        mWebView.runScript(script, callback);
    }

    public void hideWebView() {
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }
    }

    public void showWebView() {
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unused")
    public boolean canBack() {
        boolean canBack = false;

        if (mWebView != null) {
            canBack = mWebView.canGoBack();
        }

        return canBack;
    }

    @SuppressWarnings("unused")
    public void goBack() {
        if (mWebView != null) {
            mWebView.goBack();
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        hasError = true;
        Timber.w("Webview errorCode [%s] description [%s] failingUrl [%s]", errorCode, description, failingUrl);
        onReceivedError(errorCode, description);

        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        hasError = true;
        int errorCode = error != null ? error.getErrorCode() : WebViewClient.ERROR_UNKNOWN;
        CharSequence description = error != null ? error.getDescription() : null;
        Timber.w("Webview errorCode [%s] errorMessage [%s]", errorCode, description);
        onReceivedError(errorCode, description);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("===shouldOverrideUrlLoading===%s", url);
        //use case for url
        if (!TextUtils.isEmpty(url) && url.equalsIgnoreCase(AppGameConfig.URL_TO_APP)) {
            if (mWebViewListener != null) {
                mWebViewListener.finishActivity();
            }
        } else if (!TextUtils.isEmpty(url) && url.equalsIgnoreCase(AppGameConfig.URL_TO_LOGIN)) {
            if (mWebViewListener != null) {
                mWebViewListener.logout();
            }
        } else if (url.startsWith("zalopay-1://post")) {
            if (mWebViewListener != null) {
                mWebViewListener.payOrder(url);
            }
        } else {
            view.loadUrl(url);
        }

        return true;
    }

    public void onLoadResource(WebView view, String url) {
        Log.d("onLoadResource ", url);
    }

    @JavascriptInterface
    public void onJsCallBackResult(String pResult) {
        Timber.d("JsCallBackResult [%s]", pResult);
    }

    public void onDestroy() {
        mTimeOutListener = null;
    }

    public interface IWebViewListener {
        void onReceivedError(int errorCode, CharSequence description);

        void onPageFinished(String url);

        void payOrder(String url);

        void logout();

        void finishActivity();

        void setTitleAndLogo(String title, String url);
    }
}
