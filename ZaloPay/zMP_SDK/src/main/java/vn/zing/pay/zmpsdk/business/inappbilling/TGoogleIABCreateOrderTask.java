/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.inappbilling.GoogleIABCreateOrder.java
 * Created date: Dec 23, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.inappbilling;

import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.google.DGoogleIabCreateOrderResponse;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.text.TextUtils;

/**
 * @author YenNLH
 * 
 */
public class TGoogleIABCreateOrderTask extends TAbtractPaymentTask {
	private String mCreateOrderURL = Constants.getUrlPrefix() + Constants.URL_GIAB_CREATE_ORDER;

	public TGoogleIABCreateOrderTask(AdapterGoogleInappBilling pAdapterGoogleInappBilling) {
		super(pAdapterGoogleInappBilling);
	}

	@Override
	protected String doInBackground(Void... params) {

		HttpClientRequest request = new HttpClientRequest(Type.POST, mCreateOrderURL);

		try {
			putPaymentInfo(request);
		} catch (Exception ex) {
			Log.e(this, ex);
		}

		return request.getText();
	}

	@Override
	protected void onPostExecute(String result) {
		if (!TextUtils.isEmpty(result)) {
			DGoogleIabCreateOrderResponse response = (DGoogleIabCreateOrderResponse) (new DGoogleIabCreateOrderResponse())
					.fromJsonString(result);
			mAdapter.onEvent(EEventType.ON_CREATE_ORDER_COMPLETED, response);

			// TODO: Remove this
			if (Log.IS_LOG_ENABLE) {
				DialogManager.showAlertDialog(result);
			}
		} else {
			mAdapter.onEvent(EEventType.ON_CREATE_ORDER_COMPLETED);
		}
	}
}
