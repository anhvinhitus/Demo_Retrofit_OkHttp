package vn.com.zalopay.wallet.business.webview.creditcard;

import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.WebViewError;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.helper.WebViewHelper;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.data.Log;

import static vn.com.zalopay.wallet.business.entity.base.WebViewError.SSL_ERROR;

public class CCWebViewClient extends PaymentWebViewClient {
    protected boolean isFirstLoad = true;
    private String mMerchantPrefix;

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
            getAdapter().showProgressBar(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_transition_screen));
        }
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d(this, "load resource " + url);
        if (!isFirstLoad && url != null && url.contains(GlobalData.getStringResource(RS.string.zpw_string_pay_domain)) && getAdapter() != null) {
            getAdapter().showProgressBar(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_transition_screen));
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(this, "load page finish " + url);
        if (getAdapter() != null) {
            getAdapter().showProgressBar(false, null);
        }
        isFirstLoad = false;
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (WebViewHelper.isLoadSiteError(description) && getAdapter() != null) {
            getAdapter().onEvent(EEventType.ON_LOADSITE_ERROR, new WebViewError(errorCode, description));
        }
        if (getAdapter() != null) {
            StringBuilder errStringBuilder = new StringBuilder();
            errStringBuilder.append(description);
            errStringBuilder.append(failingUrl);
            try {
                getAdapter().sdkReportError(SDKReportTask.ERROR_WEBSITE, errStringBuilder.toString());
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
                getAdapter().sdkReportError(SDKReportTask.ERROR_SSL, GsonUtils.toJsonString(error));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        Log.d(this, "there're error ssl on page", error);
    }

}
