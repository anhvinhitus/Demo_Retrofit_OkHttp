package vn.com.vng.zalopay.webview.widget;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.webview.config.WebViewConfig;
import vn.com.vng.zalopay.webview.interfaces.ITimeoutLoadingListener;
import vn.com.zalopay.wallet.listener.ZPWOnProgressDialogTimeoutListener;

public class ZPWebViewProcessor extends WebViewClient {

    private boolean hasError = false;

    private ZPWebView mWebView = null;
    private ITimeoutLoadingListener mTimeOutListener;
    private IWebViewListener mWebViewListener;

    public ZPWebViewProcessor(ZPWebView pWebView,
                              ITimeoutLoadingListener timeoutLoadingListener,
                              IWebViewListener webViewListener) {
        mWebView = pWebView;
        mWebViewListener = webViewListener;
        mTimeOutListener = timeoutLoadingListener;
        mWebView.setWebViewClient(this);
    }

    public void start(final String pUrl, final Activity pActivity) {
        if (pActivity == null || TextUtils.isEmpty(pUrl) || mWebView == null) {
            return;
        }
        DialogHelper.showLoading(pActivity, new ZPWOnProgressDialogTimeoutListener() {
            @Override
            public void onProgressTimeout() {
                if (mTimeOutListener != null)
                    mTimeOutListener.onTimeoutLoading();
            }
        });
        hasError = false;
        mWebView.loadUrl(pUrl);
    }

    public boolean hasError() {
        return hasError;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Timber.d("onPageFinished url [%s]", url);
        if (hasError || mWebView == null) {
            return;
        }

        DialogHelper.hideLoading();
        injectScriptFile("webapp.js");

        mWebView.runScript("webapp_hideHeaderZalo()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Timber.d("hideHeaderZalo [%s]", s);
            }
        });
        mWebView.runScript("webapp_getNavigation()", new GetNavigationCallback(mWebViewListener));

        super.onPageFinished(view, url);

        if (mWebViewListener != null) {
            mWebViewListener.onPageFinished(url);
        }
    }

    private void injectScriptFile(String scriptFile) {
        InputStream input;
        try {
            input = mWebView.getContext().getAssets().open(scriptFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            // Stringify the script byte-array using BASE64 encoding
            String endcoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            mWebView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    // Tell the browser to BASE64-decode the string into your script
                    "script.innerHTML = window.atob('" + endcoded + "');" +
                    "parent.appendChild(script);" +
                    "})()"
            );
        } catch (IOException e) {
            Timber.w(e, "Exception");
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

    public boolean canBack() {
        return mWebViewListener.isPageValid();
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
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading: %s", url);
        //use case for url
        if (TextUtils.isEmpty(url)) {
            return true;
        }
        if (url.equalsIgnoreCase(WebViewConfig.URL_TO_APP)) {
            if (mWebViewListener != null) {
                mWebViewListener.finishActivity();
            }
        } else if (url.equalsIgnoreCase(WebViewConfig.URL_TO_LOGIN)) {
            if (mWebViewListener != null) {
                mWebViewListener.logout();
            }
        } else if (url.startsWith(WebViewConfig.URL_PAY)) {
            if (mWebViewListener != null) {
                mWebViewListener.payOrder(url);
            }
        } else {
            if (url.contains(WebViewConfig.URL_LOGIN_ZALO)) {
                clearCookieZalo();
            }
            view.loadUrl(url);
        }

        return true;
    }

    private void clearCookieZalo() {
        if (mWebView == null) {
            return;
        }
        mWebView.clearCookies("oauth.zaloapp.com");
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

        boolean isPageValid();

        void setPageValid(boolean valid);
    }
}
