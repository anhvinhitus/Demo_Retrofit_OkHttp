/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.business.atm.webview.TWebviewErrorLogSubmisssion.java
 * Created date: Feb 17, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.atm.webview;

import java.io.UnsupportedEncodingException;

import android.text.TextUtils;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.entity.DResponse;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * @author YenNLH
 * 
 */
public class TWebviewErrorLogSubmisssion extends TAbtractPaymentTask {

	private WebViewLogger mLogger = null;

	public TWebviewErrorLogSubmisssion(AdapterBase pAdapter, WebViewLogger pWebViewLogger) {
		super(pAdapter);
		mLogger = pWebViewLogger;
	}

	@Override
	protected void onPreExecute() {
		// Do nothing
	}

	@Override
	protected String doInBackground(Void... params) {

		HttpClientRequest request = new HttpClientRequest(Type.POST, Constants.getUrlPrefix()
				+ Constants.URL_ATM_SUBMIT_LOG);
		try {
			putPaymentInfo(request);
			request.addParams("bankCode", mLogger.getBankCode());
			request.addParams("exception", mLogger.getHistory());
		} catch (UnsupportedEncodingException e) {
			Log.e(this, e);
		}

		String response = null;
		int count = 0;
		while (true) {
			response = request.getText();

			if (checkSendSuccess(response)) {
				break;
			} else {
				count++;
				if (count < 10) {
					try {
						Thread.sleep(Constants.SLEEPING_INTERVAL_OF_RETRY);
					} catch (InterruptedException e) {
						Log.e(this, e);
						break;
					}
				} else {
					break;
				}
			}
		}

		GlobalData.getDefaultTracker().trackEvent(mLogger.getBankCode(), mLogger.getHistory(),
				Constants.TRACKING.EXCEPTION, count);

		return null;
	}

	protected boolean checkSendSuccess(String pResponse) {
		if (!TextUtils.isEmpty(pResponse)) {
			DResponse response = GsonUtils.fromJsonString(pResponse, DResponse.class);
			if (response.returnCode == 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPostExecute(String result) {
		// Do nothing
	}
}
