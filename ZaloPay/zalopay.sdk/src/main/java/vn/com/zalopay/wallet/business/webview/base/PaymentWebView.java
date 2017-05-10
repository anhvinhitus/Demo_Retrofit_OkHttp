package vn.com.zalopay.wallet.business.webview.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.webview.creditcard.CCWebViewClient;
import vn.com.zalopay.wallet.utils.Log;

public class PaymentWebView extends WebView {
    protected String mRecentLoadingUrl;
    protected CCWebViewClient mCCWebViewClient ;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String UA_LOLLIPOP_AND_ABOVE = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36";
            getSettings().setUserAgentString(UA_LOLLIPOP_AND_ABOVE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String UA_KITKAT_TO_LOLLIPOP = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";
            getSettings().setUserAgentString(UA_KITKAT_TO_LOLLIPOP);
        } else {
            String UA_OLD = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30";
            getSettings().setUserAgentString(UA_OLD);
        }
    }

    public void setPaymentWebViewClient(AdapterBase pAdapter) {
        mCCWebViewClient = new CCWebViewClient(pAdapter);
        setWebViewClient(mCCWebViewClient);
    }
 public CCWebViewClient getCCWebViewClient()
 {
     return mCCWebViewClient;
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
        Log.d(this, "runScript: " + scriptContent);
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
