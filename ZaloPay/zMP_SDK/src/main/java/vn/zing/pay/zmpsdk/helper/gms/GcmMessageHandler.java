/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.helper.gms.GcmMessageHandler.java
 * Created date: Mar 3, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.helper.gms;

import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.analysis.GATracker;
import vn.zing.pay.zmpsdk.analysis.IGATracker;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;
import vn.zing.pay.zmpsdk.utils.Log;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * @author YenNLH
 * 
 */
public class GcmMessageHandler extends GcmListenerService {

	public static final int MESSAGE_NOTIFICATION_ID = (int) System.currentTimeMillis() - 1000000;

	private IGATracker mTracker = null;
	private ZPPaymentListener mListener = ZingMobilePayApplication.mListener;
	private long mAppID = ZingMobilePayApplication.appID;
	private String mAppUser = ZingMobilePayApplication.mAppUser;

	@Override
	public void onMessageReceived(String from, Bundle data) {
		String message = data.toString();
		Log.e(this, from + "|" + message);

		String appTransID = data.getString("appTransID");
		String zmpTransID = data.getString("zmpTransID");
		String mno = data.getString("mno");
		long ppValue = Long.parseLong(data.getString("ppValue"));

		if (mListener != null) {
			mListener.onSMSCallBack(appTransID);
		}

		synchronized (this) {
			if (mTracker == null) {
				mTracker = new GATracker(getApplicationContext(), ZingMobilePayApplication.getInstance().getResources()
						.getString(Resource.getString(Resource.string.global_tracker_id)), mAppID, mAppUser);
			}

			mTracker.trackSmsCallbackCompleted(String.valueOf(mAppID), mno, zmpTransID, ppValue);
		}

		if (Constants.IS_DEV) {
			createNotification(from, message);
		}
	}

	// Creates notification based on title and body received
	private void createNotification(String title, String body) {
		Context context = getBaseContext();
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(Resource.getDrawable(Resource.drawable.ico_zingxu)).setContentTitle(title).setContentText(body);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
	}
}
