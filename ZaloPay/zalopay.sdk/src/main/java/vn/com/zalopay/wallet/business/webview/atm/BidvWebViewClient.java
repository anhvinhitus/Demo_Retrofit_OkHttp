package vn.com.zalopay.wallet.business.webview.atm;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptInput;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.ParseWebCode;

public class BidvWebViewClient extends PaymentWebViewClient {
    public static final long DELAY_TIME_TO_RUN_SCRIPT = 4000;
    public static final int MAX_INTERVAL_CHECK_COUNT = 15;
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    protected int countIntervalCheck = 0;

    ;
    private boolean mIsRunningScript = false;

    private boolean mIsLoadingFinished = true;
    private boolean mIsRedirect = false;

    private List<DBankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;
    private DBankScript mCurrentBankScript = null;

    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;

    public BidvWebViewClient(AdapterBase pAdapter) {
        super(pAdapter);
        if (getAdapter() != null) {
            try {
                mWebPaymentBridge = (BankWebView) getAdapter().getActivity().findViewById(R.id.webviewParser);
                mWebPaymentBridge.setWebViewClient(this);
                mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    @Override
    public void start(String pUrl) {
        mWebPaymentBridge.loadUrl(pUrl);
        mIsFirst = true;
    }

    @Override
    public void hit() {
        mLastStartPageTime = System.currentTimeMillis();
        matchAndRunJs(EJavaScriptType.HIT, false);
    }

    public DAtmScriptInput genJsInput() {
        DAtmScriptInput input = new DAtmScriptInput();
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
        for (DBankScript bankScript : mBankScripts) {
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
            Log.e(this, "===matchAndRunJs===mCurrentUrl=" + mCurrentUrl + " mCurrentBankScript=NULL");
            return;
        }
        mEventID = mCurrentBankScript.eventID;
        mPageCode = mCurrentBankScript.pageCode;

        DAtmScriptInput input = genJsInput();
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
        if (!TextUtils.isEmpty(pJsFileName)) {
            Timber.d(pJsFileName);
            Timber.d(pJsInput);

            String jsContent = null;
            for (String jsFile : pJsFileName.split(Constants.COMMA)) {
                jsContent = ResourceManager.getJavascriptContent(jsFile);
                jsContent = String.format(jsContent, pJsInput);
                mWebPaymentBridge.runScript(jsContent);
            }
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(this, "===onPageStarted: ===" + url);

        mStartedtUrl = url;
        mIsLoadingFinished = false;

        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;
        if (mStartedtUrl.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain))) {
            view.stopLoading();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("===shouldOverrideUrlLoading: ===" + url);
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
            Timber.d("===intervalCheck===stop");
            return;
        }

        countIntervalCheck++;
        Timber.d("===intervalCheck===" + countIntervalCheck);
        mIsRunningScript = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                matchAndRunJs(EJavaScriptType.AUTO, false);
            }
        }, DELAY_TIME_TO_RUN_SCRIPT);
    }

    public void onLoadResource(WebView view, final String url) {
        Timber.d("===onLoadResource: ===" + url);
        if (!loadingStaticResource(url) && isMatchUrl(url)) {
            if (mIsRunningScript) {
                Timber.d("===there're a script is runing===");
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
        Timber.d("===onPageFinished===" + url);

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
        for (DBankScript bankScript : mBankScripts) {
            if (bankScript.eventID == IGNORE_EVENT_ID_FOR_HTTPS && mStartedtUrl.matches(bankScript.url)) {
                handler.proceed(); // Ignore SSL certificate errors
                return;
            }
        }
    }

    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        mIsRunningScript = false;
        Log.d(this, "onJsPaymentResult", pResult);
        mLastStartPageTime++;
        final String result = pResult;
        try {
            getAdapter().getActivity().runOnUiThread(() -> {
                DAtmScriptOutput scriptOutput = GsonUtils.fromJsonString(result, DAtmScriptOutput.class);
                countIntervalCheck = 0;
                EEventType eventType = convertPageIdToEvent(mEventID);
                BaseResponse response = genResponse(eventType, scriptOutput);
                if (mEventID == 0 && mIsFirst && !scriptOutput.isError()) {
                    // Auto hit at first step
                    mIsFirst = false;
                    hit();
                } else {
                    if (eventType == EEventType.ON_REQUIRE_RENDER) {
                        getAdapter().onEvent(EEventType.ON_REQUIRE_RENDER, scriptOutput, mPageCode);
                    } else {
                        getAdapter().onEvent(eventType, response, mPageCode, mEventID);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(this, e);
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

    public BaseResponse genResponse(EEventType pEventType, DAtmScriptOutput pScriptOutput) {
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

        if (ret != null) {
            if (!pScriptOutput.isError()) {
                ret.returncode = 4;
            } else {
                ret.returncode = -4;
            }
        }

        return ret;
    }

    public enum EJavaScriptType {
        AUTO, HIT
    }
}