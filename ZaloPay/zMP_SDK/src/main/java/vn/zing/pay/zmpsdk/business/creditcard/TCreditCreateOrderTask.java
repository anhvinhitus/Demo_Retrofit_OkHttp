/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.sms.TGetMessageSyntaxTask.java
 * Created date: Jan 6, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.creditcard;

import android.text.TextUtils;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.entity.creditcard.DCreditCardCreateOrderResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * @author YenNLH
 *
 */
public class TCreditCreateOrderTask extends TAbtractPaymentTask {
	private String mUrl = Constants.getUrlPrefix() + Constants.URL_ATM_CREATE_ORDER;
	
	public TCreditCreateOrderTask(AdapterBase adapter) {
		super(adapter);
	}

	
	@Override
	protected String doInBackground(Void... paramVarArgs) {
		Log.i("Zmp", "TCreditCreateOrderTask.doInBackground...");		
		
		try {			
			HttpClientRequest request = new HttpClientRequest(Type.POST, mUrl);
			putPaymentInfo(request);
			return request.getText();
		} catch (Exception ex) {
			Log.e("Zmp", ex);
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		DCreditCardCreateOrderResponse response = null;
		if (!TextUtils.isEmpty(result)) {
			response = GsonUtils.fromJsonString(result, DCreditCardCreateOrderResponse.class);		
		}
		mAdapter.onEvent(EEventType.ON_SUBMIT_COMPLETED, response);
	}
}
