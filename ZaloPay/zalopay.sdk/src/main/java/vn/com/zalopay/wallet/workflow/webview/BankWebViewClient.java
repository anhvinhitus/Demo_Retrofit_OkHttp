package vn.com.zalopay.wallet.workflow.webview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.constants.ParseWebCode;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.entity.bank.AtmScriptInput;
import vn.com.zalopay.wallet.entity.bank.AtmScriptOutput;
import vn.com.zalopay.wallet.entity.bank.BankScript;
import vn.com.zalopay.wallet.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.event.SdkParseWebsiteCompleteEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteErrorEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteRenderEvent;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;

public class BankWebViewClient extends AbstractWebViewClient {
    static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    private static final long DELAY_TIME_TO_DETECT_AJAX = 8000;
    List<BankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    int mEventID = 0;
    String mPageCode = null;
    boolean mIsFirst = true;
    private boolean mIsLoadingFinished = true;
    private boolean mIsRedirect = false;
    private String mCurrentUrlPattern = null;
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;
    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    public BankWebViewClient(AbstractWorkFlow pAdapter, SdkWebView pWebview) {
        pWebview.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        initialize(pAdapter, pWebview);
    }

    @Override
    public void start(String pUrl) {
        try {
            WebView webView = getWebView();
            if (webView != null) {
                webView.loadUrl(pUrl);
                mIsFirst = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void stop() {
        try {
            WebView webView = getWebView();
            if (webView != null) {
                webView.stopLoading();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void hit() {
        mCurrentUrlPattern = null;
        final long time = System.currentTimeMillis();
        mLastStartPageTime = time;
        mHandler.postDelayed(() -> onAjax(time), DELAY_TIME_TO_DETECT_AJAX);

        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
    }

    @NonNull
    protected AtmScriptInput genJsInput() throws Exception {
        AtmScriptInput input = new AtmScriptInput();
        AbstractWorkFlow workFlow = null;
        try {
            workFlow = getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            return input;
        }
        BankCardGuiProcessor guiProcessor = null;
        try {
            if (workFlow.getGuiProcessor() instanceof BankCardGuiProcessor) {
                guiProcessor = (BankCardGuiProcessor) workFlow.getGuiProcessor();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        if (guiProcessor != null) {
            input.cardHolderName = guiProcessor.getCardName();
            input.cardNumber = guiProcessor.getCardNumber();
            input.cardMonth = guiProcessor.getCardMonth();
            input.cardYear = guiProcessor.getCardYear();
            input.cardPass = guiProcessor.getCardPass();
            input.otp = guiProcessor.getOtp();
            input.captcha = guiProcessor.getCaptcha();
            input.username = guiProcessor.getUsername();
            input.password = guiProcessor.getPassword();
        }
        return input;
    }

    private void matchAndRunJs(String url, EJavaScriptType pType, boolean pIsAjax) {
        boolean isMatched = false;
        for (BankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && url.matches(bankScript.url)) {
                Timber.d("matchAndRunJs: %s type %s", url, pType);
                isMatched = true;
                mCurrentUrl = url;
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;

                AtmScriptInput input = null;
                try {
                    input = genJsInput();
                } catch (Exception e) {
                    Timber.d(e);
                }
                if (input != null) {
                    input.isAjax = pIsAjax;
                }

                String inputScript = GsonUtils.toJsonString(input);

                if (pType == EJavaScriptType.AUTO) {
                    executeJs(bankScript.autoJs, inputScript);
                }

                if (mCurrentUrlPattern != null && mCurrentUrlPattern.equals(bankScript.url)) {
                    continue;
                }

                mCurrentUrlPattern = bankScript.url;
                if (pType == EJavaScriptType.HIT) {
                    executeJs(bankScript.hitJs, inputScript);
                }
            }
        }

        if (!isMatched) {
            SDKApplication
                    .getApplicationComponent()
                    .eventBus()
                    .postSticky(new SdkParseWebsiteErrorEvent());
        }
    }

    private void onAjax(long pLastStartPageTime) {
        if (mIsLoadingFinished && mLastStartPageTime == pLastStartPageTime) {
            Timber.d("onAjax %s", mCurrentUrl);
            matchAndRunJs(mCurrentUrl, EJavaScriptType.AUTO, true);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Timber.d("onPageStarted %s", url);
        mStartedtUrl = url;
        mIsLoadingFinished = false;
        mLastStartPageTime++;
        //STOP WEBVIEW IF THIS IS THE FINAL STEP (REDIRECT SUCCESS FROM 123PAY)
        if (!TextUtils.isEmpty(mStartedtUrl) &&
                mStartedtUrl.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain))) {
            view.stopLoading();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading: %s", url);
        if (!mIsLoadingFinished) {
            mIsRedirect = true;
        }
        mLastStartPageTime++;
        mIsLoadingFinished = false;
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (!mIsRedirect) {
            mIsLoadingFinished = true;
        }

        if (mIsLoadingFinished && !mIsRedirect) {
            Timber.d("onPageFinished %s", url);
            onPageFinished(url);
        } else {
            mIsRedirect = false;
        }
    }

    protected void onPageFinished(String url) {
        matchAndRunJs(url, EJavaScriptType.AUTO, false);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Timber.d("Current error SSL on page: %s", mStartedtUrl);
        for (BankScript bankScript : mBankScripts) {
            if (bankScript.eventID == IGNORE_EVENT_ID_FOR_HTTPS && mStartedtUrl.matches(bankScript.url)) {
                handler.proceed(); // Ignore SSL certificate errors
                return;
            }
        }
    }

    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        Timber.d("onJsPaymentResult %s", pResult);
        mLastStartPageTime++;
        final String result = pResult;
        try {
            if (getWorkFlow() == null) {
                return;
            }
            Activity activity = getWorkFlow().getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(() -> {
                AtmScriptOutput scriptOutput = GsonUtils.fromJsonString(result, AtmScriptOutput.class);
                Timber.d("onJsPaymentResult: %s", GsonUtils.toJsonString(scriptOutput));
                EEventType eventType = convertPageIdToEvent(mEventID);
                BaseResponse response = genResponse(eventType, scriptOutput);
                if (mEventID == 0 && mIsFirst
                        && scriptOutput != null && !scriptOutput.isError()) {
                    // Auto hit at first step
                    mIsFirst = false;
                    hit();
                } else {
                    EventBus eventBus = SDKApplication.getApplicationComponent().eventBus();
                    if (eventType == EEventType.ON_REQUIRE_RENDER) {
                        eventBus.postSticky(new SdkParseWebsiteRenderEvent(scriptOutput, mPageCode));
                    } else if (eventType == EEventType.ON_FAIL) {
                        eventBus.postSticky(new SdkParseWebsiteErrorEvent());
                    } else if (eventType == EEventType.ON_PAYMENT_COMPLETED) {
                        eventBus.postSticky(new SdkParseWebsiteCompleteEvent(response));
                    }
                }

            });
        } catch (Exception e) {
            Timber.d(e, "Exception on onJsPaymentResult");
        }
    }

    EEventType convertPageIdToEvent(int pEventID) {
        switch (pEventID) {
            case -1:
                return EEventType.ON_FAIL;
            case 0: // AUTO HIT at first time
            case 1: // Verify card complete
            case 2:
                return EEventType.ON_REQUIRE_RENDER;
            case 3:
                return EEventType.ON_PAYMENT_COMPLETED;
            default:
                return EEventType.ON_REQUIRE_RENDER;
        }
    }

    BaseResponse genResponse(EEventType pEventType, AtmScriptOutput pScriptOutput) {
        BaseResponse ret = new BaseResponse();
        switch (pEventType) {

            case ON_PAYMENT_COMPLETED:
                ret.returnmessage = pScriptOutput.message;
                ret.returncode = ParseWebCode.ATM_VERIFY_OTP_SUCCESS;
                return ret;

            default:
                ret.returnmessage = pScriptOutput.message;
                break;
        }

        if (!pScriptOutput.isError()) {
            ret.returncode = 4;
        } else {
            ret.returncode = -4;
        }
        return ret;
    }

    @JavascriptInterface
    public void getHtml(final String pHtml) {
        try {
            AbstractWorkFlow workFlow = getWorkFlow();
            if (workFlow == null) {
                return;
            }
            Activity activity = getWorkFlow().getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            SdkWebView webView;
            webView = getWebView();
            String currentUrl = webView != null ? webView.getUrl() : null;
            if (TextUtils.isEmpty(currentUrl)) {
                return;
            }
            activity.runOnUiThread(() -> {
                try {
                    String paymentError = GlobalData.getAppContext().getResources().getString(R.string.sdk_report_error_format);
                    paymentError = String.format(paymentError, null, currentUrl, pHtml);
                    SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                    reporter.sdkReportError(workFlow, ERROR_WEBSITE, !TextUtils.isEmpty(paymentError) ? paymentError : pHtml);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            Timber.d(e, "Exception getHtml");
        }
    }
}
