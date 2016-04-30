/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.wrapper.GlobalData.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data;

import com.google.android.gms.analytics.Tracker;

import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.analysis.GATracker;
import vn.zing.pay.zmpsdk.analysis.IGATracker;
import vn.zing.pay.zmpsdk.entity.ZPPaymentInfo;
import vn.zing.pay.zmpsdk.entity.ZPPaymentOption;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;
import vn.zing.pay.zmpsdk.utils.Log;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * @author YenNLH
 * 
 */
public class GlobalData {
	/**
	 * {@link Activity} object that is using ZingPay SDK.
	 */
	private static Activity mApplication = null;
	private static String mTempGcmToken = null;
	private static IGATracker mTracker = null;

	/**
	 * The listener is used to inform application when finishing a transaction
	 */
	private static ZPPaymentListener mListener = null;
	private static ZPPaymentInfo mPaymentInfo = null;
	private static ZPPaymentOption mPaymentOption = null;

	/**
	 * Check if the caller is {@code pay} method of {@link ZingMobilePayService}
	 * class.
	 * 
	 * @return {@code TRUE} if right caller, {@code FALSE} otherwise
	 */
	private static boolean isAccessRight() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		boolean isRightAccess = false;

		if (stackTraceElements.length >= 5) {
			if (stackTraceElements[4].getClassName().equals(ZingMobilePayService.class.getName())
					&& stackTraceElements[4].getMethodName().equals("pay")) {
				isRightAccess = true;
			}
		}

		return isRightAccess;
	}

	/**
	 * This method must be called when user wants to make a purchase.
	 * 
	 * It will store own activity for all next process.
	 * 
	 * @param pActivity
	 *            {@link Activity} instance of owner application
	 * 
	 * @throws Exception
	 *             This exception will be thrown iff the caller is not
	 *             {@code pay} method of {@link ZingMobilePayService} class.
	 */
	public static void setApplication(Activity pActivity, ZPPaymentListener pPaymentListener,
			ZPPaymentInfo pPaymentInfo, ZPPaymentOption pPaymentOption) throws Exception {

		/**
		 * Check if user violates
		 */
		if (!isAccessRight()) {
			throw new Exception(
					"Violate Design Pattern! Only 'pay' static method of ZingPayService class can set application!");
		}

		mApplication = pActivity;
		mListener = pPaymentListener;
		mPaymentInfo = pPaymentInfo;
		mPaymentOption = pPaymentOption;
		paymentResult = new ZPPaymentResult(pPaymentInfo, EPaymentStatus.ZPC_TRANXSTATUS_FAIL);

		if (mTempGcmToken != null) {
			SharedPreferencesManager.getInstance().setGcmToken(mTempGcmToken);
			mTempGcmToken = null;
		}

		Log.d("GCM", "setPaymentListener");
		ZingMobilePayApplication.mListener = mListener;
		ZingMobilePayApplication.appID = mPaymentInfo.appID;
		ZingMobilePayApplication.mAppUser = mPaymentInfo.appUser;
		Log.i("GCM", "setPaymentListener");
	}

	/**
	 * Get the {@link Context} instance of current application.
	 * 
	 * @return The current application
	 */
	public static Context getApplication() {
		return mApplication.getBaseContext();
	}

	/**
	 * Get the {@link Activity} instance of owner application.
	 * 
	 * @return Activity of owner application
	 */
	public static Activity getOwnerActivity() {
		return mApplication;
	}

	/**
	 * Get the {@link ZPPaymentListener} instance provided by application
	 * 
	 * @return Listener
	 */
	public static ZPPaymentListener getPaymentListener() {
		return mListener;
	}

	/**
	 * Get the {@link ZPPaymentInfo} instance provided by application
	 * 
	 * @return Payment information
	 */
	public static ZPPaymentInfo getPaymentInfo() {
		return mPaymentInfo;
	}

	/**
	 * Get the {@link ZPPaymentInfo} instance provided by application
	 * 
	 * @return Payment information
	 */
	public static ZPPaymentOption getPaymentOption() {
		return mPaymentOption;
	}

	/**
	 * Returns a localized string from the application's package's default
	 * string table.
	 * 
	 * @param pResourceID
	 *            Resource id for the string
	 * 
	 * @return The string data associated with the resource, stripped of styled
	 *         text information.
	 */
	public static String getStringResource(String pResourceID) {
		if (getApplication() == null)
			return null;

		// Try to get string from resource sent from before get from local
		String result = ResourceManager.getInstance(null).getString(pResourceID);

		return (result != null) ? result : getApplication().getString(Resource.getString(pResourceID));
	}

	/**
	 * Return the entry name for a given resource identifier.
	 * 
	 * @param pResourceID
	 *            The resource identifier whose entry name is to be retrieved.
	 * 
	 * @return A string holding the entry name of the resource.
	 */
	public static String getResourceName(int pResourceID) {
		return getApplication().getResources().getResourceEntryName(pResourceID);
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * 
	 * @return tracker
	 */
	public static synchronized IGATracker getDefaultTracker() {
		if (mTracker == null) {
			String trackingID = GlobalData.getStringResource(Resource.string.global_tracker_id);
			mTracker = new GATracker(getOwnerActivity().getApplicationContext(), trackingID, mPaymentInfo.appID,
					mPaymentInfo.appUser);
			mTracker.initDefaultUncaughtExceptionHandler();
		}
		return mTracker;
	}

	public static synchronized void setGcmToken(String pToken) {
		if (mApplication == null) {
			mTempGcmToken = pToken;
		} else {
			SharedPreferencesManager.getInstance().setGcmToken(pToken);
		}
	}

	/**
	 * Remember to assign to <code>NULL</code> right after used.
	 */
	public static Object tempObject = null;
	public static ZPPaymentResult paymentResult = null;
}
