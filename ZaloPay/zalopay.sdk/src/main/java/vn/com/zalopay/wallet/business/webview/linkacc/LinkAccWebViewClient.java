package vn.com.zalopay.wallet.business.webview.linkacc;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.WebViewError;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptInput;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptOutput;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.helper.WebViewHelper;
import vn.com.zalopay.wallet.utils.GsonUtils;

import static vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc.PAGE_VCB_CONFIRM_LINK;
import static vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc.VCB_REGISTER_COMPLETE_PAGE;
import static vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc.VCB_REGISTER_PAGE;
import static vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc.VCB_UNREGISTER_COMPLETE_PAGE;

/**
 * @author SinhTT
 */
public class LinkAccWebViewClient extends PaymentWebViewClient {
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    protected static final String HTTP_EXCEPTION = "http://sdk.jsexception";
    // value is used for
    // detect valid url
    // in the case
    // webview on
    // Android 2.3
    private boolean isRedirected = false;

    private AdapterLinkAcc mAdapter = null;
    private PaymentWebView mWebPaymentBridge = null;

    private List<DBankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mCurrentUrl = null;

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;
    private boolean mIsRefreshCaptcha = false;

    private String mLastAutoScriptFile = null;
    private String mLastAutoScriptInput = null;
    /***
     * listener for WebChromeClient
     */
    private WebChromeClient wcClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(final WebView view, int newProgress) {
            Log.i("load in progress...", String.valueOf(newProgress) + "%");
            mAdapter.onEvent(EEventType.ON_PROGRESSING, newProgress);
        }
    };

    public LinkAccWebViewClient(AdapterBase pAdapter) {
        super(pAdapter);
        if (pAdapter != null) {
            mAdapter = (AdapterLinkAcc) pAdapter;
            mWebPaymentBridge = new PaymentWebView(GlobalData.getAppContext());
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.setWebChromeClient(wcClient);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
            setPaymentWebView(mWebPaymentBridge);
        }
    }


    public LinkAccWebViewClient(AdapterBase pAdapter, PaymentWebView pWeb) {
        super(pAdapter);
        if (pAdapter != null) {
            mAdapter = (AdapterLinkAcc) pAdapter;
            mWebPaymentBridge = pWeb;
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.setWebChromeClient(wcClient);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
            setPaymentWebView(mWebPaymentBridge);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i("onPageStarted: ", url);
        if (!isRedirected) {
            // code somethings if you want when starts
        }

        isRedirected = false;

    }

    protected boolean shouldOnCheckMatchOnLoadResouce(String pUrl) {
        return pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_generate_captcha))
                || pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_register_complete))
                || pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_unregister_complete));
    }

    protected boolean shouldExecuteJs() {
        return !TextUtils.isEmpty(mPageCode) && (mPageCode.equals(VCB_REGISTER_COMPLETE_PAGE) || mPageCode.equals(VCB_UNREGISTER_COMPLETE_PAGE));
    }

    protected boolean shouldRequestReadOtpPermission() {
        return !TextUtils.isEmpty(mPageCode) && (mPageCode.equals(PAGE_VCB_CONFIRM_LINK));
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d("onLoadResource", url);
        if (shouldOnCheckMatchOnLoadResouce(url)) {
            onPageFinishedAuto(url);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("shouldOverrideUrlLoading", url);
        view.loadUrl(url);
        isRedirected = true;
        return true;
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
        Log.d("load page finish ", url);
        if (!isRedirected) {
            Log.d("load page finish on the first", url);
            if (GlobalData.shouldNativeWebFlow() && url.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_auto_select_service))) {
                DLinkAccScriptInput input = genJsInput();
                String inputScript = GsonUtils.toJsonString(input);
                executeJs(Constants.AUTO_SELECT_SERVICE_JS, inputScript); // auto select service #a href tag
            } else {
                onPageFinishedAuto(url);
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Log.e("Error", "++++ Current error SSL on page: " + error.toString());
        getAdapter().getActivity().showProgress(false, null);
        getAdapter().getActivity().showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                mAdapter.onEvent(EEventType.ON_FAIL);
                mAdapter.getActivity().onBackPressed();
                try {
                    getAdapter().sdkReportError(SDKReportTask.ERROR_WEBSITE, error.toString());
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }

            @Override
            public void onOKevent() {
                handler.proceed(); // Ignore SSL certificate errors
            }
        }, mAdapter.getActivity().getString(R.string.zpw_alert_ssl_error_parse_website), mAdapter.getActivity().getString(R.string.dialog_continue_button), mAdapter.getActivity().getString(R.string.dialog_close_button));
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.d(getClass().getCanonicalName(), "errorCode=" + errorCode + ",description=" + description + ",failingUrl=" + failingUrl);
        if (failingUrl.contains(HTTP_EXCEPTION)) {
            Log.d(this, "skip process fail on url " + failingUrl);
            return;
        }
        if (WebViewHelper.isLoadSiteError(description) && getAdapter() != null) {
            getAdapter().onEvent(EEventType.ON_LOADSITE_ERROR, new WebViewError(errorCode, description));
        }
        if (getAdapter() != null) {
            StringBuilder errStringBuilder = new StringBuilder();
            errStringBuilder.append(description);
            errStringBuilder.append("\n");
            errStringBuilder.append(failingUrl);
            try {
                getAdapter().sdkReportError(SDKReportTask.ERROR_WEBSITE, errStringBuilder.toString());
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        getAdapter().getActivity().showProgress(false, null);
    }

    /***
     * //     * retry WebView
     * //     *
     * //     * @param view
     * //     * @param pUrl
     * //
     */
//    public void retry(WebView view, String pUrl) {
//        Log.i("///// onRetry: ", "RETRYING");
//        view.stopLoading();
//        view.clearView();
//        view.loadUrl(pUrl);
//    }
    public void start(String pUrl) {
        // new url load.
        mWebPaymentBridge.loadUrl(pUrl);
        mIsFirst = true;
    }

    public void reloadWebView(String pUrl) {
        // new url load.
        mWebPaymentBridge.loadUrl(pUrl);
    }

    /***
     * retry run again last script auto to
     * get number phone list after timeout
     */
    public void runLastScript() {
        if (mLastAutoScriptFile != null) {
            executeJs(mLastAutoScriptFile, mLastAutoScriptInput);
            Log.d(this, "run script file " + mLastAutoScriptFile + " input " + mLastAutoScriptInput);
        }
    }

    public void stop() {
        mWebPaymentBridge.stopLoading();
    }


    public void refreshCaptcha() {
        // new url load.
        mIsRefreshCaptcha = true;
        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
    }

    public void reload() {
        mWebPaymentBridge.reload();
    }

    public void hit() {
        mAdapter.onEvent(EEventType.ON_HIT);
        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
    }

    public DLinkAccScriptInput genJsInput() {
        DLinkAccScriptInput input = new DLinkAccScriptInput();

        if (getAdapter() != null) {
            input.username = mAdapter.getUserNameValue();
            input.password = mAdapter.getPasswordValue();
            input.captchaLogin = mAdapter.getCaptchaLogin();
            input.walletType = mAdapter.getWalletTypeValue();
            input.accNum = mAdapter.getAccNumValue();
            input.phoneNum = mAdapter.getPhoneNumValue();
            input.otpValidType = mAdapter.getOTPValidValue();
            input.captchaConfirm = mAdapter.getCaptchaConfirm();
            input.otp = mAdapter.getOTPValue();
            input.linkerType = (GlobalData.isLinkAccFlow() ? 1 : 0); // 1. isLink, other. isUnLink
            input.walletTypeUnregister = mAdapter.getWalletTypeUnRegValue();
            input.phoneNumUnregister = mAdapter.getPhoneNumUnRegValue();
            input.passwordUnregister = mAdapter.getPasswordUnRegValue();
            return input;
        }

        return null;
    }

    public void matchAndRunJs(String url, EJavaScriptType pType, boolean pIsAjax) {

        boolean isMatched = false;
        for (DBankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && url.matches(bankScript.url) && !mIsRefreshCaptcha) {
                mCurrentUrl = url;
                Log.d("matchAndRunJs", "url: " + url + " ,type: " + pType);
                isMatched = true;
                if (bankScript.pageCode.equals(VCB_REGISTER_PAGE)) {
                    mAdapter.mUrlReload = url;
                }
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;

                if (GlobalData.shouldNativeWebFlow() && shouldRequestReadOtpPermission()) {
                    getAdapter().requestReadOtpPermission();
                }

                if (GlobalData.shouldNativeWebFlow() && !shouldExecuteJs()) { //prevent load js on web flow
                    return;
                }

                DLinkAccScriptInput input = genJsInput();
                input.isAjax = pIsAjax;
                String inputScript = GsonUtils.toJsonString(input);

                if (pType == EJavaScriptType.AUTO) {
                    mLastAutoScriptFile = bankScript.autoJs;
                    mLastAutoScriptInput = inputScript;
                    executeJs(bankScript.autoJs, inputScript);
                }

                if (pType == EJavaScriptType.HIT) {
                    executeJs(bankScript.hitJs, inputScript);
                }

                // break loop for
                break;
            } else if (mIsRefreshCaptcha && bankScript.pageCode.equals(AdapterLinkAcc.VCB_REFRESH_CAPTCHA)) {
                Log.d("matchAndRunJs", "url: " + url + " ,type: " + pType);
                DLinkAccScriptInput input = genJsInput();
                input.isAjax = pIsAjax;
                String inputScript = GsonUtils.toJsonString(input);
                if (pType == EJavaScriptType.HIT) {
                    executeJs(bankScript.hitJs, inputScript);
                }
                // break loop for
                mIsRefreshCaptcha = false;
                break;
            }
        }

        if (!isMatched) {
            mAdapter.onEvent(EEventType.ON_FAIL);
        }

    }

    public void fillOtpOnWebFlow(String pOtp) {
        DLinkAccScriptInput input = genJsInput();
        input.otp = pOtp;
        String inputScript = GsonUtils.toJsonString(input);
        executeJs(Constants.AUTOFILL_OTP_WEBFLOW_JS, inputScript);
    }

    /***
     * run normal. not ajax
     *
     * @param url
     */
    public void onPageFinishedAuto(String url) {
        matchAndRunJs(url, EJavaScriptType.AUTO, false);
    }

    /***
     * @param pResult
     */
    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        Log.d("Js", "==== onJsPaymentResult: " + pResult);

        final String result = pResult;

        getAdapter().getActivity().runOnUiThread(() -> {
            DLinkAccScriptOutput scriptOutput = GsonUtils.fromJsonString(result, DLinkAccScriptOutput.class);
            EEventType eventType = convertPageIdToEvent(mEventID);
            StatusResponse response = genResponse(eventType, scriptOutput);
            Log.d("Js", "==== onJsPaymentResult: " + mEventID + "==" + pResult);
            if (mEventID == 0 && mIsFirst && !scriptOutput.isError()) {
                // Auto hit at first step
                mIsFirst = false;
                hit();
            } else {
                if (eventType == EEventType.ON_REQUIRE_RENDER) {
                    mAdapter.onEvent(EEventType.ON_REQUIRE_RENDER, scriptOutput, mPageCode);
                } else {
                    mAdapter.onEvent(eventType, response, mPageCode, mEventID);
                }
            }
        });
    }

    public EEventType convertPageIdToEvent(int pEventID) {
        switch (pEventID) {
            case -1:
                return EEventType.ON_FAIL;
            case 0: // AUTO HIT at first time
            case 1: // Verify card complete
            case 2:
                return EEventType.ON_REQUIRE_RENDER;
            default:
                return EEventType.ON_REQUIRE_RENDER;
        }
    }

    public StatusResponse genResponse(EEventType pEventType, DLinkAccScriptOutput pScriptOutput) {
        StatusResponse ret;
        switch (pEventType) {
            default:
                ret = new StatusResponse();
                ret.returnmessage = pScriptOutput.message;
                break;
        }

        if (ret != null) {
            if (!pScriptOutput.isError()) {
                ret.returncode = 4;
            } else {
                ret.returncode = -4;
            }
        }

        return ret;
    }
}
