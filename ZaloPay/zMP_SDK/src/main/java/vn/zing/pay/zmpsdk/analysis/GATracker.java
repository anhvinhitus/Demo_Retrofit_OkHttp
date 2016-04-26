/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.analysis.GATracker.java
 * Created date: Mar 5, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.analysis;

import java.lang.Thread.UncaughtExceptionHandler;

import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.utils.Log;

import android.content.Context;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.analytics.ecommerce.ProductAction;

/**
 * @author YenNLH
 * 
 */
public class GATracker implements IGATracker {
	public static final String PREFIX_APP_ID = "app_id-";
	public static final String PREFIX_PMC_ID = "pmc-";
	public static final String PREFIX_USER_ID = "uid-";
	
	private Tracker mTracker = null;

	public GATracker(Context pContext, String pTrackingID, long pAppID, String pAppUser) {
		try {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(pContext);
			// Set the dispatch period in seconds.
//			analytics.setLocalDispatchPeriod(60);

			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(pTrackingID);
			mTracker.enableAdvertisingIdCollection(true);

			// You only need to set User ID on a tracker once. By setting it on
			// the tracker, the ID will be sent with all subsequent hits.
			String userID = pAppID + "|" + pAppUser;
			mTracker.setClientId(userID);
			mTracker.set("&uid", userID);
			mTracker.set("&av", Constants.VERSION);
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}
	
	public void initDefaultUncaughtExceptionHandler() {
		UncaughtExceptionHandler myHandler = new ExceptionReporter(mTracker,
				Thread.getDefaultUncaughtExceptionHandler(), GlobalData.getOwnerActivity().getApplicationContext());

		// Make myHandler the new default uncaught exception handler.
		Thread.setDefaultUncaughtExceptionHandler(myHandler);
	}

	public void trackScreen(String pScreenName, boolean pIsNewSession) {
		if (mTracker != null) {
			mTracker.setScreenName(pScreenName);

			ScreenViewBuilder screenViewBuilder = new HitBuilders.ScreenViewBuilder();
			if (pIsNewSession) {
				screenViewBuilder.setNewSession();
			}

			mTracker.send(screenViewBuilder.build());
		}
	}

	public void trackEvent(String pCategory, String pAction, String pLabel, long pValue) {
		try {
		if (mTracker != null) {
			EventBuilder eventBuilder = new HitBuilders.EventBuilder();
			if (pCategory != null) {
				eventBuilder.setCategory(pCategory);
			}
			if (pAction != null) {
				eventBuilder.setAction(pAction);
			}
			if (pLabel != null) {
				eventBuilder.setLabel(pLabel);
			}
			if (pValue != Long.MIN_VALUE) {
				eventBuilder.setValue(pValue);
			}
			mTracker.send(eventBuilder.build());
		}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}

	@Override
	public void trackPaymentCompleted(String pChannelName, String pZmpTransID) {
		try {
			if (mTracker != null) {
				Log.d(this, "---- trackPaymentCompleted ----");
				mTracker.setScreenName("Payment Success");
				
				Product product = new Product()
				.setId(GlobalData.getPaymentInfo().skuID)
				.setName(GlobalData.getPaymentInfo().displayName)
				.setCategory(String.valueOf(GlobalData.getPaymentInfo().appID))
				.setPrice(GlobalData.getPaymentInfo().amount)
				.setQuantity(
							(GlobalData.getPaymentInfo().items == null) 
							? 0 
							: GlobalData.getPaymentInfo().items.size());
				
				ProductAction productAction = new ProductAction(ProductAction.ACTION_PURCHASE)
			    .setTransactionId(pZmpTransID)
			    .setTransactionAffiliation(PREFIX_PMC_ID + pChannelName)
			    .setTransactionRevenue(GlobalData.getPaymentInfo().amount);
				
				ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
			    .addProduct(product)
			    .setProductAction(productAction);
		
				mTracker.send(builder.build());
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}
	
	public void trackSmsCallbackCompleted(String pAppID, String pMno, String pZmpTransID, long pPPValue) {
		try {
			if (mTracker != null) {
				Log.d(this, "---- trackPaymentCompleted ----");
				mTracker.setScreenName("Payment Success");
				
				Product product = new Product()
				.setId("SMS-" + pMno + "-" + pPPValue)
				.setName("SMS " + pPPValue)
				.setCategory(pAppID)
				.setPrice(pPPValue)
				.setQuantity(1);
				
				ProductAction productAction = new ProductAction(ProductAction.ACTION_PURCHASE)
			    .setTransactionId(pZmpTransID)
			    .setTransactionAffiliation(PREFIX_PMC_ID + 5)
			    .setTransactionRevenue(pPPValue);
				
				ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
			    .addProduct(product)
			    .setProductAction(productAction);
		
				mTracker.send(builder.build());
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}
}
