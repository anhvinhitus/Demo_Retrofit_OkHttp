/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.inappbilling.GoogleAdapter.java
 * Created date: Dec 22, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.inappbilling;

import android.annotation.SuppressLint;
import android.content.Intent;

import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.DResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.google.DGoogleIabCreateOrderResponse;
import vn.zing.pay.zmpsdk.entity.google.DPayload;
import vn.zing.pay.zmpsdk.helper.google.IabHelper;
import vn.zing.pay.zmpsdk.helper.google.IabResult;
import vn.zing.pay.zmpsdk.helper.google.Purchase;
import vn.zing.pay.zmpsdk.helper.google.SkuDetails;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;

/**
 * @author YenNLH
 * 
 */
public class AdapterGoogleInappBilling extends AdapterBase {

	private GoogleIABService mGoogleIAPService = null;
	private DGoogleIabCreateOrderResponse mCreateOrderResponse = null;

	private IabResult mIabResult = null;
	private Purchase mIabPurchase = null;
	private SkuDetails mSkuDetails = null;

	public AdapterGoogleInappBilling(PaymentChannelActivity pOwnerActivity) {
		super(pOwnerActivity);
	}

	@Override
	public void init() {
	}

	@Override
	public boolean isStartImmediately() {
		return true;
	}

	@Override
	public void onClickSubmission() {
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getLayoutID() {
		return null;
	}

	@Override
	public void onFinish() {
		if (mGoogleIAPService != null) {
			mGoogleIAPService.destroy();
			mGoogleIAPService = null;
		}
	}

	@Override
	public String getChannelID() {
		String id = GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_google_wallet);
		return id;
	}

	@Override
	public String getChannelName() {
		return getChannelID();
	}

	@Override
	public void startPurchaseFlow() {
		try {
			DialogManager.showProcessDialog(null, GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing));

			// Start to get detail information about purchase item.
			if (mGoogleIAPService == null) {
				mGoogleIAPService = new GoogleIABService(this);
			}
			mGoogleIAPService.startSetup();
		} catch (Exception e) {
			Log.e(this, e);
		}
	}

	/*******************************************************/
	/*******************************************************/
	/*******************************************************/

	@Override
	public synchronized Object onEvent(EEventType pEventType, Object... pAdditionParams) {

		// ////// LOGGING /////////
		logOnEvent(pEventType, pAdditionParams);

		// ///////////////////////////
		// Close processing dialog //
		DialogManager.closeProcessDialog();

		/*********************************************************
		 * ************* PROCESS EVERYEVENTS HERE ************** *
		 *********************************************************/

		// //////////////////////////////
		// FAIL IN SETUP SERVICE STEP //
		if (pEventType == EEventType.ON_SEPTUP_FAIL) {
			terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_not_support_device));
			return null;
		}
		// ///////////////////////////////////////////////////
		// // CONSUME THIS ITEM IF IT IS PURCHASED BEFORE ////
		else if (pEventType == EEventType.ON_CONSUMPTION) {
			if (getResultAndPurchase(true, pAdditionParams)) {
				if (mSkuDetails != null) {
					// Finished to get SKUDetail, go ahead to create order on
					// server side
					TGoogleIABCreateOrderTask createOrderTask = new TGoogleIABCreateOrderTask(this);
					createOrderTask.execute();
				} else {
					terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_google_iab_item_not_found));
				}
			} else {
				terminateAndShowDialog(null);
			}

			// ////////////////////////
			// // CREATE ODER STEP ////
		} else if (pEventType == EEventType.ON_CREATE_ORDER_COMPLETED) {
			if (pAdditionParams == null || pAdditionParams.length < 1) {
				terminateAndShowDialog(null);
			} else {
				mCreateOrderResponse = (DGoogleIabCreateOrderResponse) pAdditionParams[0];

				if (mCreateOrderResponse.returnCode < 0) {
					terminateAndShowDialog(mCreateOrderResponse.returnMessage);
				} else {
					// Order created successfully, launch furchase flow of
					// Google IAB
					mGoogleIAPService.launchPurchaseFlow(getPayLoadString());
				}
			}

			// ///////////////////////////////////////////////
			// // ACTIVITY RESULT AFTER MAKING A PURCHASE ////
		} else if (pEventType == EEventType.ON_ACTIVITY_RESULT && pAdditionParams != null
				&& pAdditionParams.length == 3) {
			mGoogleIAPService.onActivityResult((int) pAdditionParams[0], (int) pAdditionParams[1],
					(Intent) pAdditionParams[2]);

			// /////////////////////////////////////////////////////
			// // ORDER IS COMPLETED, RECEIVE THE PURCHASE INFO ////
		} else if (pEventType == EEventType.ON_PURCHASED) {
			if (getResultAndPurchase(false, pAdditionParams)) {

				// // PURCHASE COMPLETED ////
				if (mIabResult.isSuccess()) {

					verifyReceipt();

					// // USER CANCELED ////
				} else if (mIabResult.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED
						|| mIabResult.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
					this.mOwnerActivity.finish();

					// ////////////////////
					// Unknown exception //
				} else {
					terminateAndShowDialog(null);
				}
			}

			// /////////////////////////////////////
			// // VERIFIRYING STEP IS COMPLETED ////
			// // GET STATUS OF THIS TRANSACTION ////
			// //////////////////////////////////////
		} else if (pEventType == EEventType.ON_VERIFY_COMPLETED) {
			if (pAdditionParams != null && pAdditionParams.length == 1) {
				DResponse response = (DResponse) (pAdditionParams[0]);
				if (response.returnCode >= 1) {
					getStatus(mCreateOrderResponse.zmpTransID);
				} else {
					Log.e(this, response.toJsonString());
					terminateAndShowDialog(response.returnMessage);
				}

			} else {

				// /////////////////////////////////////
				// // VERIFIRYING IS NOT SUCCESSFUL ////
				terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_network_error_verify_iab));
			}

		}
		return null;
	}

	/**
	 * Start to verify the receipt given by Google Play on server side
	 */
	private void verifyReceipt() {
		TGoogleIABVerifyReceiptTask verifyReceiptTask = new TGoogleIABVerifyReceiptTask(this, String.valueOf(GlobalData
				.getPaymentInfo().appID), mIabPurchase.getOriginalJson(), mIabPurchase.getSignature(),
				mCreateOrderResponse.zmpTransID, getPayLoadString());
		verifyReceiptTask.execute();
	}

	private boolean getResultAndPurchase(boolean isAutoFinish, Object... pAdditionParams) {
		if (pAdditionParams == null || pAdditionParams.length == 0) {
			terminateAndShowDialog(null);
			return false;
		} else if (pAdditionParams.length == 1) {
			mIabResult = (IabResult) pAdditionParams[0];
		} else {
			mIabResult = (IabResult) pAdditionParams[0];

			if (pAdditionParams[1] instanceof Purchase) {
				mIabPurchase = (Purchase) pAdditionParams[1];
				GlobalData.paymentResult.purchase = mIabPurchase;
			} else if (pAdditionParams[1] instanceof SkuDetails) {
				mSkuDetails = (SkuDetails) pAdditionParams[1];
			}
		}
		GlobalData.paymentResult.iabResult = mIabResult;

		if (isAutoFinish && (mIabResult == null || mIabResult.isFailure())) {
			terminateAndShowDialog(null);
			return false;
		}
		return true;
	}

	/*******************************************************/
	/*******************************************************/
	/*******************************************************/

	@SuppressLint("DefaultLocale")
	public String getPayLoadString() {
		if (mCreateOrderResponse == null)
			return null;

		DPayload payload = new DPayload();
		payload.amount = GlobalData.getPaymentInfo().amount;
		payload.amountMicro = mSkuDetails.getPriceMicros();
		payload.currency = mSkuDetails.getCurrency();
		payload.orgAmount = mSkuDetails.getPrice();
		payload.zmpTransID = mCreateOrderResponse.zmpTransID;

		return payload.toJsonString();
		// "" + mCreateOrderResponse.zmpTransID +
		// GlobalData.getPaymentInfo().amount; //
	}

	public DGoogleIabCreateOrderResponse getCreateOrderResponse() {
		return mCreateOrderResponse;
	}

	public SkuDetails getSkuDetails() {
		return mSkuDetails;
	}
}
