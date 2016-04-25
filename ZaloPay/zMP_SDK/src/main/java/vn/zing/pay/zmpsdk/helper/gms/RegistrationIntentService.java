/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.helper.gms.RegistrationIntentService.java
 * Created date: Mar 2, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.helper.gms;

import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.utils.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;

/**
 * @author YenNLH
 * 
 */
public class RegistrationIntentService extends IntentService {

	// abbreviated tag name
	private static final String TAG = "RegIntentService";

	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			// Make a call to Instance API
			InstanceID instanceID = InstanceID.getInstance(this);
			String senderId = ZingMobilePayApplication.getInstance().getResources()
					.getString(Resource.getString(Resource.string.gcm_defaultSenderId));
			
			// request token that will be used by the server to send push
			// notifications
			String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
			Log.d(TAG, "GCM Registration Token: " + token);

			// pass along this data
			sendRegistrationToServer(token);
		} catch (Exception e) {
			Log.e(this, e);
		}
	}

	private void sendRegistrationToServer(String token) {
		// Add custom implementation, as needed.
		Log.e(this, "sendRegistrationToServer:" + token);
		GlobalData.setGcmToken(token);
	}
}
