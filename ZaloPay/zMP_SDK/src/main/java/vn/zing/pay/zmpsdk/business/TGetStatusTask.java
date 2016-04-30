/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.TGetStatusTask.java
 * Created date: Dec 24, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.DResponseGetStatus;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.DeviceUtil;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.text.TextUtils;

/**
 * @author YenNLH
 * 
 */
public class TGetStatusTask extends TAbtractPaymentTask {
	private String mUrl = Constants.getUrlPrefix() + Constants.URL_GET_STATUS;
	private String mTransID;

	public TGetStatusTask(AdapterBase pAdapter, String pTransID) {
		super(pAdapter);
		mTransID = pTransID;
	}

	@Override
	protected String doInBackground(Void... params) {
		boolean procesingChange1 = false, procesingChange2 = false;
		String result = null;
		Log.d(this, "Begin getting status of transID: " + mTransID);
		int sleepInterval = Constants.SLEEPING_INTERVAL_OF_RETRY;
		long startTime = System.currentTimeMillis();

		HttpClientRequest clientRequest = null;
		try {
			clientRequest = new HttpClientRequest(Type.POST, URLDecoder.decode(mUrl, "utf-8"));
			clientRequest.addParams("appID", String.valueOf(GlobalData.getPaymentInfo().appID));
			clientRequest.addParams("zmpTransID", mTransID);
			clientRequest.addParams("deviceID", DeviceUtil.getUniqueDeviceID(GlobalData.getApplication()));
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		DResponseGetStatus response = new DResponseGetStatus();
		try {
			while (System.currentTimeMillis() - startTime <= Constants.MAX_INTERVAL_OF_RETRY) {

				result = clientRequest.getText();
				if (!TextUtils.isEmpty(result)) {
					response = GsonUtils.fromJsonString(result, DResponseGetStatus.class);
				}

				if (response.isProcessing) {
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

					Thread.sleep(sleepInterval);
				} else {
					return result;
				}
			}

		} catch (Exception ex) {
			Log.e(this, ex);
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		DResponseGetStatus response = (!TextUtils.isEmpty(result)) ? (GsonUtils.fromJsonString(result,
				DResponseGetStatus.class)) : null;
		
		// Set amount to exactly value
		GlobalData.getPaymentInfo().amount = response.ppValue;
		// Call-back
		mAdapter.onGetStatusComplete(mTransID, response);
	}
}
