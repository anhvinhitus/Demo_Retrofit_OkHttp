package vn.com.zalopay.wallet.business.webview.atm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.utils.Log;

public class BankWebView extends PaymentWebView {

    public BankWebView(Context context) {
        super(context);
        initForParseBank(context);
    }

    public BankWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initForParseBank(context);
    }

    public BankWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initForParseBank(context);
    }

    private void initForParseBank(Context context) {
        getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36");
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
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      JsPromptResult result) {
                return true;
            }
        });

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

}
