package vn.com.zalopay.wallet.business.webview.base;

import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.webview.atm.BankWebView;
import vn.com.zalopay.wallet.business.webview.atm.BankWebViewClient;
import vn.com.zalopay.wallet.business.webview.atm.BidvWebViewClient;

public abstract class PaymentWebViewClient extends WebViewClient {
    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    protected WeakReference<AdapterBase> mAdapter;
    protected BankWebView mWebPaymentBridge = null;

    public PaymentWebViewClient(AdapterBase pAdapter) {
        mAdapter = new WeakReference<>(pAdapter);
    }

    public static PaymentWebViewClient createPaymentWebViewClientByBank(AdapterBase pAdapter) {
        if (pAdapter instanceof AdapterBankCard && ((AdapterBankCard) pAdapter).paymentBIDV()) {
            return new BidvWebViewClient(pAdapter);
        } else {
            return new BankWebViewClient(pAdapter);
        }
    }

    protected AdapterBase getAdapter() {
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
        }
    }

    public void getSiteContent() {
        mWebPaymentBridge.loadUrl("javascript:window.zingpaysdk_wv.getHtml('<head>'+document.getElementsByTagName('body')[0].innerHTML+'</head>');");
    }

    public abstract void start(String pUrl);

    public abstract void hit();
}
