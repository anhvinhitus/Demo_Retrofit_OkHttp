package vn.com.zalopay.wallet.business.webview.base;

import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.BankCardWorkFlow;
import vn.com.zalopay.wallet.business.webview.atm.BankWebView;
import vn.com.zalopay.wallet.business.webview.atm.BankWebViewClient;
import vn.com.zalopay.wallet.business.webview.atm.BidvWebViewClient;

public abstract class PaymentWebViewClient extends WebViewClient {
    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    protected WeakReference<AbstractWorkFlow> mAdapter;
    protected BankWebView mWebPaymentBridge = null;

    public PaymentWebViewClient(AbstractWorkFlow pAdapter) {
        mAdapter = new WeakReference<>(pAdapter);
    }

    public static PaymentWebViewClient createPaymentWebViewClientByBank(AbstractWorkFlow pAdapter) {
        if (pAdapter instanceof BankCardWorkFlow && ((BankCardWorkFlow) pAdapter).paymentBIDV()) {
            return new BidvWebViewClient(pAdapter);
        } else {
            return new BankWebViewClient(pAdapter);
        }
    }

    protected AbstractWorkFlow getAdapter() {
        if (mAdapter != null) {
            return mAdapter.get();
        }
        return null;
    }

    public String getCurrentUrl() {
        return mWebPaymentBridge.getUrl();
    }

    public void dispose() {
        if (mWebPaymentBridge != null) {
            mWebPaymentBridge.removeJavascriptInterface(JAVA_SCRIPT_INTERFACE_NAME);
            mWebPaymentBridge.setWebViewClient(null);
            mWebPaymentBridge.removeAllViews();
            mWebPaymentBridge.clearHistory();
            mWebPaymentBridge.freeMemory();
            mWebPaymentBridge.destroy();
            mWebPaymentBridge = null;
            Timber.w("release webview client");
        }
    }

    public void getSiteContent() {
        mWebPaymentBridge.loadUrl("javascript:window.zingpaysdk_wv.getHtml('<head>'+document.getElementsByTagName('body')[0].innerHTML+'</head>');");
    }

    public abstract void start(String pUrl);

    public abstract void stop();

    public abstract void hit();
}
