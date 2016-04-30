/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.DialogManager.java
 * Created date: Dec 17, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view.dialog;

import java.lang.ref.WeakReference;

import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.listener.ZPOnClickListener;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.BasePaymentActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;

/**
 * @author YenNLH
 * 
 */
public class DialogManager {
	private final static Object SYNC = new Object();
	private static ProgressDialog mProgressDialog = null;
	private static long mLastShowProcessDialog = 0;

	/**
	 * Start the dialog and display it on screen. The window is placed in the
	 * application layer and opaque.
	 * 
	 * @param pTitle
	 *            Title
	 * 
	 * @param pMessage
	 *            Message content
	 * 
	 * @param pStartTime
	 *            Miliseconds at the time start showing
	 * 
	 * @throws Exception
	 *             This {@link Exception} will be thown iff there is a dialog is
	 *             showing on screen.
	 */
	public static void showProcessDialog(String pTitle, String pMessage, long pStartTime) {
		synchronized (SYNC) {
			mLastShowProcessDialog = pStartTime;

			Activity activity = BasePaymentActivity.getCurrentActivity();
			if (activity == null) {
				activity = GlobalData.getOwnerActivity();
			}

			if (mProgressDialog == null)
				mProgressDialog = new ProgressDialog(activity);

			if (!mProgressDialog.isShowing()) {
				try {
					mProgressDialog.setTitle((pTitle == null) ? "" : pTitle);
					mProgressDialog.setMessage((pMessage == null) ? GlobalData
							.getStringResource(Resource.string.zingpaysdk_alert_processing) : pMessage);
					mProgressDialog.setCancelable(false);

					if (activity != null && !activity.isFinishing()) {
						mProgressDialog.show();

						Log.d("DIALOG_MANAGER", "Starting a processing dialog");
					}
				} catch (Exception e) {
					Log.e("DIALOG_MANAGER", e);
				}
			} else {
				Log.e("DIALOG_MANAGER", "There is a showing process dialog!");
			}
		}
	}

	/**
	 * Start to show processing dialog. Beside it, this function will inform to
	 * adapter a {@code EEventType.ON_FAIL} event when the dialog isn't closed
	 * within {@code Constants.MAX_INTERVAL_OF_RETRY + 1000} miliseconds.
	 * 
	 * @param pTitle
	 *            Title
	 * @param pMessage
	 *            Message content
	 * @param pAdapterBase
	 *            Informed adapter
	 */
	public static void showProcessDialog(String pTitle, String pMessage, AdapterBase pAdapterBase) {
		synchronized (SYNC) {
			final long startTime = System.currentTimeMillis();
			showProcessDialog(pTitle, pMessage, startTime);

			final WeakReference<AdapterBase> adapterBase = new WeakReference<AdapterBase>(pAdapterBase);
			(new Handler()).postDelayed(new Runnable() {

				@Override
				public void run() {
					synchronized (SYNC) {
						if (mProgressDialog != null && adapterBase.get() != null
								 && mProgressDialog.isShowing() && mLastShowProcessDialog == startTime) {
							adapterBase.get().onEvent(EEventType.ON_FAIL);
						}
					}
				}
			}, Constants.MAX_INTERVAL_OF_RETRY + 1000);
		}
	}

	/**
	 * Start to show processing dialog. See {@link showProcessDialog} method for
	 * more details.
	 * 
	 * @param pTitle
	 *            Title
	 * @param pMessage
	 *            Message content
	 */
	public static void showProcessDialog(String pTitle, String pMessage) {
		showProcessDialog(pTitle, pMessage, 0);
	}

	/**
	 * Change message text of a processing dialog showing on the screen.
	 * 
	 * @param pMessage
	 *            A new message
	 */
	public static void changeMessageProcessingDialog(final String pMessage) {
		synchronized (SYNC) {
			try {
				if (GlobalData.getOwnerActivity() != null && !GlobalData.getOwnerActivity().isFinishing()
						&& mProgressDialog != null && mProgressDialog.isShowing()) {
					(BasePaymentActivity.getCurrentActivity() == null ? GlobalData.getOwnerActivity()
							: BasePaymentActivity.getCurrentActivity()).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.d("DIALOG_MANAGER", "==== Change message: " + pMessage);
							mProgressDialog.setMessage(pMessage);
						}
					});
				}
			} catch (Exception e) {
				Log.e("DIALOG_MANAGER", e);
			}
		}
	}

	/**
	 * Dismiss this dialog, removing it from the screen. This method can be
	 * invoked safely from any thread.
	 */
	public static void closeProcessDialog() {
		synchronized (SYNC) {
			try {
				if (GlobalData.getOwnerActivity() != null && !GlobalData.getOwnerActivity().isFinishing()
						&& mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
					Log.e("DIALOG_MANAGER", "Dismissed a processing dialog");
				}
			} catch (Exception e) {
				Log.e("DIALOG_MANAGER", e);
			}
		}
	}

	/**
	 * Start immediately the alert dialog and display it on screen. The window
	 * is placed in the application layer and opaque.
	 * 
	 * @param pMessage
	 *            Message content you want to show on the screen.
	 */
	public static void showAlertDialog(final String pMessage) {
		final Activity currentActivity = BasePaymentActivity.getCurrentActivity();
		if (currentActivity != null) {
			currentActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					PaymentAlertDialog alertDlg = new PaymentAlertDialog(currentActivity);
					alertDlg.showAlert(pMessage);
				}
			});
		}
	}

	/**
	 * 
	 * @param pMessage
	 */
	public static void showOptionAlertDialog(final String pMessage, final ZPOnClickListener pListener,
			final boolean pIsHideCancelBtn) {
		final Activity currentActivity = BasePaymentActivity.getCurrentActivity();
		if (currentActivity != null) {
			currentActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					PaymentAlertDialog alertDlg = new PaymentAlertDialog(currentActivity, pListener, false,
							pIsHideCancelBtn);
					alertDlg.showAlert(pMessage);
				}
			});
		}
	}
}
