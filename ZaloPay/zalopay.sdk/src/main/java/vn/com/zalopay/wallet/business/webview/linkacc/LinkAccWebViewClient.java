/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed,
 * or transmitted in any form or by any means, including photocopying, recording,
 * or other electronic or mechanical methods, without the prior written permission
 * of the publisher, except in the case of brief quotations embodied in critical reviews
 * and certain other noncommercial uses permitted by copyright law.
 * <p>
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.AbstractWebViewProcessor.java
 * Created date: Jan 15, 2016
 * Owner: SEGFRY
 */
package vn.com.zalopay.wallet.business.webview.linkacc;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.List;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.business.entity.enumeration.ELinkAccType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptInput;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptOutput;
import vn.com.zalopay.wallet.business.entity.linkacc.DResponse;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.utils.GsonUtils;

/**
 * @author YenNLH
 */
public class LinkAccWebViewClient extends PaymentWebViewClient {
    public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
    public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
    public static final int TIME_REQUEST_RETRY_LOADING_AGAIN = 10000; // 10s
    public static final int TIME_WAITING_LOAD_AJAX_GET_RESULT = 5000; // 5s
    public static final int TIME_WAITING_LOAD_AJAX_GET_MESSAGE = 200; // 200ms
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
    ;
    private String mCurrentUrlPattern = null;
    private String mStartedtUrl = null;
    private String mCurrentUrl = null;
    private String mUrl = null;

    private long mLastStartPageTime = 0;
    private Handler mHandler = new Handler();

    private int mEventID = 0;
    private String mPageCode = null;

    private boolean mIsFirst = true;

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
            mWebPaymentBridge = new LinkAccWebView(mAdapter.getActivity().getBaseContext());
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

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d("TAG", "///// onLoadResource: " + url);
//        // check form OTP show, to know isOTPShow()
//        if (url.matches("^.+(vietcombank\\.com\\.vn).+(\\/ViDienTu\\/CheckCaptcha).*$")) {
//            onPageFinishedAuto(url);
//            return;
//        }

        if (url.matches("^.+(vietcombank\\.com\\.vn).+(\\/Account\\/GenerateCaptcha).*$")) {
//            onPageFinishedAjax(url, TIME_WAITING_LOAD_AJAX_GET_MESSAGE); // delay 200ms for wait run ajax, to get status message. Confirm info
            onPageFinishedAuto(url);
            return;
        }

        if (url.matches("^.+(vietcombank\\.com\\.vn).+(\\/ViDienTu\\/DangKySuDung).*$")) {
//            onPageFinishedAjax(url, TIME_WAITING_LOAD_AJAX_GET_RESULT); // wait run ajax, to get status message. Confirm OTP
            onPageFinishedAuto(url);
            return;
        }

        if (url.matches("^.+(vietcombank\\.com\\.vn).+(\\/ViDienTu\\/NgungSuDungViDienTu).*$")) {
//            onPageFinishedAjax(url, TIME_WAITING_LOAD_AJAX_GET_RESULT); // wait run ajax, to get status message. Confirm OTP
            onPageFinishedAuto(url);
            return;
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
        Log.e("Error", "++++ Current error SSL on page: " + mStartedtUrl);
        handler.proceed(); // Ignore SSL certificate errors
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.e("///// onReceivedError: ", description);
        mAdapter.onEvent(EEventType.ON_FAIL);
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
            input.linkerType = (mAdapter.getLinkerType() == ELinkAccType.LINK ? 1 : 0); // 1. isLink, other. isUnLink
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
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && url.matches(bankScript.url)) {
                Log.d("WebView", "$$$$$$ matchAndRunJs: " + url + " ,type: " + pType);
                isMatched = true;


                mCurrentUrl = url;
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
                DResponse response = genResponse(eventType, scriptOutput);

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

    public DResponse genResponse(EEventType pEventType, DLinkAccScriptOutput pScriptOutput) {
        DResponse ret = null;
        switch (pEventType) {
            default:
                ret = new DResponse();
                ret.returnMessage = pScriptOutput.message;
                break;
        }

        if (ret != null) {
            if (!pScriptOutput.isError()) {
                ret.returnCode = 4;
            } else {
                ret.returnCode = -4;
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
