/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.inappbilling.GoogleIAPService.java
 * Created date: Dec 23, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.inappbilling;

import java.util.ArrayList;
import java.util.List;

import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.helper.google.IabHelper;
import vn.zing.pay.zmpsdk.helper.google.IabResult;
import vn.zing.pay.zmpsdk.helper.google.Inventory;
import vn.zing.pay.zmpsdk.helper.google.Purchase;
import vn.zing.pay.zmpsdk.helper.google.SkuDetails;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.BasePaymentActivity;
import android.app.AlertDialog;
import android.content.Intent;

/**
 * @author YenNLH
 * 
 */
public class GoogleIABService {
	public static final int RC_REQUEST = 40001;
	private static final String TAG = "GoogleIAPService";

	private AdapterGoogleInappBilling mAdapterGoogleInappBilling = null;
	private IabHelper mIabHelper = null;
	private BasePaymentActivity mOwnerActivity = null;

	private SkuDetails mSkuDetails = null;

	public GoogleIABService(AdapterGoogleInappBilling pAdapterGoogleInappBilling) {
		mAdapterGoogleInappBilling = pAdapterGoogleInappBilling;
		mOwnerActivity = mAdapterGoogleInappBilling.getOwnerActivity();
		mIabHelper = new IabHelper(mOwnerActivity, null);

		// enable debug logging (for a production application, you
		// should set this to false).
		mIabHelper.enableDebugLogging(true);
	}

	/**
	 * Start setup. This is asynchronous and the specified listener will be
	 * called once setup completes.
	 */
	public void startSetup() {

		Log.d(TAG, "Starting setup.");
		mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(TAG, "Setup finished.");

				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					complain("Problem setting up in-app billing: " + result);
					mAdapterGoogleInappBilling.onEvent(EEventType.ON_SEPTUP_FAIL);
					return;
				}

				// Have we been disposed of in the meantime? If so,
				// quit.
				if (mIabHelper == null)
					return;

				// IAB is fully set up. Now, let's get an inventory of
				// stuff we own.
				Log.d(TAG, "Setup successful. Querying inventory.");
				List<String> moreSkuIds = new ArrayList<String>();
				moreSkuIds.add(GlobalData.getPaymentInfo().skuID);
				mIabHelper.queryInventoryAsync(mGotInventoryListener, moreSkuIds);
			}
		});
	}

	public void launchPurchaseFlow(String pPayload) {
		/*
		 * TODO: for security, generate your payload here for verification. See
		 * the comments on verifyDeveloperPayload() for more info. Since this is
		 * a SAMPLE, we just use an empty string, but on a production app you
		 * should carefully generate this.
		 */

		mIabHelper.launchPurchaseFlow(mOwnerActivity, GlobalData.getPaymentInfo().skuID, RC_REQUEST,
				mPurchaseFinishedListener, pPayload);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
		if (mIabHelper == null)
			return;

		// Pass on the activity result to the helper for handling
		if (mIabHelper.handleActivityResult(requestCode, resultCode, data)) {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}

	/**
	 * Listener that's called when we finish querying the items and
	 * subscriptions we own
	 */
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {

		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Log.d(TAG, "Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (mIabHelper == null)
				return;

			// Is it a failure?
			if (result.isFailure()) {
				complain("Failed to query inventory: " + result);
				return;
			}

			Log.i(TAG, "Query inventory was successful: " + inventory);

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Check for gas delivery -- if we own gas, we should fill up the
			// tank immediately
			Purchase itemPurchase = inventory.getPurchase(GlobalData.getPaymentInfo().skuID);
			mSkuDetails = inventory.getSkuDetails(GlobalData.getPaymentInfo().skuID);
			Log.i(TAG, "Sku Details: " + mSkuDetails);

			if (itemPurchase != null/* && verifyDeveloperPayload(itemPurchase) */) {
				Log.d(TAG, "We have " + GlobalData.getPaymentInfo().skuID + ". Consuming it.");
				mIabHelper.consumeAsync(itemPurchase, mConsumeFinishedListener);
				return;
			}

			// Inform to adapter
			mAdapterGoogleInappBilling.onEvent(EEventType.ON_CONSUMPTION, result, mSkuDetails);

			Log.d(TAG, "Initial inventory query finished; enabling main UI.");
		}
	};

	/**
	 * Called when consumption is complete
	 */
	IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

			// if we were disposed of in the meantime, quit.
			if (mIabHelper == null)
				return;

			// We know this is the "gas" sku because it's the only one we
			// consume, so we don't check which sku was consumed. If you have
			// more than one sku, you probably should check...
			if (result.isSuccess()) {
				// successfully consumed, so we apply the effects of the item in
				// our game world's logic, which in our case means filling the
				// gas tank a bit
				Log.d(TAG, "Consumption successful. Provisioning.");
			} else {
				complain("Error while consuming: " + result);
			}

			// Inform to adapter
			mAdapterGoogleInappBilling.onEvent(EEventType.ON_CONSUMPTION, result, mSkuDetails);

			Log.d(TAG, "End consumption flow.");
		}
	};

	/**
	 * Callback for when a purchase is finished
	 */
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

			// if we were disposed of in the meantime, quit.
			if (mIabHelper == null)
				return;

			if (result.isFailure()) {
				complain("Error purchasing: " + result);
				mAdapterGoogleInappBilling.onEvent(EEventType.ON_PURCHASED, result, purchase);
				return;
			}

			Log.d(TAG, "Purchase successful.");

			if (purchase.getSku().equals(GlobalData.getPaymentInfo().skuID)) {
				// TODO:
				mAdapterGoogleInappBilling.onEvent(EEventType.ON_PURCHASED, result, purchase);
			}
		}
	};

	/**
	 * Very important:
	 * 
	 * We're being destroyed. It's important to dispose of the helper here!
	 */
	public void destroy() {
		Log.d(TAG, "Destroying helper.");
		if (mIabHelper != null) {
			mIabHelper.dispose();
			mIabHelper = null;
		}
	}

	void complain(String message) {
		Log.e(TAG, "**** TrivialDrive Error: " + message);
		// alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this.mOwnerActivity);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		Log.d(TAG, "Showing alert dialog: " + message);
		bld.create().show();
	}
}
