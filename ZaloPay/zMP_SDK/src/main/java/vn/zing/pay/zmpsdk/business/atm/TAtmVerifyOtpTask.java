/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.business.atm.TAtmVerifyOtpTask.java
 * Created date: Jan 25, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.atm;

import java.net.URLDecoder;

import android.text.TextUtils;

import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.entity.atm.DAtmSubmitCardResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;

/**
 * @author YenNLH
 * 
 */
public class TAtmVerifyOtpTask extends TAbtractPaymentTask {

	private String mZmpTransID = null;
	private String mCaptcha = null;
	private String mOtp = null;

	public TAtmVerifyOtpTask(AdapterBase adapter, String pZmpTransID, String pCaptcha, String pOtp) {
		super(adapter);

		mZmpTransID = pZmpTransID;
		mCaptcha = pCaptcha;
		mOtp = pOtp;
	}

	@Override
	protected String doInBackground(Void... params) {
		Log.d(this, "--- TAtmVerifyOtpTask ---");
		try {
			HttpClientRequest request = new HttpClientRequest(Type.GET, URLDecoder.decode(Constants.getUrlPrefix() + Constants.URL_ATM_VERIFY_OTP,
					"utf-8"));
			request.addParams("zmpTransID", mZmpTransID);
			request.addParams("captcha", mCaptcha);
			request.addParams("otp", mOtp);
			String result = request.getText();

			return result;
		} catch (Exception ex) {
			Log.e(this, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		DAtmSubmitCardResponse response = null;
		if (!TextUtils.isEmpty(result)) {
			response = GsonUtils.fromJsonString(result, DAtmSubmitCardResponse.class);

			// TODO: Remove this
			if (Log.IS_LOG_ENABLE) {
				DAtmSubmitCardResponse response2 = GsonUtils.fromJsonString(result, DAtmSubmitCardResponse.class);
				response2.captcha = "x";
				DialogManager.showAlertDialog(response2.toJsonString());
			}
		}
		mAdapter.onEvent(EEventType.ON_PAYMENT_COMPLETED, response);
	}
}
