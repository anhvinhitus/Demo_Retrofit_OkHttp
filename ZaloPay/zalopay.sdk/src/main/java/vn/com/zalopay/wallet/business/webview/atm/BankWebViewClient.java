package vn.com.zalopay.wallet.business.webview.atm;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.AtmScriptInput;
import vn.com.zalopay.wallet.business.entity.atm.AtmScriptOutput;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankScript;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.ParseWebCode;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkParseWebsiteCompleteEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteErrorEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteRenderEvent;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.ERROR_WEBSITE;

public class BankWebViewClient extends PaymentWebViewClient {

    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    public static final long DELAY_TIME_TO_DETECT_AJAX = 8000;
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    // value is used for
    // detect valid url
    // in the case
    // webview on
    // Android 2.3

    private boolean mIsLoadingFinished = true;
    private boolean mIsRedirect = false;

    private BankWebView mWebPaymentBridge = null;

    private List<BankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mCurrentUrlPattern = null;
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;

    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;

    public BankWebViewClient(AbstractWorkFlow pAdapter) {
        super(pAdapter);
        initWebViewBridge();
    }

    private void initWebViewBridge() {
        mWebPaymentBridge = new BankWebView(GlobalData.getAppContext());
        mWebPaymentBridge.setWebViewClient(this);
        mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
    }

    public void start(String pUrl) {
        if (mWebPaymentBridge != null) {
            mWebPaymentBridge.loadUrl(pUrl);
            mIsFirst = true;
        }
    }

    @Override
    public void stop() {
        if (mWebPaymentBridge != null) {
            mWebPaymentBridge.stopLoading();
        }
    }

    public void hit() {
        mCurrentUrlPattern = null;

        // Check if ajax

        final long time = System.currentTimeMillis();
        mLastStartPageTime = time;
        mHandler.postDelayed(() -> onAjax(time), DELAY_TIME_TO_DETECT_AJAX);

        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
    }

    public AtmScriptInput genJsInput() throws Exception {
        AtmScriptInput input = new AtmScriptInput();

        if (getAdapter() != null && getAdapter().getGuiProcessor() != null) {
            input.cardHolderName = getAdapter().getGuiProcessor().getCardName();
            input.cardNumber = getAdapter().getGuiProcessor().getCardNumber();
            input.cardMonth = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getCardMonth();
            input.cardYear = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getCardYear();
            input.cardPass = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getCardPass();
            input.otp = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getOtp();
            input.captcha = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getCaptcha();
            input.username = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getUsername();
            input.password = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getPassword();

            return input;
        }
        return input;
    }

    public void matchAndRunJs(String url, EJavaScriptType pType, boolean pIsAjax) {
        boolean isMatched = false;
        for (BankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && url.matches(bankScript.url)) {
                Timber.d("$$$$$$ matchAndRunJs: " + url + " ,type: " + pType);
                isMatched = true;

                mCurrentUrl = url;
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;

                AtmScriptInput input = null;
                try {
                    input = genJsInput();
                } catch (Exception e) {
                    Timber.w(e.getMessage());
                    SDKApplication.getApplicationComponent().eventBus().postSticky(new SdkParseWebsiteErrorEvent());
                }
                input.isAjax = pIsAjax;

                String inputScript = GsonUtils.toJsonString(input);

                if (pType == EJavaScriptType.AUTO) {
                    executeJs(bankScript.autoJs, inputScript);
                }

                if (mCurrentUrlPattern != null && mCurrentUrlPattern.equals(bankScript.url)) {
                    continue;
                }

                // Process this url
                mCurrentUrlPattern = bankScript.url;
                if (pType == EJavaScriptType.HIT) {
                    executeJs(bankScript.hitJs, inputScript);
                }
            }
        }

        if (!isMatched) {
            SDKApplication.getApplicationComponent().eventBus().postSticky(new SdkParseWebsiteErrorEvent());
        }
    }

    public void executeJs(String pJsFileName, String pJsInput) {
        if (TextUtils.isEmpty(pJsFileName)) {
            return;
        }
        if (mWebPaymentBridge == null) {
            Timber.d("NULL on executeJs");
            return;
        }
        Timber.d("file name %s input %s", pJsFileName, pJsInput);
        Subscription subscription = Observable.from(pJsFileName.split(Constants.COMMA))
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(ResourceManager::getJavascriptContent)
                .filter(s -> !TextUtils.isEmpty(s))
                .map(jsContent -> String.format(jsContent, pJsInput))
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(jsContent -> mWebPaymentBridge.runScript(jsContent),
                        throwable -> Timber.w(throwable, "Exception load js file"));
        CompositeSubscription compositeSubscription = getAdapter() != null ? getAdapter().mCompositeSubscription : null;
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }

    public void onAjax(long pLastStartPageTime) {
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

        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;
        //STOP WEBVIEW IF THIS IS THE FINAL STEP (REDIRECT SUCCESS FROM 123PAY)
        if (!TextUtils.isEmpty(mStartedtUrl) &&
                mStartedtUrl.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain))) {
            view.stopLoading();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("///// shouldOverrideUrlLoading: " + url);

        if (!mIsLoadingFinished) {
            mIsRedirect = true;
        }
        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;

        mIsLoadingFinished = false;
        view.loadUrl(url);

        return true;
    }

    public void onLoadResource(WebView view, String url) {
        // Log.d(this, "///// onLoadResource: " + url);
    }

    @JavascriptInterface
    public void logDebug(String msg) {
        Timber.d("****** Debug webview: " + msg);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (!mIsRedirect) {
            mIsLoadingFinished = true;
        }

        if (mIsLoadingFinished && !mIsRedirect) {
            Timber.d("onPageFinished" + url);

            onPageFinished(url);
        } else {
            mIsRedirect = false;
        }
    }

    public void onPageFinished(String url) {
        matchAndRunJs(url, EJavaScriptType.AUTO, false);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Timber.d("++++ Current error SSL on page: " + mStartedtUrl);

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
            if (getAdapter() == null) {
                return;
            }
            Activity activity = getAdapter().getActivity();
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

    public boolean isVerifyCardComplete() {
        return mEventID == 1;
    }

    public EEventType convertPageIdToEvent(int pEventID) {
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

    public BaseResponse genResponse(EEventType pEventType, AtmScriptOutput pScriptOutput) {
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

    public void dispose() {
        if (mWebPaymentBridge != null) {
            mWebPaymentBridge.removeJavascriptInterface(JAVA_SCRIPT_INTERFACE_NAME);
            mWebPaymentBridge.setWebViewClient(null);
            mWebPaymentBridge.removeAllViews();
            mWebPaymentBridge.clearHistory();
            mWebPaymentBridge.freeMemory();
            mWebPaymentBridge.destroy();
            mWebPaymentBridge = null;
        }
    }

    public String getCurrentUrl() {
        return mWebPaymentBridge.getUrl();
    }

    @JavascriptInterface
    public void getHtml(final String pHtml) {
        try {
            //pHtml = PaymentHtmlParser.getContent(pHtml);
            if (getAdapter() == null) {
                return;
            }
            Activity activity = getAdapter().getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(() -> {
                try {
                    String paymentError = GlobalData.getAppContext().getResources().getString(R.string.sdk_report_error_format);
                    if (!TextUtils.isEmpty(paymentError)) {
                        paymentError = String.format(paymentError, null, getCurrentUrl(), pHtml);
                    }
                    SdkErrorReporter reporter = SDKApplication.sdkErrorReporter();
                    reporter.sdkReportError(getAdapter(), ERROR_WEBSITE, !TextUtils.isEmpty(paymentError) ? paymentError : pHtml);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            Timber.d(e, "Exception getHtml");
        }
    }
}
