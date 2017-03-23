package vn.com.vng.zalopay.webview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.Lists;

public class ZPWebView extends WebView {
    private List<String> mHosts;
    private Map<String, String> mCookiesMap;

    public ZPWebView(Context context) {
        super(context);
        init();
    }

    public ZPWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZPWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void appendUserAgent(String userAgent) {
        if (TextUtils.isEmpty(userAgent)) {
            return;
        }
        String newUserAgent = getSettings().getUserAgentString() + " " + userAgent;
        getSettings().setUserAgentString(newUserAgent);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setSavePassword(false);
        getSettings().setSaveFormData(false);
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return true;
            }
        });
        mHosts = new ArrayList<>();
        mCookiesMap = new HashMap<>();
    }

    public void runScript(String scriptContent, ValueCallback<String> resultCallback) {
        Timber.d("##### runScript: %s", scriptContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(scriptContent, resultCallback);
        } else {
            loadUrl("javascript:{" + scriptContent + "}");
        }
    }

    private void saveCookies(String rejectUrl) {
        if (Lists.isEmptyOrNull(mHosts)) {
            return;
        }
        for (String host : mHosts) {
            if (TextUtils.isEmpty(host) || host.equals(rejectUrl)) {
                continue;
            }
            mCookiesMap.put(host, CookieManager.getInstance().getCookie(host));
        }
    }

    private void restoreCookies() {
        if (mCookiesMap == null || mCookiesMap.size() <= 0) {
            return;
        }
        for (Map.Entry<String, String> entry : mCookiesMap.entrySet()) {
            if (entry == null
                    || TextUtils.isEmpty(entry.getKey())
                    || TextUtils.isEmpty(entry.getValue())) {
                continue;
            }
            CookieManager.getInstance().setCookie(entry.getKey(), entry.getValue());
        }
        mCookiesMap.clear();
    }

    @SuppressWarnings("deprecation")
    public void clearCookies(String url) {
        saveCookies(url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Timber.d("Using clearCookies code for API >= [%s] url [%s]",
//                    String.valueOf(Build.VERSION_CODES.LOLLIPOP), url);
            clearAllCookies();
            clearSessionCookie();
            CookieManager.getInstance().flush();
        } else {
//            Timber.d("Using clearCookies code for API < [%s] url [%s]",
//                    String.valueOf(Build.VERSION_CODES.LOLLIPOP), url);
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getContext());
            cookieSyncManager.startSync();
            clearAllCookies();
            clearSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
        restoreCookies();
    }

    @SuppressWarnings("deprecation")
    private void clearAllCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else {
            cookieManager.removeAllCookie();
        }
    }

    @SuppressWarnings("deprecation")
    private void clearSessionCookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(value ->
                    Timber.d("clearSessionCookie result [%s]", value));
        } else {
            cookieManager.removeSessionCookie();
        }
    }

    public void addHost(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (TextUtils.isEmpty(host) || "null".equals(host) || mHosts.contains(host)) {
            return;
        }
        Timber.d("addHost[%s]", host);
        mHosts.add(host);
    }
}
