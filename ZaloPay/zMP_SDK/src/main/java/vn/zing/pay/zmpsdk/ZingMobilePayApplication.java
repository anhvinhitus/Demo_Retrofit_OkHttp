/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.ZingPayApplication.java
 * Created date: Dec 19, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk;

import java.util.List;

import vn.zing.pay.zmpsdk.business.inappbilling.TGoogleIABVerifyReceiptTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.sqllite.GoogleIABReceiptDataSource;
import vn.zing.pay.zmpsdk.entity.google.DGoogleIabReceipt;
import vn.zing.pay.zmpsdk.helper.gms.RegistrationIntentService;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

/**
 * @author YenNLH
 * 
 */
public class ZingMobilePayApplication extends Application {
	public static long appID = 1L;
	public static ZPPaymentListener mListener = null;
	public static String mAppUser = null;;

	private static boolean mIsConfigOffSuccessDialog = false;
	private static boolean mIsConfigFullScreen = true;

	private static Handler mHandler = new Handler();
	private static Application mInstance = null;
	

	public static boolean isConfigOffSuccessDialog() {
		return mIsConfigOffSuccessDialog;
	}

	public static boolean isConfigFullScreen() {
		return mIsConfigFullScreen;
	}

	public void onCreate() {
		super.onCreate();
		init(this);
	}

	public static void wrap(Application app) {
		init(app);
	}

	private static void init(final Application app) {
		mInstance = app;
		
		loadConfiguration(app);
		retryGoogleInAppBilling(app, 0);
		try {
			Intent i = new Intent(app, RegistrationIntentService.class);
			app.startService(i);
		} catch (Exception ex) {
			Log.e("RegistrationIntentService", ex);
		}
	}

	private static void loadConfiguration(Application app) {
		try {
			Bundle bundle = app.getPackageManager().getApplicationInfo(app.getPackageName(), PackageManager.GET_META_DATA).metaData;
			Object zmpAppID = bundle.get("zmpAppID");
			if (zmpAppID instanceof Integer) {
				appID = (Integer) zmpAppID;
			} else if (zmpAppID instanceof Long) {
				appID = (Long) zmpAppID;
			} else {
				appID = Long.parseLong(zmpAppID.toString());
			}

			if (bundle.containsKey("mIsConfigOffSuccessDialog"))
				mIsConfigOffSuccessDialog = ((Boolean) bundle.get("zmpConfigOffSuccessDialog")).booleanValue();
			if (bundle.containsKey("mIsConfigFullScreen"))
				mIsConfigFullScreen = ((Boolean) bundle.get("zmpConfigFullScreen")).booleanValue();

		} catch (Exception e) {
			Log.e("ZingMobilePayApplication.loadConfiguration", e);
		}
	}

	public static void retryGoogleInAppBilling(final Context pContext, final int pDelay) {
		Runnable retry = new Runnable() {
			private int previousNumOfReceiptList = -1;
			private int numOfRetry = 0;

			@Override
			public void run() {
				/* do what you need to do */
				Log.i(this, "********************* RETRY GIAB VERIFYING **************************");

				if (numOfRetry == 10) {
					return;
				}

				if (ConnectionUtil.isOnline(pContext)) {
					GoogleIABReceiptDataSource dataSource = new GoogleIABReceiptDataSource(pContext);
					List<DGoogleIabReceipt> receiptList = dataSource.getAll();

					if (receiptList != null && receiptList.size() > 0) {
						if (previousNumOfReceiptList != -1 && previousNumOfReceiptList == receiptList.size()) {
							numOfRetry++;
						}
						previousNumOfReceiptList = receiptList.size();

						DGoogleIabReceipt receipt = receiptList.get(0);
						TGoogleIABVerifyReceiptTask task = new TGoogleIABVerifyReceiptTask(null, receipt.appID,
								receipt.receipt, receipt.signature, receipt.zmpTransID, receipt.payload);
						task.setIsRetry(pContext);
						task.execute();
					} else {
						if (Constants.IS_DEV && Log.IS_LOG_ENABLE) {
							Toast.makeText(pContext, "Google IAB retry: success : " + numOfRetry, Toast.LENGTH_LONG)
									.show();
						}
						// Stop this step if everything is okay
						return;
					}
				} else {
					numOfRetry++;
				}
				Log.i(this, "********************* numOfRetry:" + numOfRetry + " **************************");

				if (Constants.IS_DEV && Log.IS_LOG_ENABLE) {
					Toast.makeText(pContext, "Google IAB numOfRetry:" + numOfRetry, Toast.LENGTH_LONG).show();
				}
				/* and here comes the "trick" */
				mHandler.postDelayed(this, Constants.SLEEPING_INTERVAL_OF_GIAB_VERIFY_RETRY);
			}
		};
		mHandler.postDelayed(retry, pDelay);
	}
	
	public static Application getInstance() {
		return mInstance;
	}
}