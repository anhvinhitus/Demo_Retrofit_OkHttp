package vn.com.zalopay.wallet.workflow.webview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

import timber.log.Timber;

public class SdkWebView extends WebView {
    protected String mRecentLoadingUrl;

    public SdkWebView(Context context) {
        super(context);
        init();
    }

    public SdkWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SdkWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setSupportZoom(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setSavePassword(false);
        getSettings().setSaveFormData(false);
        setUserAgent();
    }

    private void setUserAgent() {
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

    public void reloadLastUrl() {
        if (!TextUtils.isEmpty(mRecentLoadingUrl)) {
            startLoadUrl(mRecentLoadingUrl);
        }
    }

    public void startLoadUrl(String pUrl) {
        Timber.d("load url %s", pUrl);
        mRecentLoadingUrl = pUrl;
        loadUrl(pUrl);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void runScript(String scriptContent) {
        Timber.d("start runScript: %s", scriptContent);
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
