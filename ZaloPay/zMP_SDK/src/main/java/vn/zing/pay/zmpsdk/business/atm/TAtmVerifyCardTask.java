/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.TAtmVerifyCardTask.java
 * Created date: Jan 14, 2016
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
public class TAtmVerifyCardTask extends TAbtractPaymentTask {
	private String mZmpTransID;
	private String mCardHolderName;
	private String mCardNumber;
	private String mCardPass;
	private String mCardMonth;
	private String mCardYear;

	public TAtmVerifyCardTask(AdapterBase adapter, String pZmpTransId, String pCardHolder, String pCardNum,
			String pCardPass, String pCardMonth, String pCardYear) {
		super(adapter);

		mZmpTransID = pZmpTransId;
		mCardHolderName = pCardHolder;
		mCardNumber = pCardNum;
		mCardPass = pCardPass;
		mCardMonth = pCardMonth;
		mCardYear = pCardYear;
	}

	@Override
	protected String doInBackground(Void... params) {
		Log.d(this, "--- TAtmVerifyCardTask ---");
		try {
			HttpClientRequest request = new HttpClientRequest(Type.POST, URLDecoder.decode(
					Constants.getUrlPrefix() + Constants.URL_ATM_VERIFY_CARD, "utf-8"));
			request.addParams("zmpTransID", mZmpTransID);
			request.addParams("cardHolderName", mCardHolderName);
			request.addParams("cardNumber", mCardNumber);
			request.addParams("cardPass", mCardPass);
			request.addParams("cardMonth", mCardMonth);
			request.addParams("cardYear", mCardYear);
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
		mAdapter.onEvent(EEventType.ON_VERIFY_COMPLETED, response);
	}
}
