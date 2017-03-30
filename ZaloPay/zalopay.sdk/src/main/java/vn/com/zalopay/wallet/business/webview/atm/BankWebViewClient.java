package vn.com.zalopay.wallet.business.webview.atm;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import java.util.List;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptInput;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentReturnCode;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

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

    private List<DBankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mCurrentUrlPattern = null;
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;

    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;

    public BankWebViewClient(AdapterBase pAdapter) {
        super(pAdapter);

        if (getAdapter() != null) {
            mWebPaymentBridge = new BankWebView(getAdapter().getActivity().getApplicationContext());
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        }
    }

    public void start(String pUrl) {

        mWebPaymentBridge.loadUrl(pUrl);
        mIsFirst = true;
    }

    public void hit() {
        mCurrentUrlPattern = null;

        // Check if ajax

        final long time = System.currentTimeMillis();
        mLastStartPageTime = time;
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                onAjax(time);
            }
        }, DELAY_TIME_TO_DETECT_AJAX);

        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
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
            input.accountIndex = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getSelectedAccountIndex();
            input.captcha = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getCaptcha();
            input.username = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getUsername();
            input.password = ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).getPassword();

            return input;
        }
        return input;
    }

    public void matchAndRunJs(String url, EJavaScriptType pType, boolean pIsAjax) {
        //for testing
        /*
        if( ! TextUtils.isEmpty(((BankCardGuiProcessor)getAdapter().getGuiProcessor()).getOtp()))
		{
			getAdapter().onEvent(EEventType.ON_FAIL);
			return;
		}
		*/

        boolean isMatched = false;
        for (DBankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && url.matches(bankScript.url)) {
                Log.d(this, "$$$$$$ matchAndRunJs: " + url + " ,type: " + pType);
                isMatched = true;

                mCurrentUrl = url;
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;

                DAtmScriptInput input = genJsInput();
                input.isAjax = pIsAjax;

                String inputScript = GsonUtils.toJsonString(input);

                if (pType == EJavaScriptType.AUTO)
                    executeJs(bankScript.autoJs, inputScript);

                if (mCurrentUrlPattern != null && mCurrentUrlPattern.equals(bankScript.url)) {
                    continue;
                }

                // Process this url
                mCurrentUrlPattern = bankScript.url;
                if (pType == EJavaScriptType.HIT)
                    executeJs(bankScript.hitJs, inputScript);
            }
        }

        if (!isMatched) {
            getAdapter().onEvent(EEventType.ON_FAIL);
        }
    }

    public void executeJs(String pJsFileName, String pJsInput) {
        if (!TextUtils.isEmpty(pJsFileName)) {
            Log.d(this, pJsFileName);
            Log.d(this, pJsInput);

            String jsContent = null;
            for (String jsFile : pJsFileName.split(Constants.COMMA)) {
                jsContent = ResourceManager.getJavascriptContent(jsFile);
                jsContent = String.format(jsContent, pJsInput);
                mWebPaymentBridge.runScript(jsContent);
            }
        }
    }

    public void onAjax(long pLastStartPageTime) {
        if (mIsLoadingFinished && mLastStartPageTime == pLastStartPageTime) {
            Log.i(this, "///// onAjax: " + mCurrentUrl);
            matchAndRunJs(mCurrentUrl, EJavaScriptType.AUTO, true);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(this, "///// onPageStarted: " + url);

        mStartedtUrl = url;
        mIsLoadingFinished = false;

        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;

        /***
         * STOP WEBVIEW IF THIS IS THE FINAL STEP (REDIRECT SUCCESS FROM 123PAY)
         */
        if (mStartedtUrl.contains(BuildConfig.HOST_COMPLETE)) {
            view.stopLoading();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d(this, "///// shouldOverrideUrlLoading: " + url);

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
        Log.d(this, "****** Debug webview: " + msg);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (!mIsRedirect) {
            mIsLoadingFinished = true;
        }

        if (mIsLoadingFinished && !mIsRedirect) {
            Log.d(this, "onPageFinished" + url);

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
        Log.d(this, "++++ Current error SSL on page: " + mStartedtUrl);

        for (DBankScript bankScript : mBankScripts) {
            if (bankScript.eventID == IGNORE_EVENT_ID_FOR_HTTPS && mStartedtUrl.matches(bankScript.url)) {
                handler.proceed(); // Ignore SSL certificate errors
                return;
            }
        }
    }

    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        Log.d(this, "==== onJsPaymentResult: " + pResult);
        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;

        final String result = pResult;

        getAdapter().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DAtmScriptOutput scriptOutput = GsonUtils.fromJsonString(result, DAtmScriptOutput.class);

                Log.d("=====onJsPaymentResult=====", GsonUtils.toJsonString(scriptOutput));

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

            }
        });
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
                ret.returncode = EPaymentReturnCode.ATM_VERIFY_OTP_SUCCESS.getValue();
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

    public void dispose() {
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

    public String getCurrentUrl() {
        return mWebPaymentBridge.getUrl();
    }

    @JavascriptInterface
    public void getHtml(final String pHtml) {
        //pHtml = PaymentHtmlParser.getContent(pHtml);
        Log.d(this, "===pHtml=" + pHtml);
        if (getAdapter() != null) {
            getAdapter().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
                    if (!TextUtils.isEmpty(paymentError)) {
                        paymentError = String.format(paymentError, null, getCurrentUrl(), pHtml);
                    }

                    try {
                        getAdapter().sdkReportError(SDKReportTask.ERROR_WEBSITE, !TextUtils.isEmpty(paymentError) ? paymentError : pHtml);
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                }
            });
        }
    }
}
