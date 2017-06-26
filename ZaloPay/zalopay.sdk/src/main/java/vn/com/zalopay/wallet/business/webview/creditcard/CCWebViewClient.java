package vn.com.zalopay.wallet.business.webview.creditcard;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.WebViewError;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.helper.WebViewHelper;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_SSL;
import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;
import static vn.com.zalopay.wallet.business.entity.base.WebViewError.SSL_ERROR;

public class CCWebViewClient extends PaymentWebViewClient {
    protected boolean isFirstLoad = true;

    private String mMerchantPrefix = "";
    private WebView mWebView = null;


    public CCWebViewClient(AdapterBase pAdapter) {
        super(pAdapter);
        this.mMerchantPrefix = GlobalData.getStringResource(RS.string.zpw_string_merchant_creditcard_3ds_url_prefix);
    }

    @Override
    public void start(String pUrl) {

    }

    @Override
    public void hit() {

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d(this, "load url " + url);
        if ((url.contains(mMerchantPrefix) || url.contains(BuildConfig.HOST_COMPLETE)) && getAdapter() != null) {
            getAdapter().onEvent(EEventType.ON_PAYMENT_RESULT_BROWSER, new Object());
            return true;
        }
        if (getAdapter() != null) {
            try {
                getAdapter().getView().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_transition_screen));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        view.loadUrl(url);
        mWebView = view;
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d(this, "load resource " + url);
        if (!isFirstLoad && url != null && url.contains(GlobalData.getStringResource(RS.string.zpw_string_pay_domain)) && getAdapter() != null) {
            try {
                getAdapter().getView().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_transition_screen));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(this, "load page finish " + url);
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
            getAdapter().onEvent(EEventType.ON_LOADSITE_ERROR, new WebViewError(errorCode, description));
        }
        if (getAdapter() != null) {
            StringBuffer errStringBuilder = new StringBuffer();
            errStringBuilder.append(description);
            errStringBuilder.append(failingUrl);
            try {
                getAdapter().sdkReportError(ERROR_WEBSITE, errStringBuilder.toString());
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        Log.d(this, "errorCode=" + errorCode + ",description=" + description + ",failingUrl=" + failingUrl);
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (getAdapter() != null) {
            getAdapter().onEvent(EEventType.ON_LOADSITE_ERROR, new WebViewError(SSL_ERROR, null));
            try {
                getAdapter().sdkReportError(ERROR_SSL, GsonUtils.toJsonString(error));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        Log.d(this, "there're error ssl on page", error);
    }

    public void BIDVWebFlow(String pOtp, String pUrl, WebView pView) {
        if (pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_bidv_auto_select_rule))) {
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
            Log.d("executeJs", pJsFileName);
            Log.d("executeJs", pJsInput);
            for (String jsFile : pJsFileName.split(Constants.COMMA)) {
                jsContent = ResourceManager.getJavascriptContent(jsFile);
                jsContent = String.format(jsContent, pJsInput);
                runScript(jsContent, pView);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void runScript(String scriptContent, WebView pView) {
        Log.d(this, "runScript: " + scriptContent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pView.evaluateJavascript(scriptContent, null);
        } else {
            pView.loadUrl("javascript:{" + scriptContent + "}");
        }
    }
}
