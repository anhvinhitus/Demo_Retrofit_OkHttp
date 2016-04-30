/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.AtmGuiProcessor.java
 * Created date: Jan 14, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.atm;

import android.text.TextUtils;
import vn.zing.pay.zmpsdk.analysis.JavaInstanceTracker;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.atm.webview.WebPaymentBridge;
import vn.zing.pay.zmpsdk.business.atm.webview.WebViewProcessor;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.entity.DResponse;
import vn.zing.pay.zmpsdk.entity.atm.DAtmCardCache;
import vn.zing.pay.zmpsdk.entity.atm.DAtmCreateOrderResponse;
import vn.zing.pay.zmpsdk.entity.atm.DAtmScriptOutput;
import vn.zing.pay.zmpsdk.entity.atm.DAtmSubmitCardResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentReturnCode;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DGroupPaymentChannel;
import vn.zing.pay.zmpsdk.utils.DeviceUtil;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.utils.StringUtil;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;

/**
 * @author YenNLH
 * 
 */
public class AdapterATM extends AdapterBase {

	public static final String PAGE_OTP = "zpsdk_atm_otp_normal";
	public static final String PAGE_CARD_INFO = "zpsdk_atm_card_info";

	private long mPriceMultiple = 0;
	private long mBackupAmount = 0;
	private DGroupPaymentChannel mConfig;

	private String mPageCode = PAGE_CARD_INFO;
	private EEventType mLastEventType = null;
	private boolean mIsGetBankListFinished = false;

	private AtmGuiProcessor mGuiProcessor = null;
	private WebViewProcessor mWebViewProcessor = null;
	private int mMinNumOfCaptcha = 4;
	private int mMinNumOfOtp = 4;

	private long mStartingTime = 0;
	private DAtmCreateOrderResponse mCreateOrderResponse = null;
	protected String mBankCode = null;

	public AdapterATM(PaymentChannelActivity pOwnerActivity) {
		super(pOwnerActivity);

		try {
			// Because we will render a large of bitmap
			System.gc();

			mBackupAmount = GlobalData.getPaymentInfo().amount;
			mPriceMultiple = Long.parseLong(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_price_multiple));
			mConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getATMGChannelConfig(),
					DGroupPaymentChannel.class);
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}

	@Override
	public void init() {
		mGuiProcessor = new AtmGuiProcessor(mOwnerActivity, this);

		try {
			mMinNumOfCaptcha = Integer.parseInt(GlobalData
					.getStringResource(Resource.string.zingpaysdk_conf_atm_api_num_of_captcha));
			mMinNumOfOtp = Integer.parseInt(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_atm_api_num_of_otp));
		} catch (Exception ex) {
			Log.e(this, ex);
		}

		if (mIsGetBankListFinished) {
			mGuiProcessor.resetPmc();
			mGuiProcessor.checkCardCache();
		}
	}

	@Override
	public String getChannelID() {
		return GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_atm);
	}

	@Override
	public String getChannelName() {
		return getChannelID() + "|" + getBankCode();
	}

	public String getBankCode() {
		return mBankCode;
	}

	public void setPageName(String pPageName) {
		mPageCode = pPageName;
	}

	@Override
	public String getPageName() {
		return mPageCode;
	}

	@Override
	public String getLayoutID() {
		return Resource.layout.zpsdk_atm;
	}

	@Override
	public boolean isStartImmediately() {
		mIsGetBankListFinished = TAtmGetBankListTask.isFinished();
		return !mIsGetBankListFinished;
	}

	@Override
	public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
		try {
			// //////////
			// LOGING //
			logOnEvent(pEventType, pAdditionParams);
			
			if (pEventType == EEventType.ON_RESUME) {
				return null;
			}
			
			mLastEventType = pEventType;
			DialogManager.closeProcessDialog();

			if (pEventType == EEventType.ON_SELECT) {
				mGuiProcessor.onSelectBank((VPaymentChannelButton) pAdditionParams[0]);
				// Re-create order
				mLastEventType = null;
				mCreateOrderResponse = null;

				// //////////////////////////////////
				// GET ACTIVE BANK LIST COMPLETED //
				// //////////////////////////////////
			} else if (pEventType == EEventType.ON_GET_BANK_LIST_COMPLETED) {
				mOwnerActivity.renderByResource();
				mGuiProcessor.resetPmc();
				mGuiProcessor.checkCardCache();

				// /////////////////////////////////////////////////
				// FAIL TO PROCESS CURRENT TRANSACTION, TRY LATER //
				// /////////////////////////////////////////////////
			} else if (pEventType == EEventType.ON_FAIL) {

				if (pAdditionParams == null || pAdditionParams.length == 0) {
					// Error
					terminateAndShowDialog(null);
				} else {
					terminateAndShowDialog(((DResponse) pAdditionParams[0]).returnMessage);
				}

				// //////////////////////////
				// CREATE ORDER COMPLETED //
				// //////////////////////////
			} else if (pEventType == EEventType.ON_CREATE_ORDER_COMPLETED) {
				if (pAdditionParams == null || pAdditionParams.length == 0) {
					// Error
					DialogManager.showAlertDialog(null);
				} else {
					mCreateOrderResponse = (DAtmCreateOrderResponse) pAdditionParams[0];

					if (mCreateOrderResponse.returnCode > 1) {
						mStartingTime = System.currentTimeMillis();

						if (mCreateOrderResponse.src.equals(Constants.BANK_PAYMENT_TYPE.API)) {
							TAtmVerifyCardTask task = new TAtmVerifyCardTask(this, mCreateOrderResponse.zmpTransID,
									mGuiProcessor.getCardHolder(), mGuiProcessor.getCardNumber(),
									mGuiProcessor.getCardPass(), mGuiProcessor.getCardMonth(),
									mGuiProcessor.getCardYear());
							task.execute();
						} else {
							initWebView(mCreateOrderResponse.redirectUrl);
						}
					} else {
						DialogManager.showAlertDialog(mCreateOrderResponse.returnMessage);
						mLastEventType = null;
					}
				}

				// ////////////////////////
				// VERIFY CARD COMPLETED //
				// ////////////////////////
			} else if (pEventType == EEventType.ON_VERIFY_COMPLETED) {
				if (pAdditionParams == null || pAdditionParams.length == 0) {
					// Error
					DialogManager.showAlertDialog(null);
				} else {
					DAtmSubmitCardResponse verifyCardResponse = (DAtmSubmitCardResponse) pAdditionParams[0];

					// Reset captcha imediately
					if (!TextUtils.isEmpty(verifyCardResponse.captcha)) {
						mGuiProcessor.setCaptchaImage(verifyCardResponse.captcha);
					}

					// TODO: Fix here
					if (verifyCardResponse.returnCode > 1) {
						storeCardInfo();

						if (verifyCardResponse.src.equals(Constants.BANK_PAYMENT_TYPE.API)) {
							setPageName(PAGE_OTP);
							mOwnerActivity.renderByResource();
							mGuiProcessor.checkEnableSubmitButton();
						} else {
							// TODO: Smartlink
							initWebView(verifyCardResponse.redirectUrl);
						}
					} else {
						DialogManager.showAlertDialog(verifyCardResponse.returnMessage);
						mLastEventType = null;
					}
				}

				// ////////////////////////////////////////
				// PAYMENT COMPLETED AFTER VERIFYING OTP //
				// ////////////////////////////////////////
			} else if (pEventType == EEventType.ON_PAYMENT_COMPLETED) {
				if (pAdditionParams == null || pAdditionParams.length == 0) {
					// Error
					DialogManager.showAlertDialog(null);
				} else {
					DAtmSubmitCardResponse response = (DAtmSubmitCardResponse) pAdditionParams[0];
					if (response.returnCode == EPaymentReturnCode.ATM_VERIFY_OTP_SUCCESS.getValue()) {
						// /// GET STATUS NOW /////
						getStatus(mCreateOrderResponse.zmpTransID);

					} else if (response.returnCode == EPaymentReturnCode.ATM_RETRY_CAPTCHA.getValue()
							|| response.returnCode == EPaymentReturnCode.ATM_RETRY_OTP.getValue()) {
						setPageName(PAGE_OTP);

						// Reset captcha imediately
						if (!TextUtils.isEmpty(response.captcha)) {
							mGuiProcessor.setCaptchaImage(response.captcha);
						}

						if (!TextUtils.isEmpty(response.returnMessage)) {
							DialogManager.showAlertDialog(response.returnMessage);
						}

						mOwnerActivity.renderByResource();
						mGuiProcessor.checkEnableSubmitButton();
					} else {
						terminateAndShowDialog(response.returnMessage);
					}
				}

				// ////////////////////////////////////////////////
				// MET A NEW WEBPAGE, NEED TO RE-RENDER ACTIVITY //
				// ////////////////////////////////////////////////
			} else if (pEventType == EEventType.ON_REQUIRE_RENDER) {
				if (pAdditionParams == null || pAdditionParams.length == 0) {
					// Error
					DialogManager.showAlertDialog(null);
				} else {
					DAtmScriptOutput response = (DAtmScriptOutput) pAdditionParams[0];

					// Reset captcha imediately
					if (!TextUtils.isEmpty(response.otpimg)) {
						mGuiProcessor.setCaptchaImage(response.otpimg, response.otpimgsrc);
					}

					// Re-render
					if (pAdditionParams.length > 1 && !PAGE_CARD_INFO.equalsIgnoreCase((String) pAdditionParams[1])) {
						mPageCode = (String) pAdditionParams[1];
						mOwnerActivity.renderByResource(response.staticView, response.dynamicView);
						mGuiProcessor.renderItemList(response.itemList);
						mGuiProcessor.enableChargeAmount(false);
						mGuiProcessor.checkEnableSubmitButton();
					}

					if (!response.isError()) {
						if (!TextUtils.isEmpty(response.info)) {
							DialogManager.showAlertDialog(response.info);
						}

						// Verification step was completed, store this card for
						// the next payment
						if (mWebViewProcessor.isVerifyCardComplete()) {
							storeCardInfo();
						}
					} else {
						DialogManager.showAlertDialog(response.message);
					}
				}
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}

		return null;
	}

	@Override
	public void onFinish() {
		if (mWebViewProcessor != null) {
			mWebViewProcessor.dispose();
			mWebViewProcessor = null;
		}
		mGuiProcessor.dispose();
		mGuiProcessor = null;
		mOwnerActivity = null;
		// Because we will render a large of bitmap
		System.gc();

		// Restore original
		if (!mIsSuccess) {
			GlobalData.getPaymentInfo().amount = mBackupAmount;
		}
	}

	@Override
	public void startPurchaseFlow() {
		DialogManager.showProcessDialog(null, null);
		TAtmGetBankListTask.setAdapter(this);
	}

	@Override
	public void onClickSubmission() {
		if (checkExpired()) {
			return;
		}

		GlobalData.getPaymentInfo().amount = mGuiProcessor.getAmount();
		if (GlobalData.getPaymentInfo().amount % mPriceMultiple != 0) {
			DialogManager.showAlertDialog(String.format(
					GlobalData.getStringResource(Resource.string.zingpaysdk_alert_price_multiple),
					StringUtil.longToStringNoDecimal(mPriceMultiple)));
			return;
		}

		if (GlobalData.getPaymentInfo().amount < mConfig.minPPValue) {
			DialogManager.showAlertDialog(String.format(
					GlobalData.getStringResource(Resource.string.zingpaysdk_alert_lower_min_value),
					StringUtil.longToStringNoDecimal(mConfig.minPPValue)));
			return;
		}

		if (GlobalData.getPaymentInfo().amount > mConfig.maxPPValue) {
			DialogManager.showAlertDialog(String.format(
					GlobalData.getStringResource(Resource.string.zingpaysdk_alert_higher_max_value),
					StringUtil.longToStringNoDecimal(mConfig.minPPValue)));
			return;
		}

		// API flow
		if (mLastEventType == EEventType.ON_VERIFY_COMPLETED || mLastEventType == EEventType.ON_PAYMENT_COMPLETED) {
			// Check minimum of characters of OTP and CAPTCHA
			if (mGuiProcessor.getOtp().length() < mMinNumOfOtp) {
				DialogManager.showAlertDialog(GlobalData
						.getStringResource(Resource.string.zingpaysdk_alert_atm_incomplete_otp));
				return;
			} else if (mGuiProcessor.getCaptcha().length() < mMinNumOfCaptcha) {
				DialogManager.showAlertDialog(GlobalData
						.getStringResource(Resource.string.zingpaysdk_alert_atm_incomplete_captcha));
				return;
			}

			TAtmVerifyOtpTask verifyOtpTask = new TAtmVerifyOtpTask(this, mCreateOrderResponse.zmpTransID,
					mGuiProcessor.getCaptcha(), mGuiProcessor.getOtp());
			verifyOtpTask.execute();

		} else if (mLastEventType == null) {

			// //////////////////
			// CACHED ATM CARD //
			// //////////////////
			if (mGuiProcessor.validateCardCache()) {
				SharedPreferencesManager.getInstance().setNumOfWrong4LastDigits(0);
				// Create order
				TAtmCreateOrder atmCreateOrder = new TAtmCreateOrder(this);
				atmCreateOrder.execute();

				// Tracking the new page for a new bankcode
				GlobalData.getDefaultTracker().trackScreen(getSimpleName() + "~" + mBankCode, false);
			} else {
				int numOfRetry = SharedPreferencesManager.getInstance().getNumOfWrong4LastDigits();
				numOfRetry++;
				SharedPreferencesManager.getInstance().setNumOfWrong4LastDigits(numOfRetry);

				// Check if malicious user is trying to make an unauthorized
				// purchase
				if (numOfRetry < 4) {
					DialogManager.showAlertDialog(GlobalData
							.getStringResource(Resource.string.zingpaysdk_alert_atm_wrong_4last_digits));
				} else {
					// Busted
					terminateAndShowDialog(GlobalData
							.getStringResource(Resource.string.zingpaysdk_alert_atm_wrong_4last_digits_more));
					// Delete cached card
					SharedPreferencesManager.getInstance().setCardInfo(null);
				}
			}
		} else if (mWebViewProcessor != null) {
			// Webview flow
			mWebViewProcessor.hit();
		}
	}

	public AtmGuiProcessor getCreateOrderGuiProcessor() {
		return mGuiProcessor;
	}

	public WebViewProcessor getWebViewProcessor() {
		return mWebViewProcessor;
	}

	private void initWebView(String pUrl) {
		if (mWebViewProcessor == null) {
			if (Constants.IS_DEV) {
				mWebViewProcessor = new WebViewProcessor(this,
						(WebPaymentBridge) mOwnerActivity.findViewById(Resource.id.webView));
			} else {
				mWebViewProcessor = new WebViewProcessor(this);
			}
			JavaInstanceTracker.observe(mWebViewProcessor);
		}
		mWebViewProcessor.start(pUrl);

		if (Constants.IS_DEV) {
			DeviceUtil.copyToClipboard(mOwnerActivity, pUrl);
		}
	}

	private boolean checkExpired() {
		if (mStartingTime > 0 && ((int) (System.currentTimeMillis() - mStartingTime) > Constants.MAX_INTERVAL_OF_ATM)) {
			terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_atm_expired));
			return true;
		}
		return false;
	}

	private void storeCardInfo() {
		DAtmCardCache cache = new DAtmCardCache();
		cache.bankCode = mBankCode;
		cache.cardHolderName = mGuiProcessor.getCardHolder();
		cache.cardNumber = mGuiProcessor.getCardNumber();
		cache.cardMonth = mGuiProcessor.getCardMonth();
		cache.cardYear = mGuiProcessor.getCardYear();

		SharedPreferencesManager.getInstance().setCardInfo(cache);
	}
}
