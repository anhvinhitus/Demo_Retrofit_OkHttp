package vn.com.zalopay.wallet.business.webview.base;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.webview.atm.BankWebView;
import vn.com.zalopay.wallet.business.webview.atm.BankWebViewClient;
import vn.com.zalopay.wallet.business.webview.atm.NewBankWebViewClient;
import vn.com.zalopay.wallet.utils.GsonUtils;

public abstract class PaymentWebViewClient extends WebViewClient {
    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    protected WeakReference<AdapterBase> mAdapter;
    protected BankWebView mWebPaymentBridge = null;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public PaymentWebViewClient(AdapterBase pAdapter) {
        mAdapter = new WeakReference<AdapterBase>(pAdapter);
    }

    public static PaymentWebViewClient createPaymentWebViewClientByBank(AdapterBase pAdapter) {
        if (pAdapter instanceof AdapterBankCard && ((AdapterBankCard) pAdapter).isBidvBankPayment()) {
            return new NewBankWebViewClient(pAdapter);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mWebPaymentBridge.removeJavascriptInterface(JAVA_SCRIPT_INTERFACE_NAME);
            }
            mWebPaymentBridge.setWebViewClient(null);
            mWebPaymentBridge.removeAllViews();
            mWebPaymentBridge.clearHistory();
            mWebPaymentBridge.freeMemory();
            mWebPaymentBridge.destroy();
            mWebPaymentBridge = null;
            Log.d(this, "disposed mWebPaymentBridge");
        }
        if (compositeSubscription.hasSubscriptions()) {
            compositeSubscription.clear();
            Log.d(this, "cleared subscriptions");
        }
    }

    public void getSiteContent() {
        mWebPaymentBridge.loadUrl("javascript:window.zingpaysdk_wv.getHtml('<head>'+document.getElementsByTagName('body')[0].innerHTML+'</head>');");
    }

    public abstract void start(String pUrl);

    public abstract void hit();

    protected void executeJs(String pJsFileName, String pJsInput) {
        if (!TextUtils.isEmpty(pJsFileName)) {
            Log.d(this, pJsFileName);
            Log.d(this, pJsInput);
            for (String jsFile : pJsFileName.split(Constants.COMMA)) {
                Subscription subscription = ResourceManager.getJavascriptContent(jsFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleSubscriber<String>() {
                            @Override
                            public void onSuccess(String fileContent) {
                                if (!TextUtils.isEmpty(fileContent)) {
                                    String jsContent = String.format(fileContent, pJsInput);
                                    mWebPaymentBridge.runScript(jsContent);
                                }
                            }

                            @Override
                            public void onError(Throwable error) {
                                Log.e(this, "load file js error " + GsonUtils.toJsonString(error));
                                getAdapter().onEvent(EEventType.ON_FAIL);
                            }
                        });
                compositeSubscription.add(subscription);
            }
        }
    }

    @JavascriptInterface
    public void logDebug(String msg) {
        Log.d(this, "****** Debug webview: " + msg);
    }
}
