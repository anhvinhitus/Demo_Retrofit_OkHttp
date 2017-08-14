package vn.com.zalopay.wallet.business.webview.linkacc;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.WebViewHelper;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptInput;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptOutput;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.AccountLinkWorkFlow;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_VCB_CONFIRM_LINK;
import static vn.com.zalopay.wallet.constants.Constants.VCB_REFRESH_CAPTCHA;
import static vn.com.zalopay.wallet.constants.Constants.VCB_REGISTER_COMPLETE_PAGE;
import static vn.com.zalopay.wallet.constants.Constants.VCB_REGISTER_PAGE;
import static vn.com.zalopay.wallet.constants.Constants.VCB_UNREGISTER_COMPLETE_PAGE;

/**
 * @author SinhTT
 */
public class LinkAccWebViewClient extends PaymentWebViewClient {
    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    protected static final String HTTP_EXCEPTION = "http://sdk.jsexception";
    AccountLinkWorkFlow mAdapter = null;
    // value is used for
    // detect valid url
    // in the case
    // webview on
    // Android 2.3
    private boolean isRedirected = false;
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
            Timber.d("load in progress... %s", String.valueOf(newProgress) + "%");
            mAdapter.onEvent(EEventType.ON_PROGRESSING, newProgress);
        }
    };

    public LinkAccWebViewClient(AbstractWorkFlow pAdapter) {
        super(pAdapter);
        if (pAdapter != null) {
            mAdapter = (AccountLinkWorkFlow) pAdapter;
            mWebPaymentBridge = new PaymentWebView(GlobalData.getAppContext());
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.setWebChromeClient(wcClient);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        }
    }


    public LinkAccWebViewClient(AbstractWorkFlow pAdapter, PaymentWebView pWeb) {
        super(pAdapter);
        if (pAdapter != null) {
            mAdapter = (AccountLinkWorkFlow) pAdapter;
            mWebPaymentBridge = pWeb;
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.setWebChromeClient(wcClient);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        isRedirected = false;

    }

    protected boolean shouldOnCheckMatchOnLoadResouce(String pUrl) {
        return pUrl.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_generate_captcha))
                || pUrl.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_register_complete))
                || pUrl.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_unregister_complete));
    }

    protected boolean shouldExecuteJs() {
        return !TextUtils.isEmpty(mPageCode) && (mPageCode.equals(VCB_REGISTER_COMPLETE_PAGE) || mPageCode.equals(VCB_UNREGISTER_COMPLETE_PAGE));
    }

    protected boolean shouldRequestReadOtpPermission() {
        return !TextUtils.isEmpty(mPageCode) && (mPageCode.equals(PAGE_VCB_CONFIRM_LINK));
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Timber.d(url);
        if (shouldOnCheckMatchOnLoadResouce(url)) {
            onPageFinishedAuto(url);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading: %s", url);
        view.loadUrl(url);
        isRedirected = true;
        return true;
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
        Timber.d("load page finish %s", url);
        if (!isRedirected) {
            Timber.d("load page finish on the first %s", url);
            if (PaymentPermission.allowVCBNativeFlow() && url.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_auto_select_service))) {
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
        try {
            if (getAdapter() == null) {
                return;
            }
            ChannelFragment channelFragment = getAdapter().getView();
            if (channelFragment == null) {
                return;
            }
            channelFragment.hideLoading();
            channelFragment.showConfirmDialog(
                    GlobalData.getAppContext().getString(R.string.sdk_parsewebsite_sslerror_mess),
                    GlobalData.getAppContext().getString(R.string.dialog_continue_button),
                    GlobalData.getAppContext().getString(R.string.dialog_close_button),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                            try {
                                mAdapter.onEvent(EEventType.ON_FAIL);
                                mAdapter.getActivity().onBackPressed();
                                SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                                reporter.sdkReportError(getAdapter(), ERROR_WEBSITE, error.toString());
                            } catch (Exception ignored) {
                            }
                        }

                        @Override
                        public void onOKEvent() {
                            handler.proceed(); // Ignore SSL certificate errors
                        }
                    });
        } catch (Exception e) {
            Timber.d(e, "Exception onReceivedSslError");
        }
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Timber.d("errorCode=" + errorCode + ",description=" + description + ",failingUrl=" + failingUrl);
        if (failingUrl.contains(HTTP_EXCEPTION)) {
            Timber.d("skip process fail on url " + failingUrl);
            return;
        }
        if (WebViewHelper.isLoadSiteError(description) && getAdapter() != null) {
            SDKApplication.getApplicationComponent()
                    .eventBus().postSticky(new WebViewHelper(errorCode, description));
        }
        if (getAdapter() != null) {
            StringBuffer errStringBuilder = new StringBuffer();
            errStringBuilder.append(description);
            errStringBuilder.append("\n");
            errStringBuilder.append(failingUrl);
            SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
            reporter.sdkReportError(getAdapter(), ERROR_WEBSITE, errStringBuilder.toString());
        }
        try {
            getAdapter().getView().hideLoading();
        } catch (Exception e) {
            Timber.w(e);
        }
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
            Timber.d("run script file " + mLastAutoScriptFile + " input " + mLastAutoScriptInput);
        }
    }

    @Override
    public void stop() {
        if (mWebPaymentBridge != null) {
            mWebPaymentBridge.stopLoading();
        }
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
            input.linkerType = mAdapter.getPaymentInfoHelper().bankAccountLink() ? 1 : 0; // 1. isLink, other. isUnLink
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
                Timber.d("url: " + url + " ,type: " + pType);
                isMatched = true;
                if (bankScript.pageCode.equals(VCB_REGISTER_PAGE)) {
                    mAdapter.mUrlReload = url;
                }
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;

                if (PaymentPermission.allowVCBNativeFlow() && shouldRequestReadOtpPermission()) {
                    getAdapter().requestReadOtpPermission();
                }

                if (PaymentPermission.allowVCBNativeFlow() && !shouldExecuteJs()) { //prevent load js on web flow
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
            } else if (mIsRefreshCaptcha && bankScript.pageCode.equals(VCB_REFRESH_CAPTCHA)) {
                Timber.d("url: " + url + " ,type: " + pType);
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

    public void executeJs(String pJsFileName, String pJsInput) {
        if (TextUtils.isEmpty(pJsFileName)) {
            return;
        }
        Timber.d("file name %s input %s", pJsFileName, pJsInput);
        Subscription subscription = Observable.from(pJsFileName.split(Constants.COMMA))
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(ResourceManager::getJavascriptContent)
                .filter(s -> !TextUtils.isEmpty(s))
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(jsContent -> {
                    String content = String.format(jsContent, pJsInput);
                    if (mWebPaymentBridge != null) {
                        mWebPaymentBridge.runScript(content);
                    }
                }, throwable -> Timber.w(throwable, "Exception load js file"));
        CompositeSubscription compositeSubscription = getAdapter() != null ? getAdapter().mCompositeSubscription : null;
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }

    @JavascriptInterface
    public void logDebug(String msg) {
        Timber.d("****** Debug webview: " + msg);
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
     * run when load ajax
     *
     * @param pUrl
     */
    public void onPageFinishedAjax(final String pUrl, long pDelay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                matchAndRunJs(pUrl, EJavaScriptType.AUTO, true);
            }
        }, pDelay); // delay 1s for Ajax run
    }

    /***
     * @param pResult
     */
    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        Timber.d("onJsPaymentResult %s", pResult);
        final String result = pResult;
        try {
            getAdapter().getActivity().runOnUiThread(() -> {
                DLinkAccScriptOutput scriptOutput = GsonUtils.fromJsonString(result, DLinkAccScriptOutput.class);
                EEventType eventType = convertPageIdToEvent(mEventID);
                StatusResponse response = getResponse(eventType, scriptOutput);
                if (mEventID == 0 && mIsFirst && scriptOutput != null && !scriptOutput.isError()) {
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
        } catch (Exception e) {
        }
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

    public boolean isVerifyCardComplete() {
        return mEventID == 1;
    }

    public StatusResponse getResponse(EEventType pEventType, DLinkAccScriptOutput pScriptOutput) {
        StatusResponse ret = new StatusResponse();
        ret.returnmessage = pScriptOutput.message;

        if (!pScriptOutput.isError()) {
            ret.returncode = 4;
        } else {
            ret.returncode = -4;
        }

        return ret;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    public void dispose() {
        // When clearing the webview reference. Simply set it to null is not
        // enough.
        // http://garena.github.io/blog/2014/07/18/android-prevent-webview-from-memory-leak/
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
        }
    }
}
