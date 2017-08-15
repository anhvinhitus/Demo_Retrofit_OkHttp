package vn.com.zalopay.wallet.workflow.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.entity.bank.BankScript;
import vn.com.zalopay.wallet.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.entity.linkacc.LinkAccScriptInput;
import vn.com.zalopay.wallet.entity.linkacc.LinkAccScriptOutput;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.helper.WebViewHelper;
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
public class AccountLinkWebViewClient extends AbstractWebViewClient {
    private static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    private static final String HTTP_EXCEPTION = "http://sdk.jsexception";
    private boolean isRedirected = false;

    private List<BankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mCurrentUrl = null;

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;
    private boolean mIsRefreshCaptcha = false;

    private String mLastAutoScriptFile = null;
    private String mLastAutoScriptInput = null;

    private WebChromeClient wcClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(final WebView view, int newProgress) {
            Timber.d("load in progress %s", String.valueOf(newProgress));
            try {
                ((AccountLinkWorkFlow) getWorkFlow()).onEvent(EEventType.ON_PROGRESSING, newProgress);
            } catch (Exception e) {
                Timber.d(e);
            }
        }
    };

    public AccountLinkWebViewClient(@NonNull AbstractWorkFlow workFlow) {
        SdkWebView webView = new SdkWebView(GlobalData.getAppContext());
        webView.setWebChromeClient(wcClient);
        webView.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        initialize(workFlow, webView);
    }


    public AccountLinkWebViewClient(@NonNull AbstractWorkFlow workFlow, SdkWebView pWebview) {
        pWebview.setWebChromeClient(wcClient);
        pWebview.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        initialize(workFlow, pWebview);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        isRedirected = false;

    }

    private boolean shouldOnCheckMatchOnLoadResouce(String pUrl) {
        return pUrl.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_generate_captcha))
                || pUrl.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_register_complete))
                || pUrl.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_unregister_complete));
    }

    private boolean shouldExecuteJs() {
        return !TextUtils.isEmpty(mPageCode)
                && (mPageCode.equals(VCB_REGISTER_COMPLETE_PAGE) || mPageCode.equals(VCB_UNREGISTER_COMPLETE_PAGE));
    }

    private boolean shouldRequestReadOtpPermission() {
        return !TextUtils.isEmpty(mPageCode) && (mPageCode.equals(PAGE_VCB_CONFIRM_LINK));
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Timber.d("onLoadResource %s", url);
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
        if (isRedirected) {
            return;
        }
        Timber.d("load page finish on the first %s", url);
        if (PaymentPermission.allowVCBNativeFlow()
                && url.matches(GlobalData.getStringResource(RS.string.sdk_vcb_bankscript_auto_select_service))) {
            LinkAccScriptInput input = genJsInput();
            String inputScript = GsonUtils.toJsonString(input);
            executeJs(Constants.AUTO_SELECT_SERVICE_JS, inputScript); // auto select service #a href tag
        } else {
            onPageFinishedAuto(url);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        try {
            AbstractWorkFlow workFlow = getWorkFlow();
            if (workFlow == null) {
                return;
            }
            ChannelFragment channelFragment = getWorkFlow().getView();
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
                                ((AccountLinkWorkFlow) workFlow).onEvent(EEventType.ON_FAIL);
                                workFlow.getActivity().onBackPressed();
                                SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                                reporter.sdkReportError(getWorkFlow(), ERROR_WEBSITE, error.toString());
                            } catch (Exception e) {
                                Timber.d(e);
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
        AccountLinkWorkFlow workFlow = null;
        try {
            workFlow = (AccountLinkWorkFlow) getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            return;
        }
        if (WebViewHelper.isLoadSiteError(description)) {
            SDKApplication.getApplicationComponent()
                    .eventBus().postSticky(new WebViewHelper(errorCode, description));
        }
        StringBuffer errStringBuilder = new StringBuffer();
        errStringBuilder.append(description);
        errStringBuilder.append("\n");
        errStringBuilder.append(failingUrl);
        SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
        reporter.sdkReportError(workFlow, ERROR_WEBSITE, errStringBuilder.toString());
        try {
            workFlow.getView().hideLoading();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void start(String pUrl) {
        try {
            SdkWebView webView = getWebView();
            if (webView != null) {
                webView.loadUrl(pUrl);
                mIsFirst = true;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void stop() {
        try {
            SdkWebView webView = getWebView();
            if (webView != null) {
                webView.stopLoading();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void hit() {
        try {
            ((AccountLinkWorkFlow) getWorkFlow()).onEvent(EEventType.ON_HIT);
            matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void reloadWebView(String pUrl) {
        try {
            SdkWebView webView = getWebView();
            if (webView != null) {
                webView.loadUrl(pUrl);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    /*
     * retry run again last script auto to
     * get number phone list after timeout
     */
    public void runLastScript() {
        if (mLastAutoScriptFile != null) {
            executeJs(mLastAutoScriptFile, mLastAutoScriptInput);
        }
    }

    public void refreshCaptcha() {
        mIsRefreshCaptcha = true;
        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
    }

    public void reload() {
        try {
            SdkWebView webView = getWebView();
            if (webView != null) {
                webView.reload();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @NonNull
    private LinkAccScriptInput genJsInput() {
        LinkAccScriptInput input = new LinkAccScriptInput();
        try {
            AccountLinkWorkFlow workFlow = (AccountLinkWorkFlow) getWorkFlow();
            if (workFlow != null) {
                input.username = workFlow.getUserNameValue();
                input.password = workFlow.getPasswordValue();
                input.captchaLogin = workFlow.getCaptchaLogin();
                input.walletType = workFlow.getWalletTypeValue();
                input.accNum = workFlow.getAccNumValue();
                input.phoneNum = workFlow.getPhoneNumValue();
                input.otpValidType = workFlow.getOTPValidValue();
                input.captchaConfirm = workFlow.getCaptchaConfirm();
                input.otp = workFlow.getOTPValue();
                input.linkerType = workFlow.getPaymentInfoHelper().bankAccountLink() ? 1 : 0; // 1. isLink, other. isUnLink
                input.walletTypeUnregister = workFlow.getWalletTypeUnRegValue();
                input.phoneNumUnregister = workFlow.getPhoneNumUnRegValue();
                input.passwordUnregister = workFlow.getPasswordUnRegValue();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return input;
    }

    private void matchAndRunJs(String url, EJavaScriptType pType, boolean pIsAjax) {
        boolean isMatched = false;
        AbstractWorkFlow workFlow = null;
        try {
            workFlow = getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            return;
        }
        for (BankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && url.matches(bankScript.url) && !mIsRefreshCaptcha) {
                mCurrentUrl = url;
                Timber.d("url: %s type %s", url, pType);
                isMatched = true;
                if (bankScript.pageCode.equals(VCB_REGISTER_PAGE)) {
                    ((AccountLinkWorkFlow) workFlow).mUrlReload = url;
                }
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;

                if (PaymentPermission.allowVCBNativeFlow() && shouldRequestReadOtpPermission()) {
                    workFlow.requestReadOtpPermission();
                }

                if (PaymentPermission.allowVCBNativeFlow() && !shouldExecuteJs()) { //prevent load js on web flow
                    return;
                }

                LinkAccScriptInput input = genJsInput();
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
                break;
            } else if (mIsRefreshCaptcha && bankScript.pageCode.equals(VCB_REFRESH_CAPTCHA)) {
                Timber.d("url: %s type %s", url, pType);
                LinkAccScriptInput input = genJsInput();
                input.isAjax = pIsAjax;
                String inputScript = GsonUtils.toJsonString(input);
                if (pType == EJavaScriptType.HIT) {
                    executeJs(bankScript.hitJs, inputScript);
                }
                mIsRefreshCaptcha = false;
                break;
            }
        }

        if (!isMatched) {
            ((AccountLinkWorkFlow) workFlow).onEvent(EEventType.ON_FAIL);
        }

    }

    public void fillOtpOnWebFlow(String pOtp) {
        LinkAccScriptInput input = genJsInput();
        input.otp = pOtp;
        String inputScript = GsonUtils.toJsonString(input);
        executeJs(Constants.AUTOFILL_OTP_WEBFLOW_JS, inputScript);
    }

    private void onPageFinishedAuto(String url) {
        matchAndRunJs(url, EJavaScriptType.AUTO, false);
    }

    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        Timber.d("onJsPaymentResult %s", pResult);
        try {
            AccountLinkWorkFlow workFlow = (AccountLinkWorkFlow) getWorkFlow();
            if (workFlow == null) {
                return;
            }
            workFlow.getActivity().runOnUiThread(() -> {
                LinkAccScriptOutput scriptOutput = GsonUtils.fromJsonString(pResult, LinkAccScriptOutput.class);
                EEventType eventType = convertPageIdToEvent(mEventID);
                StatusResponse response = getResponse(eventType, scriptOutput);
                if (mEventID == 0 && mIsFirst && scriptOutput != null && !scriptOutput.isError()) {
                    mIsFirst = false;
                    hit();
                } else {
                    if (eventType == EEventType.ON_REQUIRE_RENDER) {
                        workFlow.onEvent(EEventType.ON_REQUIRE_RENDER, scriptOutput, mPageCode);
                    } else {
                        workFlow.onEvent(eventType, response, mPageCode, mEventID);
                    }
                }
            });
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private EEventType convertPageIdToEvent(int pEventID) {
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

    private StatusResponse getResponse(EEventType pEventType, LinkAccScriptOutput pScriptOutput) {
        StatusResponse ret = new StatusResponse();
        ret.returnmessage = pScriptOutput.message;

        if (!pScriptOutput.isError()) {
            ret.returncode = 4;
        } else {
            ret.returncode = -4;
        }

        return ret;
    }
}
