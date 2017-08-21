package vn.com.zalopay.wallet.workflow.webview;

import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

public abstract class AbstractWebViewClient extends WebViewClient {
    static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    private WeakReference<AbstractWorkFlow> mWorkFlow;
    private WeakReference<SdkWebView> mWebView;

    public void initialize(AbstractWorkFlow pAdapter, SdkWebView pWebview) {
        mWorkFlow = new WeakReference<>(pAdapter);
        mWebView = new WeakReference<>(pWebview);
        if (pWebview != null) {
            pWebview.setWebViewClient(this);
        }
    }

    protected AbstractWorkFlow getWorkFlow() throws Exception {
        if (mWorkFlow == null || mWorkFlow.get() == null) {
            throw new IllegalAccessException("Workflow is released");
        }
        return mWorkFlow.get();
    }

    protected SdkWebView getWebView() throws Exception {
        if (mWebView == null || mWebView.get() == null) {
            throw new IllegalAccessException("web view is released");
        }
        return mWebView.get();
    }

    void executeJs(String pJsFileName, String pJsInput) {
        if (TextUtils.isEmpty(pJsFileName)) {
            return;
        }
        SdkWebView webView = null;
        try {
            webView = getWebView();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (webView == null) {
            Timber.d("NULL on executeJs");
            return;
        }
        AbstractWorkFlow workFlow = null;
        try {
            workFlow = getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            return;
        }
        Timber.d("file name %s input %s", pJsFileName, pJsInput);
        SdkWebView finalWebView = webView;
        Subscription subscription = Observable.from(pJsFileName.split(Constants.COMMA))
                .filter(s -> !TextUtils.isEmpty(s))
                .concatMap(ResourceManager::getJavascriptContent)
                .filter(s -> !TextUtils.isEmpty(s))
                .map(jsContent -> String.format(jsContent, pJsInput))
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(finalWebView::runScript,
                        throwable -> Timber.w(throwable, "Exception load js file"));
        CompositeSubscription compositeSubscription = workFlow.mCompositeSubscription;
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }


    @JavascriptInterface
    public void logDebug(String msg) {
        Timber.d("Debug webview: %s", msg);
    }

    public void dispose() {
        Timber.d("dispose AbstractWebViewClient");
        mWorkFlow = null;
        if (mWebView != null && mWebView.get() != null) {
            mWebView.get().removeJavascriptInterface(JAVA_SCRIPT_INTERFACE_NAME);
            mWebView.get().setWebViewClient(null);
            mWebView.get().removeAllViews();
            mWebView.get().clearHistory();
            mWebView.get().freeMemory();
            mWebView.get().destroy();
            mWebView = null;
        }
    }

    public abstract void start(String pUrl);

    public abstract void stop();

    public abstract void hit();
}
