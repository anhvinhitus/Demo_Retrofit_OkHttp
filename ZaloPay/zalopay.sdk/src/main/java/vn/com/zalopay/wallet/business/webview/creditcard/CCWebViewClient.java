package vn.com.zalopay.wallet.business.webview.creditcard;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.helper.WebViewHelper;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkWebsite3dsBackEvent;
import vn.com.zalopay.wallet.event.SdkWebsite3dsEvent;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;
import static vn.com.zalopay.wallet.helper.WebViewHelper.SSL_ERROR;

public class CCWebViewClient extends PaymentWebViewClient {
    protected boolean isFirstLoad = true;

    private String mMerchantPrefix = "";
    private WebView mWebView = null;


    public CCWebViewClient(AbstractWorkFlow pAdapter) {
        super(pAdapter);
        this.mMerchantPrefix = GlobalData.getStringResource(RS.string.sdk_website3ds_callback_url);
    }

    @Override
    public void start(String pUrl) {

    }

    @Override
    public void hit() {

    }

    @Override
    public void stop() {
        if (mWebView != null) {
            mWebView.stopLoading();
        }
    }

    private boolean shouldStopFlow(String url) {
        return TextUtils.isEmpty(url) || url.contains(mMerchantPrefix) || url.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain));
    }

    private boolean shouldOpenBrowser(String url) {
        return TextUtils.isEmpty(url) || url.contains(GlobalData.getStringResource(RS.string.sdk_bidv_bankscript_term_of_use))
                || url.contains(GlobalData.getStringResource(RS.string.sdk_bidv_bankscript_dknhdt));
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Timber.d("onPageStarted %s ", url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading %s ", url);
        if (getAdapter() == null) {
            Timber.w("Adapter is release on loading webwsite");
            return true;
        }

        if (shouldStopFlow(url)) {
            SDKApplication.getApplicationComponent().eventBus().postSticky(new SdkWebsite3dsEvent());
            return true;
        }

        if (shouldOpenBrowser(url)) {
            try {
                SdkUtils.openWebPage(getAdapter().getActivity(), url);
            } catch (Exception e) {
                Timber.w(e);
            }
            return true;
        }
        getAdapter().showTimeoutLoading(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_load_website3ds_mess));
        view.loadUrl(url);
        mWebView = view;
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Timber.d("load resource %s", url);
        if (!isFirstLoad && url != null && url.contains(GlobalData.getStringResource(RS.string.sdk_website123pay_domain)) && getAdapter() != null) {
            getAdapter().showTimeoutLoading(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_load_website3ds_mess));
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Timber.d("load page finish %s", url);
        if (getAdapter() != null) {
            try {
                getAdapter().getView().hideLoading();
            } catch (Exception ignored) {
            }
            // BIDVWebFlow(null, url, view);
        }
        isFirstLoad = false;
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (WebViewHelper.isLoadSiteError(description)) {
            SDKApplication.getApplicationComponent().eventBus()
                    .postSticky(new SdkWebsite3dsBackEvent(new WebViewHelper(errorCode, description)));
        }
        if (getAdapter() != null) {
            StringBuffer errStringBuilder = new StringBuffer();
            errStringBuilder.append(description);
            errStringBuilder.append(failingUrl);
            SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
            reporter.sdkReportError(getAdapter(), ERROR_WEBSITE, errStringBuilder.toString());
        }
        Timber.d("errorCode=" + errorCode + ",description=" + description + ",failingUrl=" + failingUrl);
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        SDKApplication.getApplicationComponent().eventBus()
                .postSticky(new SdkWebsite3dsBackEvent(new WebViewHelper(SSL_ERROR, null)));
        new Handler().postDelayed(() -> {
            try {
                SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                reporter.sdkReportError(getAdapter(), ERROR_WEBSITE, GsonUtils.toJsonString(error));
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }, 500);
        Timber.w("there're error ssl on page %s", GsonUtils.toJsonString(error));
    }

    public void BIDVWebFlow(String pOtp, String pUrl, WebView pView) {
        if (pUrl.matches(GlobalData.getStringResource(RS.string.sdk_bidv_bankscript_auto_select_rule))) {
            executeJs(Constants.AUTOCHECK_RULE_FILLOTP_BIDV_JS, pOtp, pView);
            //request permission read/view sms on android 6.0+
            if (isFirstLoad) {
                getAdapter().requestReadOtpPermission();
            }
        }

    }

    public void BIDVWebFlowFillOtp(String pOtp) {
        if (mWebView != null) {
            executeJs(Constants.AUTOCHECK_RULE_FILLOTP_BIDV_JS, pOtp, mWebView);
        }

    }

    public void executeJs(String pJsFileName, String pJsInput, WebView pView) {
        if (TextUtils.isEmpty(pJsFileName)) {
            return;
        }
        Timber.d("file name %s input %s", pJsFileName, pJsInput);
        Subscription subscription = Observable.from(pJsFileName.split(Constants.COMMA))
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(ResourceManager::getJavascriptContent)
                .filter(s -> !TextUtils.isEmpty(s))
                .map(jsContent -> String.format(jsContent, pJsInput))
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(jsContent -> runScript(jsContent, pView),
                        throwable -> Timber.w(throwable, "Exception load js file"));
        CompositeSubscription compositeSubscription = getAdapter() != null ? getAdapter().mCompositeSubscription : null;
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void runScript(String scriptContent, WebView pView) {
        Timber.d("runScript: " + scriptContent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pView.evaluateJavascript(scriptContent, null);
        } else {
            pView.loadUrl("javascript:{" + scriptContent + "}");
        }
    }
}
