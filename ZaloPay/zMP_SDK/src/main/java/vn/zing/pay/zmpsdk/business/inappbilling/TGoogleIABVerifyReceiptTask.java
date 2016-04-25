/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.inappbilling.TGoogleIABVerifyReceiptTask.java
 * Created date: Dec 24, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.inappbilling;

import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.sqllite.GoogleIABReceiptDataSource;
import vn.zing.pay.zmpsdk.entity.DResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.google.DGoogleIabReceipt;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.DeviceUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.content.Context;
import android.text.TextUtils;

/**
 * @author YenNLH
 * 
 */
public class TGoogleIABVerifyReceiptTask extends TAbtractPaymentTask {
	private String mVerifyReceiptURL = Constants.getUrlPrefix() + Constants.URL_GIAB_VERIFY_RECEIPT;

	private Context mContext;
	private String mAppID;
	private String mReceipt;
	private String mSignature;
	private String mZmpTransID;
	private String mDeveloperPayload;
	private boolean mIsRetry = false;

	public TGoogleIABVerifyReceiptTask(AdapterGoogleInappBilling pAdapterGoogleInappBilling, String pAppID,
			String pReceipt, String pSignature, String pZmpTransID, String pDeveloperPayload) {
		super(pAdapterGoogleInappBilling);

		if (mAdapter != null)
			this.mContext = mAdapter.getOwnerActivity();
		this.mAppID = pAppID;
		this.mReceipt = pReceipt;
		this.mSignature = pSignature;
		this.mZmpTransID = pZmpTransID;
		this.mDeveloperPayload = pDeveloperPayload;
	}

	public void setIsRetry(Context pContext) {
		mIsRetry = true;
		mContext = pContext;
	}

	@Override
	protected String doInBackground(Void... params) {
		boolean procesingChange1 = false, procesingChange2 = false;
		long startTime = System.currentTimeMillis();
		String result = null;

		HttpClientRequest request = new HttpClientRequest(Type.POST, mVerifyReceiptURL);
		request.addParams("appID", mAppID);
		request.addParams("zmpTransID", String.valueOf(mZmpTransID));
		request.addParams("deviceID", DeviceUtil.getUniqueDeviceID(mContext));
		request.addParams("receipt", mReceipt);
		request.addParams("developerPayload", mDeveloperPayload);
		request.addParams("sig", mSignature);
		request.addParams("version", Constants.VERSION);

		while (System.currentTimeMillis() - startTime <= Constants.MAX_INTERVAL_OF_RETRY) {

			result = request.getText();

			if (!TextUtils.isEmpty(result)) {
				break;
			} else {

				// Change message text but retrying case
				if (!mIsRetry) {
					if (!procesingChange1
							&& (System.currentTimeMillis() - startTime >= (Constants.MAX_INTERVAL_OF_RETRY / 3))) {
						procesingChange1 = true;
						DialogManager.changeMessageProcessingDialog(GlobalData
								.getStringResource(Resource.string.zingpaysdk_alert_continue_processing));
					}

					if (!procesingChange2
							&& (System.currentTimeMillis() - startTime >= (Constants.MAX_INTERVAL_OF_RETRY * 2 / 3))) {
						procesingChange2 = true;
						DialogManager.changeMessageProcessingDialog(GlobalData
								.getStringResource(Resource.string.zingpaysdk_alert_bad_processing));
					}
				}

				try {
					Thread.sleep(Constants.SLEEPING_INTERVAL_OF_RETRY);
				} catch (InterruptedException e) {
					Log.e(this, e);
				}
			}
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (!TextUtils.isEmpty(result)) {
			if (mAdapter != null) {
				DResponse response = (new DResponse()).fromJsonString(result);
				mAdapter.onEvent(EEventType.ON_VERIFY_COMPLETED, response);

				// TODO: Remove this
				if (Log.IS_LOG_ENABLE) {
					DialogManager.showAlertDialog(result);
				}
			}

			// //////////////////////////////////////////
			// DELETE SUCCESSFUL VERIFYING SUBMISSION //
			if (mIsRetry) {
				GoogleIABReceiptDataSource dataSource = new GoogleIABReceiptDataSource(mContext);
				dataSource.deleteReceipt(mZmpTransID);
			}
		} else {
			Log.e(this, "**** TGoogleIABVerifyReceiptTask return NULL ****");

			// /////////////////////////////////////
			// // VERIFIRYING IS NOT SUCCESSFUL ////

			GoogleIABReceiptDataSource dataSource = new GoogleIABReceiptDataSource(mContext);
			if (mIsRetry) {
				DGoogleIabReceipt receipt = dataSource.getReceipt(mZmpTransID);
				receipt.retryCount++;
				dataSource.updateTransaction(receipt);
			} else {
				// Save transaction to DB
				DGoogleIabReceipt iabReceipt = new DGoogleIabReceipt(mReceipt, mZmpTransID, mSignature,
						mDeveloperPayload, String.valueOf(GlobalData.getPaymentInfo().appID));
				dataSource.insertReceipt(iabReceipt);

				Log.e(this, "#### ERORR VERIFIRYING ++ INSERT DB: " + dataSource.getReceipt(mZmpTransID).toJsonString());
				Log.e(this, iabReceipt.toJsonString());

				// Start to retry
				ZingMobilePayApplication.retryGoogleInAppBilling(mContext,
						Constants.SLEEPING_INTERVAL_OF_GIAB_VERIFY_RETRY);

				mAdapter.onEvent(EEventType.ON_VERIFY_COMPLETED);
			}
		}
	}
}
