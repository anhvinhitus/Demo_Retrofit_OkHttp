package vn.com.zalopay.wallet.workflow.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkWebsite3dsBackEvent;
import vn.com.zalopay.wallet.event.SdkWebsite3dsEvent;
import vn.com.zalopay.wallet.helper.WebViewHelper;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;
import static vn.com.zalopay.wallet.helper.WebViewHelper.SSL_ERROR;

public class CCWebViewClient extends AbstractWebViewClient {
    private boolean isFirstLoad = true;
    private String mMerchantPrefix = "";

    public CCWebViewClient(AbstractWorkFlow pAdapter, SdkWebView webView) {
        initialize(pAdapter, webView);
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
        SdkWebView webView = null;
        try {
            webView = getWebView();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (webView != null) {
            webView.stopLoading();
        }
    }

    private boolean shouldStopFlow(String url) {
        return TextUtils.isEmpty(url)
                || url.contains(mMerchantPrefix)
                || url.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain));
    }

    private boolean shouldOpenBrowser(String url) {
        return TextUtils.isEmpty(url)
                || url.contains(GlobalData.getStringResource(RS.string.sdk_bidv_bankscript_term_of_use))
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
        AbstractWorkFlow workFlow = null;
        try {
            workFlow = getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            Timber.w("workFlow is release on loading website");
            return true;
        }

        if (shouldStopFlow(url)) {
            SDKApplication
                    .getApplicationComponent()
                    .eventBus()
                    .postSticky(new SdkWebsite3dsEvent());
            return true;
        }

        if (shouldOpenBrowser(url)) {
            try {
                SdkUtils.openWebPage(getWorkFlow().getActivity(), url);
            } catch (Exception e) {
                Timber.d(e);
            }
            return true;
        }
        workFlow.showTimeoutLoading(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_load_website3ds_mess));
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Timber.d("load resource %s", url);
        AbstractWorkFlow workFlow = null;
        try {
            workFlow = getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            return;
        }
        if (!isFirstLoad && url != null
                && url.contains(GlobalData.getStringResource(RS.string.sdk_website123pay_domain))) {
            workFlow.showTimeoutLoading(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_load_website3ds_mess));
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Timber.d("load page finish %s", url);
        try {
            getWorkFlow().getView().hideLoading();
        } catch (Exception e) {
            Timber.d(e);
        }
        isFirstLoad = false;
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (WebViewHelper.isLoadSiteError(description)) {
            SDKApplication
                    .getApplicationComponent()
                    .eventBus()
                    .postSticky(new SdkWebsite3dsBackEvent(new WebViewHelper(errorCode, description)));
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
        StringBuffer errStringBuilder = new StringBuffer();
        errStringBuilder.append(description);
        errStringBuilder.append(failingUrl);
        SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
        reporter.sdkReportError(workFlow, ERROR_WEBSITE, errStringBuilder.toString());
        Timber.d("errorCode = %s description = %s failingUrl = %s", errorCode, description, failingUrl);
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        SDKApplication
                .getApplicationComponent()
                .eventBus()
                .postSticky(new SdkWebsite3dsBackEvent(new WebViewHelper(SSL_ERROR, null)));
        new Handler().postDelayed(() -> {
            try {
                SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                reporter.sdkReportError(getWorkFlow(), ERROR_WEBSITE, GsonUtils.toJsonString(error));
            } catch (Exception e) {
                Timber.d(e);
            }
        }, 500);
        Timber.w("there're error ssl on page %s", GsonUtils.toJsonString(error));
    }
}
