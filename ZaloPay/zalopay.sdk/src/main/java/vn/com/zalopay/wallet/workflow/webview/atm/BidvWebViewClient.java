package vn.com.zalopay.wallet.workflow.webview.atm;

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
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.entity.bank.AtmScriptInput;
import vn.com.zalopay.wallet.entity.bank.AtmScriptOutput;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.entity.bank.BankScript;
import vn.com.zalopay.wallet.workflow.webview.base.PaymentWebViewClient;
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

public class BidvWebViewClient extends PaymentWebViewClient {
    public static final long DELAY_TIME_TO_RUN_SCRIPT = 4000;
    public static final int MAX_INTERVAL_CHECK_COUNT = 15;
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    protected int countIntervalCheck = 0;

    ;
    private boolean mIsRunningScript = false;

    private boolean mIsLoadingFinished = true;
    private boolean mIsRedirect = false;

    private List<BankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;
    private BankScript mCurrentBankScript = null;

    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;

    public BidvWebViewClient(AbstractWorkFlow pAdapter) {
        super(pAdapter);
        if (pAdapter == null) {
            return;
        }
        try {
            mWebPaymentBridge = (BankWebView) getAdapter().getActivity().findViewById(R.id.webviewParser);
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void start(String pUrl) {
        if (mWebPaymentBridge == null) {
            return;
        }
        mWebPaymentBridge.loadUrl(pUrl);
        mIsFirst = true;
    }

    @Override
    public void stop() {
        if (mWebPaymentBridge != null) {
            mWebPaymentBridge.stopLoading();
        }
    }

    @Override
    public void hit() {
        mLastStartPageTime = System.currentTimeMillis();
        matchAndRunJs(EJavaScriptType.HIT, false);
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
            input.onlinePassword = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getOnlinePassword();

            return input;
        }
        return input;
    }

    protected boolean isMatchUrl(String pUrl) {
        boolean isMatch = false;
        for (BankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && pUrl.matches(bankScript.url)) {
                Timber.d("$$$$$$ matchAndRunJs: " + pUrl);

                mCurrentUrl = pUrl;
                mCurrentBankScript = bankScript;
                isMatch = true;
            }
        }

        return isMatch;
    }

    public void matchAndRunJs(EJavaScriptType pType, boolean pIsAjax) {
        if (mCurrentBankScript == null) {
            return;
        }
        mEventID = mCurrentBankScript.eventID;
        mPageCode = mCurrentBankScript.pageCode;

        AtmScriptInput input = null;
        try {
            input = genJsInput();
        } catch (Exception e) {
            Timber.w(e);
        }
        if (input == null) {
            return;
        }
        input.isAjax = pIsAjax;
        String inputScript = GsonUtils.toJsonString(input);
        if (pType == EJavaScriptType.AUTO) {
            executeJs(mCurrentBankScript.autoJs, inputScript);
        }
        if (pType == EJavaScriptType.HIT) {
            executeJs(mCurrentBankScript.hitJs, inputScript);
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

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mStartedtUrl = url;
        mIsLoadingFinished = false;
        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;
        if (!TextUtils.isEmpty(mStartedtUrl)
                && mStartedtUrl.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain))) {
            view.stopLoading();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!mIsLoadingFinished) {
            mIsRedirect = true;
        }
        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;
        mIsLoadingFinished = false;
        view.loadUrl(url);

        return true;
    }

    private boolean loadingStaticResource(String pUrl) {
        return TextUtils.isEmpty(pUrl) || pUrl.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain)) || pUrl.endsWith(".css") || pUrl.endsWith(".js") || pUrl.endsWith(".png") || pUrl.endsWith(".ico")
                || pUrl.endsWith(".xml") || pUrl.endsWith(".jpg") || pUrl.endsWith(".jpeg") || pUrl.endsWith(".gif");
    }

    private void intervalCheck() {
        if (countIntervalCheck >= MAX_INTERVAL_CHECK_COUNT) {
            return;
        }
        countIntervalCheck++;
        mIsRunningScript = true;
        mHandler.postDelayed(() -> matchAndRunJs(EJavaScriptType.AUTO, false), DELAY_TIME_TO_RUN_SCRIPT);
    }

    public void onLoadResource(WebView view, final String url) {
        if (!loadingStaticResource(url) && isMatchUrl(url)) {
            if (mIsRunningScript) {
                return;
            }
            intervalCheck();
        }
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
            onPageFinished(url);
        } else {
            mIsRedirect = false;
        }
    }

    public void onPageFinished(String url) {
        if (isMatchUrl(url)) {
            matchAndRunJs(EJavaScriptType.AUTO, false);
        }
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
        mIsRunningScript = false;
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
                countIntervalCheck = 0;
                EEventType eventType = convertPageIdToEvent(mEventID);
                BaseResponse response = genResponse(eventType, scriptOutput);
                if (mEventID == 0 && mIsFirst && scriptOutput != null && !scriptOutput.isError()) {
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
            Timber.d(e);
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

    public enum EJavaScriptType {
        AUTO, HIT
    }
}