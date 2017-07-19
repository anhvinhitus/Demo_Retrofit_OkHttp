package vn.com.zalopay.wallet.business.webview.creditcard;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.WebViewHelper;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;
import static vn.com.zalopay.wallet.business.entity.base.WebViewHelper.SSL_ERROR;

public class CCWebViewClient extends PaymentWebViewClient {
    protected boolean isFirstLoad = true;

    private String mMerchantPrefix = "";
    private WebView mWebView = null;


    public CCWebViewClient(AdapterBase pAdapter) {
        super(pAdapter);
        this.mMerchantPrefix = GlobalData.getStringResource(RS.string.sdk_website3ds_callback_url);
    }

    @Override
    public void start(String pUrl) {

    }

    @Override
    public void hit() {

    }

    private boolean shouldStopFlow(String url) {
        return TextUtils.isEmpty(url) || url.contains(mMerchantPrefix) || url.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain));
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading %s ", url);
        if (getAdapter() == null) {
            Timber.w("Adapter is release on loading webwsite");
            return true;
        }
        if (shouldStopFlow(url)) {
            getAdapter().onEvent(EEventType.ON_PAYMENT_RESULT_BROWSER, new Object());
            return true;
        }
        getAdapter().showLoadindTimeout(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_load_website3ds_mess));
        view.loadUrl(url);
        mWebView = view;
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Timber.d("load resource %s", url);
        if (!isFirstLoad && url != null && url.contains(GlobalData.getStringResource(RS.string.sdk_website123pay_domain)) && getAdapter() != null) {
            getAdapter().showLoadindTimeout(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_load_website3ds_mess));
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Timber.d("load page finish %s", url);
        if (getAdapter() != null) {
            try {
                getAdapter().getView().hideLoading();
            } catch (Exception e) {
                Log.e(this, e);
            }
            BIDVWebFlow(null, url, view);
        }
        isFirstLoad = false;
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (WebViewHelper.isLoadSiteError(description) && getAdapter() != null) {
            getAdapter().onEvent(EEventType.ON_LOADSITE_ERROR, new WebViewHelper(errorCode, description));
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
        if (getAdapter() != null) {
            getAdapter().onEvent(EEventType.ON_LOADSITE_ERROR, new WebViewHelper(SSL_ERROR, null));
            new Handler().postDelayed(() -> {
                try {
                    SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                    reporter.sdkReportError(getAdapter(), ERROR_WEBSITE, GsonUtils.toJsonString(error));
                } catch (Exception e) {
                    Timber.w(e.getMessage());
                }
            }, 500);
        }
        Log.d(this, "there're error ssl on page", error);
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
        if (!TextUtils.isEmpty(pJsFileName)) {
            String jsContent;
            Timber.d(pJsFileName);
            Timber.d(pJsInput);
            for (String jsFile : pJsFileName.split(Constants.COMMA)) {
                jsContent = ResourceManager.getJavascriptContent(jsFile);
                jsContent = String.format(jsContent, pJsInput);
                runScript(jsContent, pView);
            }
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
