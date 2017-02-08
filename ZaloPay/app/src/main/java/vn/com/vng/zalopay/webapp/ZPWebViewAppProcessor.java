package vn.com.vng.zalopay.webapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.webview.config.WebViewConfig;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

public class ZPWebViewAppProcessor extends WebViewClient {

    private String mCurrentUrl = "";
    private boolean mIsPageValid = false;
    private boolean mHasError = false;
    private boolean mLoadPageFinished = false;

    private ZPWebViewApp mWebView = null;
    private IWebViewListener mWebViewListener;
    private WebAppCommunicationHandler mCommunicationHandler;

    public ZPWebViewAppProcessor(ZPWebViewApp pWebView,
                                 IWebViewListener webViewListener) {
        mWebView = pWebView;
        mWebViewListener = webViewListener;
        mWebView.setWebViewClient(this);
        mWebView.addJavascriptInterface(this, "ZaloPayJSBridge");
        //mWebView.addJavascriptInterface(this, "AlipayJSBridge");

        mCommunicationHandler = new WebAppCommunicationHandler(pWebView, webViewListener);
    }

    public void start(final String pUrl, final Activity pActivity) {
        if (pActivity == null || TextUtils.isEmpty(pUrl) || mWebView == null) {
            return;
        }
        mHasError = false;
        mLoadPageFinished = false;
        mCurrentUrl = pUrl;
        Timber.d("start load url [%s]", pUrl);
        mWebView.loadUrl(pUrl);
    }

    public boolean hasError() {
        return mHasError;
    }

    public boolean isLoadPageFinished() {
        return mLoadPageFinished;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        showLoading();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Timber.d("onPageFinished url [%s]", url);
        if (mHasError || mWebView == null) {
            return;
        }
        mLoadPageFinished = true;
        hideLoading();
        injectScriptFile("webapp.js");
        injectScriptFile("jsbridge.js");
        mWebView.runScript("initializeWebBridge();", null);

        mCurrentUrl = url;
        showWebView();

        super.onPageFinished(view, url);
    }
//
//    private void executeScriptFile(String scriptFile) {
//        InputStream input;
//        try {
//            input = mWebView.getContext().getAssets().open(scriptFile);
//            byte[] buffer = new byte[input.available()];
//            input.read(buffer);
//            input.close();
//            // byte buffer into a string
//            String text = new String(buffer);
//            mWebView.runScript(text, null);
//        } catch (IOException e) {
//            Timber.w(e, "Exception");
//        }
//    }

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
                    "script.messageType = 'text/javascript';" +
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

    public void runScript(String script, ValueCallback<String> callback) {
        if (mWebView == null) {
            return;
        }
        mWebView.runScript(script, callback);
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

    public boolean canBack() {
        return mIsPageValid;
    }

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
        Timber.d("shouldOverrideUrlLoading  uri [%s]", url);
        return !TextUtils.isEmpty(url) && shouldOverrideUrlLoading(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading url [%s]", url);
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

    }

    private void showLoading() {
        if (mWebViewListener != null) {
            mWebViewListener.showLoading();
        }
    }

    private void hideLoading() {
        if (mWebViewListener != null) {
            mWebViewListener.hideLoading();
        }
    }

    public void refreshWeb(Activity activity) {
        start(mCurrentUrl, activity);
    }

    public String getCurrentUrl() {
        return mCurrentUrl;
    }

    public interface IWebViewListener {

        void payOrder(String url);

        void logout();

        void finishActivity();

        void showError(int errorCode);

        void showLoading();

        void hideLoading();

        void showDialog(int dialogType, String title, String message, String buttonLabel);

        Context getContext();
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
        mCommunicationHandler.cleanup();
    }

    public boolean onBackPress() {
        if(mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }

    @android.webkit.JavascriptInterface
    void callNativeFunction(String a1, String messageData) {
        Timber.d("Invoke function with data: %s", messageData);
        if (TextUtils.isEmpty(messageData)) {
            return;
        }

        // message = {"func":"vibrate","param":{"duration":3000},"msgType":"call","clientId":"14865289272660.004411039873957634"}

        // preHandleWebMessage(messageData);
        mCommunicationHandler.preHandleWebMessage(messageData);
    }

}
