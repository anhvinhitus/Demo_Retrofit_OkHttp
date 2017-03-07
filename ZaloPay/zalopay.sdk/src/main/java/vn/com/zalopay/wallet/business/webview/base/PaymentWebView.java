package vn.com.zalopay.wallet.business.webview.base;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.webview.creditcard.CCWebViewClient;
import vn.com.zalopay.wallet.utils.Log;

public class PaymentWebView extends WebView {
    protected String mRecentLoadingUrl;

    public PaymentWebView(Context context) {
        super(context);
        init(context);
    }

    public PaymentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaymentWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context pContext) {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setSupportZoom(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setSavePassword(false);
        getSettings().setSaveFormData(false);
    }

    public void setUserAgent(String pUserAgent) {
        getSettings().setUserAgentString(pUserAgent);
    }

    public void setPaymentWebViewClient(AdapterBase pAdapter) {
        setWebViewClient(new CCWebViewClient(pAdapter));
    }

    public void reloadPaymentUrl() {
        if (!TextUtils.isEmpty(mRecentLoadingUrl)) {
            Log.d(this, "===reloadPaymentUrl===mRecentLoadingUrl=" + mRecentLoadingUrl);
            loadUrl("onclick='window.history.back()'");
            loadPaymentUrl(mRecentLoadingUrl);
        } else {
            Log.e(this, "===reloadPaymentUrl===mRecentLoadingUrl=NULL");
        }
    }

    public void loadPaymentUrl(String pUrl) {
        Log.d(this, "===loadPaymentUrl===pUrl=" + pUrl);
        mRecentLoadingUrl = pUrl;
        loadUrl(pUrl);
    }

    public void release() {
        destroy();
    }
}
