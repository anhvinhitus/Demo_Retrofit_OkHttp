/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.AbstractWebViewProcessor.java
 * Created date: Jan 15, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.atm.webview;

import java.util.List;

import vn.zing.pay.zmpsdk.business.atm.AdapterATM;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.entity.DResponse;
import vn.zing.pay.zmpsdk.entity.atm.DAtmCreateOrderResponse;
import vn.zing.pay.zmpsdk.entity.atm.DAtmScriptInput;
import vn.zing.pay.zmpsdk.entity.atm.DAtmScriptOutput;
import vn.zing.pay.zmpsdk.entity.atm.DAtmSubmitCardResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentReturnCode;
import vn.zing.pay.zmpsdk.entity.staticconfig.bank.DBankScript;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author YenNLH
 * 
 */
public class WebViewProcessor extends WebViewClient {

	public static final String JAVA_SCRIPT_INTERFACE_NAME = "zingpaysdk_wv";
	public static final long DELAY_TIME_TO_DETECT_AJAX = 8000;
	public static final int IGNORE_EVENT_ID_FOR_HTTPS = -2; // This event id
															// value is used for
															// detect valid url
															// in the case
															// webview on
															// Android 2.3

	public static enum EJavaScriptType {
		AUTO, HIT
	};

	private WebViewLogger mLogger = null;
	private boolean mIsLoadingFinished = true;
	private boolean mIsRedirect = false;

	private AdapterATM mAdapterATM = null;
	private WebPaymentBridge mWebPaymentBridge = null;

	private List<DBankScript> mBankScripts = ResourceManager.getInstance(null).getBankScripts();
	private String mCurrentUrlPattern = null;
	private String mStartedtUrl = null;
	private String mCurrentUrl = null;

	private long mLastStartPageTime = 0;
	private Handler mHandler = new Handler();

	private int mEventID = 0;
	private String mPageCode = null;

	private boolean mIsFirst = true;

	public WebViewProcessor(AdapterATM pAdapterATM) {
		if (pAdapterATM != null) {
			mAdapterATM = pAdapterATM;
			mLogger = new WebViewLogger(mAdapterATM.getBankCode());
			// Avoid memory-leak in WebView:
			// http://stackoverflow.com/questions/3130654/memory-leak-in-webview
			mWebPaymentBridge = new WebPaymentBridge(mAdapterATM.getOwnerActivity().getApplicationContext());
			mWebPaymentBridge.setWebViewClient(this);
			mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
		}
	}

	public WebViewProcessor(AdapterATM pAdapterATM, WebPaymentBridge pWebPaymentBridge) {
		if (pAdapterATM != null) {
			mAdapterATM = pAdapterATM;
			mLogger = new WebViewLogger(mAdapterATM.getBankCode());
			mWebPaymentBridge = pWebPaymentBridge;
			mWebPaymentBridge.setWebViewClient(this);
			mWebPaymentBridge.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
		}
	}

	public void start(String pUrl) {
		DialogManager.showProcessDialog(null, GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing_bank),
				mAdapterATM);
		mWebPaymentBridge.loadUrl(pUrl);
		mIsFirst = true;
	}

	public void hit() {
		DialogManager.showProcessDialog(null, GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing_bank),
				mAdapterATM);
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

		if (mAdapterATM != null) {

			if (mAdapterATM.getCreateOrderGuiProcessor() != null) {
				input.cardHolderName = mAdapterATM.getCreateOrderGuiProcessor().getCardHolder();
				input.cardNumber = mAdapterATM.getCreateOrderGuiProcessor().getCardNumber();
				input.cardMonth = mAdapterATM.getCreateOrderGuiProcessor().getCardMonth();
				input.cardYear = mAdapterATM.getCreateOrderGuiProcessor().getCardYear();
				input.cardPass = mAdapterATM.getCreateOrderGuiProcessor().getCardPass();
				input.otp = mAdapterATM.getCreateOrderGuiProcessor().getOtp();
				input.captcha = mAdapterATM.getCreateOrderGuiProcessor().getCaptcha();
				input.username = mAdapterATM.getCreateOrderGuiProcessor().getUsername();
				input.password = mAdapterATM.getCreateOrderGuiProcessor().getPassword();
				input.selectedItemOrderNum = mAdapterATM.getCreateOrderGuiProcessor().getSelectedItemOrderNumber();
			}

			return input;
		}
		return input;
	}

	public void matchAndRunJs(String url, EJavaScriptType pType, boolean pIsAjax) {
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

				String inputScript = input.toJsonString();

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
			mAdapterATM.onEvent(EEventType.ON_FAIL);
			submitErrorLog();
		}
	}

	public void executeJs(String pJsFileName, String pJsInput) {
		if (!TextUtils.isEmpty(pJsFileName)) {
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

		mLogger.travel(url);
		mStartedtUrl = url;
		mIsLoadingFinished = false;

		// Modify this variable to inform that it not run in ajax mode
		mLastStartPageTime++;
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
			Log.e(this, "=========== ALREADY FINISHED ===========");
			Log.e(this, url);
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
		Log.e(this, "++++ Current error SSL on page: " + mStartedtUrl);

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

		mAdapterATM.getOwnerActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				DAtmScriptOutput scriptOutput = GsonUtils.fromJsonString(result, DAtmScriptOutput.class);
				EEventType eventType = convertPageIdToEvent(mEventID);
				DResponse response = genResponse(eventType, scriptOutput);

				if (mEventID == 0 && mIsFirst && !scriptOutput.isError()) {
					// Auto hit at first step
					mIsFirst = false;
					hit();
				} else {
					if (eventType == EEventType.ON_REQUIRE_RENDER) {
						mAdapterATM.onEvent(EEventType.ON_REQUIRE_RENDER, scriptOutput, mPageCode);
					} else {
						mAdapterATM.onEvent(eventType, response, mPageCode, mEventID);

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
		case 3:
			return EEventType.ON_PAYMENT_COMPLETED;
		default:
			return EEventType.ON_REQUIRE_RENDER;
		}
	}

	public boolean isVerifyCardComplete() {
		return mEventID == 1;
	}

	public DResponse genResponse(EEventType pEventType, DAtmScriptOutput pScriptOutput) {
		DResponse ret = null;
		switch (pEventType) {

		case ON_CREATE_ORDER_COMPLETED:
			ret = new DAtmCreateOrderResponse();
			ret.returnMessage = pScriptOutput.message;
			((DAtmCreateOrderResponse) ret).src = Constants.BANK_PAYMENT_TYPE.SML;
			((DAtmCreateOrderResponse) ret).redirectUrl = mCurrentUrl;
			break;

		case ON_PAYMENT_COMPLETED:
			ret = new DAtmSubmitCardResponse();
			ret.returnMessage = pScriptOutput.message;
			ret.returnCode = EPaymentReturnCode.ATM_VERIFY_OTP_SUCCESS.getValue();
			return ret;

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

	public void submitErrorLog() {
		TWebviewErrorLogSubmisssion errorLogSubmisssion = new TWebviewErrorLogSubmisssion(mAdapterATM, mLogger);
		errorLogSubmisssion.execute();
	}

	public String getCurrentUrl() {
		return mWebPaymentBridge.getUrl();
	}
}
