package vn.com.vng.zalopay.webapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Vector;

import timber.log.Timber;

public class ZPWebViewApp extends WebView {

    public ZPWebViewApp(Context context) {
        super(context);
        init();
    }

    public ZPWebViewApp(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZPWebViewApp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36 ZaloPay/2.8");

        settings.setBlockNetworkImage(false);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setSavePassword(false);
        settings.setSaveFormData(false);
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

        addJavascriptInterface(new JavascriptInterface(), "ZaloPayJSBridge");
    }

    public void runScript(String scriptContent, ValueCallback<String> resultCallback) {
        Timber.d("##### runScript: %s", scriptContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(scriptContent, resultCallback);
        } else {
            loadUrl("javascript:{" + scriptContent + "}");
        }
    }

    @SuppressWarnings("deprecation")
    public void clearCookies(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("Using clearCookies code for API >= [%s]", String.valueOf(Build.VERSION_CODES.LOLLIPOP));
            clearCookieByUrl(url);
            clearSessionCookie();
            CookieManager.getInstance().flush();
        } else {
            Timber.d("Using clearCookies code for API < [%s]", String.valueOf(Build.VERSION_CODES.LOLLIPOP));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getContext());
            cookieSyncMngr.startSync();
            clearCookieByUrl(url);
            clearSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void clearCookieByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        Timber.d("clearCookieByUrl host [%s]", host);
        clearCookieByUrlInternal(url);
        clearCookieByUrlInternal("http://." + host);
        clearCookieByUrlInternal("https://." + host);
    }

    @SuppressWarnings("deprecation")
    private void clearSessionCookie() {
        CookieManager pCookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pCookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    Timber.d("clearCookieByUrlInternal removeSessionCookies [%s]", value);
                }
            });
        } else {
            pCookieManager.removeSessionCookie();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void clearCookieByUrlInternal(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        CookieManager pCookieManager = CookieManager.getInstance();
        String cookieString = pCookieManager.getCookie(url);
        //Timber.d("clearCookieByUrlInternal cookieString [%s]", cookieString);
        Vector<String> cookie = getCookieNamesByUrl(cookieString);
        if (cookie == null || cookie.isEmpty()) {
            return;
        }
        for (int i = 0; i < cookie.size(); i++) {
            //Timber.d("clearCookieByUrlInternal cookie [%s]", cookie.get(i));
            pCookieManager.setCookie(url, cookie.get(i) + "=");
        }
    }

    private Vector<String> getCookieNamesByUrl(String cookie) {
        //Timber.d("getCookieNamesByUrl cookie [%s]", cookie);
        if (TextUtils.isEmpty(cookie)) {
            return null;
        }
        String[] cookieField = cookie.split(";");
        int len = cookieField.length;
        for (int i = 0; i < len; i++) {
            cookieField[i] = cookieField[i].trim();
        }
        Vector<String> allCookieField = new Vector<>();
        for (String aCookieField : cookieField) {
            //Timber.d("getCookieNamesByUrl cookie [%s]", aCookieField);
            if (TextUtils.isEmpty(aCookieField)) {
                continue;
            }
            if (!aCookieField.contains("=")) {
                continue;
            }
            String[] singleCookieField = aCookieField.split("=");
            allCookieField.add(singleCookieField[0]);
        }
        return allCookieField;
    }
}
