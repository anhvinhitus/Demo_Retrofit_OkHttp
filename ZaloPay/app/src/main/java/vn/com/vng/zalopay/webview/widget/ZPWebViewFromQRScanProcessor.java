package vn.com.vng.zalopay.webview.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

public class ZPWebViewFromQRScanProcessor extends WebViewClient {

    private String mCurrentUrl = "";
    private boolean mHasError = false;
    private boolean mLoadPageFinished = false;

    private ZPWebView mWebView = null;
    private IWebViewFromQRScanListener mWebViewListener;

    public ZPWebViewFromQRScanProcessor(ZPWebView pWebView,
                                        IWebViewFromQRScanListener webViewListener) {
        mWebView = pWebView;
        mWebViewListener = webViewListener;
        mWebView.setWebViewClient(this);
    }

    public void start(final String pUrl, final Activity pActivity) {
        if (pActivity == null || TextUtils.isEmpty(pUrl) || mWebView == null) {
            return;
        }
        mHasError = false;
        mLoadPageFinished = false;
        mCurrentUrl = pUrl;
        Timber.d("start load url[%s]", pUrl);
        mWebView.loadUrl(pUrl);
    }

    public boolean isLoadPageFinished() {
        return mLoadPageFinished;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Timber.d("onPageFinished url [%s]", url);
        if (mHasError || mWebView == null) {
            return;
        }

        mLoadPageFinished = true;

        super.onPageFinished(view, url);

        mCurrentUrl = url;
        mWebView.addHost(mCurrentUrl);
        showWebView();
    }

    private void onReceivedError(int errorCode, CharSequence description) {
        Timber.d("onReceivedError errorCode [%s] description [%s]", errorCode, description);
        hideWebView();
        if (mWebViewListener != null) {
            mWebViewListener.showError(errorCode);
        }
    }

    private void hideWebView() {
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }
    }

    private void showWebView() {
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }

        mHasError = true;
        Timber.w("Webview errorCode [%s] description [%s] failingUrl [%s]", errorCode, description, failingUrl);
        onReceivedError(errorCode, description);

        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        mHasError = true;
        int errorCode = error != null ? error.getErrorCode() : WebViewClient.ERROR_UNKNOWN;
        CharSequence description = error != null ? error.getDescription() : null;
        Timber.w("Webview errorCode [%s] errorMessage [%s]", errorCode, description);
        onReceivedError(errorCode, description);
    }

    public void refreshWeb(Activity activity) {
        start(mCurrentUrl, activity);
    }

    public String getCurrentUrl() {
        return mCurrentUrl;
    }

    public interface IWebViewFromQRScanListener {

        void showError(int errorCode);
    }

    public void onPause() {
        if (mWebView == null) {
            return;
        }
        mWebView.pauseTimers(); //pause layout, js
        mWebView.onPause();
    }

    public void onResume() {
        if (mWebView == null) {
            return;
        }
        mWebView.onResume();
        mWebView.resumeTimers(); //resume layout, js
    }

    public void onDestroyView() {
        if (mWebView == null) {
            return;
        }

        try {
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
        } catch (Exception ex) {
            //empty
        }

        try {
            mWebView.removeAllViews();
        } catch (Exception ex) {
            //empty
        }

        mWebView.destroy();

        mWebViewListener = null;
    }

    public boolean onBackPress() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }
}
