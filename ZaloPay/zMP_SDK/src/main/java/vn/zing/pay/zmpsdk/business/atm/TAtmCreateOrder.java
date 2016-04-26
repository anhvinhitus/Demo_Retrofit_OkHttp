/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.TAtmCreateOrder.java
 * Created date: Jan 14, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.atm;

import android.text.TextUtils;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.entity.atm.DAtmCreateOrderResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * @author YenNLH
 * 
 */
public class TAtmCreateOrder extends TAbtractPaymentTask {

	public TAtmCreateOrder(AdapterBase adapter) {
		super(adapter);
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			Log.i("Zmp", "TAtmCreateOrder.doInBackground...");
			HttpClientRequest request = new HttpClientRequest(Type.POST, Constants.getUrlPrefix() + Constants.URL_ATM_CREATE_ORDER);
			putPaymentInfo(request);
			request.addParams("bankCode", ((AdapterATM) mAdapter).getBankCode());

			return request.getText();
		} catch (Exception ex) {
			Log.e(this, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		DAtmCreateOrderResponse response = null;
		if (!TextUtils.isEmpty(result)) {
			response = GsonUtils.fromJsonString(result, DAtmCreateOrderResponse.class);
		}
		mAdapter.onEvent(EEventType.ON_CREATE_ORDER_COMPLETED, response);
	}
}
