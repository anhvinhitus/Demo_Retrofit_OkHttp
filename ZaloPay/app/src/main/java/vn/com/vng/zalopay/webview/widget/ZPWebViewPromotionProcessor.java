package vn.com.vng.zalopay.webview.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;
import vn.com.vng.zalopay.webview.config.WebViewConfig;

public class ZPWebViewPromotionProcessor extends WebViewClient implements GetNavigationCallback.INavigationListener {

    private String mCurrentUrl = "";
    private boolean mHasError = false;
    private boolean mLoadPageFinished = false;

    private ZPWebView mWebView = null;
    private IWebViewPromotionListener mWebViewListener;

    public ZPWebViewPromotionProcessor(ZPWebView pWebView,
                                       IWebViewPromotionListener webViewListener) {
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
        mWebViewListener.setRefresh(false);
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
        injectScriptFile("webapp.js");

        mWebView.runScript("webapp_hideHeaderZalo()", s -> Timber.d("hideHeaderZalo [%s]", s));
        mWebView.runScript("webapp_getNavigation()", new GetNavigationCallback(this));

        super.onPageFinished(view, url);

        mCurrentUrl = url;
        mWebView.addHost(mCurrentUrl);
        showWebView();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request == null) {
            return false;
        }
        String url = request.getUrl().toString();
        Timber.d("shouldOverrideUrlLoading  uri[%s]", url);
        return !TextUtils.isEmpty(url) && shouldOverrideUrlLoading(view, url);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading url[%s]", url);
        //use case for url
        if (url.contains("detail")) {
            if (mWebViewListener != null) {
                mWebViewListener.openWebDetail(url);
            }
        } else {
            view.loadUrl(url);
        }
        return true;
    }

    public void onDestroy() {

    }

    public void refreshWeb(Activity activity) {
        start(mCurrentUrl, activity);
    }

    public String getCurrentUrl() {
        return mCurrentUrl;
    }


    @Override
    public void setTitleAndLogo(String title, String thumb) {

    }

    @Override
    public void onWebAppStateChanged(boolean valid) {

    }

    public interface IWebViewPromotionListener {

        void showError(int errorCode);

        void openWebDetail(String url);

        void setRefresh(boolean refresh);
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
