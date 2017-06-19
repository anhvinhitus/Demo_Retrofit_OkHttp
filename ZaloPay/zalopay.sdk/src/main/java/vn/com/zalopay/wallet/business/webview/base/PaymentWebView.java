package vn.com.zalopay.wallet.business.webview.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.zalopay.ui.widget.util.AgentUtil;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.webview.creditcard.CCWebViewClient;

public class PaymentWebView extends WebView {
    protected String mRecentLoadingUrl;

    public PaymentWebView(Context context) {
        super(context);
        init();
    }

    public PaymentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaymentWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setSupportZoom(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setSavePassword(false);
        getSettings().setSaveFormData(false);

        // set user agent. mobile
        getSettings().setUserAgentString(AgentUtil.getUserAgent());
    }

    public void setPaymentWebViewClient(AdapterBase pAdapter) {
        setWebViewClient(new CCWebViewClient(pAdapter));
    }

    public void reloadPaymentUrl() {
        if (!TextUtils.isEmpty(mRecentLoadingUrl)) {
            loadPaymentUrl(mRecentLoadingUrl);
        }
    }

    public void loadPaymentUrl(String pUrl) {
        Log.d(this, "load url " + pUrl);
        mRecentLoadingUrl = pUrl;
        loadUrl(pUrl);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void runScript(String scriptContent) {
        Log.d(this, "##### runScript: " + scriptContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(scriptContent, null);
        } else {
            loadUrl("javascript:{" + scriptContent + "}");
        }
    }

    public void release() {
        destroy();
    }
}
