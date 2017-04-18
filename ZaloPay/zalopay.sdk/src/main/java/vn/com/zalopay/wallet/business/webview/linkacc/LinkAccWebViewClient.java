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

import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.WebViewError;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptInput;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptOutput;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.helper.WebViewHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

/**
 * @author SinhTT
 */
public class LinkAccWebViewClient extends PaymentWebViewClient {
    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    public static final int TIME_REQUEST_RETRY_LOADING_AGAIN = 10000; // 10s
    public static final int TIME_WAITING_LOAD_AJAX_GET_RESULT = 5000; // 5s
    public static final int TIME_WAITING_LOAD_AJAX_GET_MESSAGE = 200; // 200ms
    protected static final String HTTP_EXCEPTION = "http://sdk.jsexception";
    // value is used for
    // detect valid url
    // in the case
    // webview on
    // Android 2.3
    private boolean mIsLoading = false;
    private boolean isRedirected = false;
    // private boolean mIsRedirect = false;
    // private boolean mIsFreeze = false;

    private AdapterLinkAcc mAdapter = null;
    private LinkAccWebView mWebPaymentBridge = null;

    private List<DBankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
    private String mCurrentUrlPattern = null;
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;
    private String mUrl = null;

    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;
    private boolean mIsRefreshCaptcha = false;
    private boolean mIsRetry = false;

    private int mProgress = 0;
    private int mProgressOld = 0;
    /***
     * listener for WebChromeClient
     */
    private WebChromeClient wcClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(final WebView view, int newProgress) {
            Log.i("LOADING...", String.valueOf(newProgress) + "%");
            mAdapter.onEvent(EEventType.ON_PROGRESSING, newProgress);

//            // check for retry.
//            if (mProgress != newProgress) {
//                mProgress = newProgress;
//                mIsRetry = true;
//            }
//
//            // set value
//            if (mIsRetry) {
//                mProgressOld = mProgress;
//                mIsRetry = false;
//            }
//
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (mProgress == mProgressOld && mProgress < 100) {
//                        retry(view, mUrl);
//                    }
//                }
//            }, TIME_REQUEST_RETRY_LOADING_AGAIN);
        }
    };

    public LinkAccWebViewClient(AdapterBase pAdapter) {
        super(pAdapter);
        if (pAdapter != null) {
            mAdapter = (AdapterLinkAcc) pAdapter;
            // Avoid memory-leak in WebView:
            // http://stackoverflow.com/questions/3130654/memory-leak-in-webview
            mWebPaymentBridge = new LinkAccWebView(GlobalData.getAppContext());
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.setWebChromeClient(wcClient);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        }
    }


    public LinkAccWebViewClient(AdapterBase pAdapter, LinkAccWebView pWeb) {
        super(pAdapter);
        if (pAdapter != null) {
            mAdapter = (AdapterLinkAcc) pAdapter;
            // Avoid memory-leak in WebView:
            // http://stackoverflow.com/questions/3130654/memory-leak-in-webview
            mWebPaymentBridge = pWeb;
            mWebPaymentBridge.setWebViewClient(this);
            mWebPaymentBridge.setWebChromeClient(wcClient);
            mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i("///// onPageStarted: ", url);
        if (!isRedirected) {
            // code somethings if you want when starts
            mUrl = url;
        }

        isRedirected = false;

    }

    protected boolean shouldOnCheckMatchOnLoadResouce(String pUrl) {
        return pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_generate_captcha))
                || pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_register_complete))
                || pUrl.matches(GlobalData.getStringResource(RS.string.zpw_string_special_bankscript_vcb_unregister_complete));
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d("TAG", "///// onLoadResource: " + url);
        if (shouldOnCheckMatchOnLoadResouce(url)) {
            onPageFinishedAuto(url);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("OverrideUrl", "///// shouldOverrideUrlLoading:" + url);
        view.loadUrl(url);
        isRedirected = true;
        return true;
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
        Log.d("///// onPageFinished: ", url);
        if (!isRedirected) {
            //Do something you want when finished loading
            Log.i("Runnable", "=========== ALREADY FINISHED ===========");
            Log.i("Runnable", url);
            onPageFinishedAuto(url);
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
                    getAdapter().sdkReportError(SDKReport.ERROR_WEBSITE, error.toString());
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
            StringBuffer errStringBuilder = new StringBuffer();
            errStringBuilder.append(description);
            errStringBuilder.append("\n");
            errStringBuilder.append(failingUrl);
            try {
                getAdapter().sdkReportError(SDKReport.ERROR_WEBSITE, errStringBuilder.toString());
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


    public void stop() {
        mWebPaymentBridge.stopLoading();
    }


    public void refreshCaptcha() {
        // new url load.
        mIsRefreshCaptcha = true;
        matchAndRunJs(mCurrentUrl, EJavaScriptType.HIT, false);
    }
    public void reload()
   {
       mWebPaymentBridge.reload();
   }
    public void hit() {
        mAdapter.onEvent(EEventType.ON_HIT);
        mCurrentUrlPattern = null;
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
                Log.d("WebView", "$$$$$$ matchAndRunJs: " + url + " ,type: " + pType);
                isMatched = true;
                if (bankScript.pageCode.equals(mAdapter.VCB_REGISTER_PAGE)) {
                    mAdapter.mUrlReload = url;
                }
                mEventID = bankScript.eventID;
                mPageCode = bankScript.pageCode;
                DLinkAccScriptInput input = genJsInput();
                input.isAjax = pIsAjax;
                String inputScript = GsonUtils.toJsonString(input);

                if (pType == EJavaScriptType.AUTO)
                    executeJs(bankScript.autoJs, inputScript);

                if (pType == EJavaScriptType.HIT)
                    executeJs(bankScript.hitJs, inputScript);

                // break loop for
                break;
            }else if(mIsRefreshCaptcha && bankScript.pageCode.equals(mAdapter.VCB_REFRESH_CAPTCHA)) {
                Log.d("WebView", "$$$$$$ matchAndRunJs: " + url + " ,type: " + pType);
                DLinkAccScriptInput input = genJsInput();
                input.isAjax = pIsAjax;
                String inputScript = GsonUtils.toJsonString(input);
                if (pType == EJavaScriptType.HIT)
                    executeJs(bankScript.hitJs, inputScript);
                // break loop for
                mIsRefreshCaptcha = false;
                break;
            }
        }

        if (!isMatched) {
            mAdapter.onEvent(EEventType.ON_FAIL);
        }

    }

    public void executeJs(String pJsFileName, String pJsInput) {
        if (!TextUtils.isEmpty(pJsFileName)) {
            String jsContent = null;

            Log.d("WebView", pJsFileName);
            Log.d("WebView", pJsInput);

            for (String jsFile : pJsFileName.split(Constants.COMMA)) {
                jsContent = ResourceManager.getJavascriptContent(jsFile);
                jsContent = String.format(jsContent, pJsInput);
                mWebPaymentBridge.runScript(jsContent);
            }
        }
    }

    @JavascriptInterface
    public void logDebug(String msg) {
        Log.d("Js", "****** Debug webview: " + msg);
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
        Log.d("Js", "==== onJsPaymentResult: " + pResult);
        // Modify this variable to inform that it not run in ajax mode
        mLastStartPageTime++;

        final String result = pResult;

        getAdapter().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

    public boolean isVerifyCardComplete() {
        return mEventID == 1;
    }

    public StatusResponse genResponse(EEventType pEventType, DLinkAccScriptOutput pScriptOutput) {
        StatusResponse ret = null;
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
