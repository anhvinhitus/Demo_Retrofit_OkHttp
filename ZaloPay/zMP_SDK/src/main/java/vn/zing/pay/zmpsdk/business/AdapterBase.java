/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.BaseAdapter.java
 * Created date: Dec 22, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.DResponseGetStatus;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPOnClickListener;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.BasePaymentActivity;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.PaymentGatewayActivity;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import vn.zing.pay.zmpsdk.view.dialog.PaymentAlertDialog;

/**
 * @author YenNLH
 * 
 */
public abstract class AdapterBase {

	public abstract void init();

	public abstract String getChannelID();

	public abstract String getChannelName();

	public abstract String getPageName();

	public abstract String getLayoutID();

	public abstract Object onEvent(EEventType pEventType, Object... pAdditionParams);

	/**
	 * This event will be call when the attached activity is destroyed.
	 */
	public abstract void onFinish();

	/**
	 * Start immediately purchase flow of this adapter without user interaction
	 * precondition.
	 */
	public abstract void startPurchaseFlow();

	/**
	 * Event when user click on OK (Submit) button.
	 */
	public abstract void onClickSubmission();
	
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////

	protected PaymentChannelActivity mOwnerActivity = null;
	protected boolean mIsSuccess = false;

	public AdapterBase(PaymentChannelActivity pOwnerActivity) {
		if (pOwnerActivity != null) {
			mOwnerActivity = pOwnerActivity;

			GlobalData.getDefaultTracker().trackScreen(getSimpleName(), false);
		}
	}

	public String getSimpleName() {
		return getClass().getSimpleName().replace("Adapter", "");
	}

	public void setListener() {
		/************************
		 * SET ONCLICK LISTENER *
		 ************************/
		// setOnclikListner(Resource.id.zpsdk_exit_ctl, mOnClickListener);
		setOnclikListner(Resource.id.payment_channel_adapter, mOnSelectListener);
		setOnclikListner(Resource.id.zpsdk_btn_submit, okClickListener);
	}

	public PaymentChannelActivity getOwnerActivity() {
		return mOwnerActivity;
	}

	public void setOnclikListner(String pID, OnClickListener pListener) {
		View view = mOwnerActivity.findViewById(pID);
		if (view != null) {
			view.setOnClickListener(pListener);
		}
	}

	public boolean isStartImmediately() {
		return false;
	}

	public String getSuccessMessage(DResponseGetStatus pResponse) {
		return null;
	}

	protected void logOnEvent(EEventType pEventType, Object... pAdditionParams) {
		// ////// LOGGING /////////
		if (Log.IS_LOG_ENABLE) {
			StringBuilder strBuilder = new StringBuilder();
			if (pAdditionParams != null) {
				for (Object obj : pAdditionParams) {
					strBuilder.append(obj);
				}
			}
			Log.d(this, "=== OnEvent " + pEventType + ": " + strBuilder.toString());
		}
	}

	protected void getStatus(String pZmpTransID) {
		DialogManager.showProcessDialog(null, GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing));

		TGetStatusTask getStatusTask = new TGetStatusTask(this, pZmpTransID);
		getStatusTask.execute();
	}

	protected void onGetStatusComplete(String pZmpTransID, DResponseGetStatus pResponse) {
		
		if (pResponse != null) {
			if (pResponse.returnCode == 1) {
				// Successfull
				mIsSuccess = true;
				setPmcToResult();
				success(getSuccessMessage(pResponse), pZmpTransID);
			} else if (pResponse.isProcessing) {
				askToRetryGetStatus(pZmpTransID);
			} else {
				terminateAndShowDialog(pResponse.returnMessage);
			}
			return;
		}

		// Error
		terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_network_error_get_status_fail));
	}
	
	protected void setPmcToResult() {
		GlobalData.paymentResult.channelID = getChannelID();
		GlobalData.paymentResult.channelDetail = getChannelName();
	}

	/**
	 * This function is used to pass a message to parent activity and finish
	 * current activity.
	 * 
	 * @param pMessage
	 *            The message you want to send to previous activity.
	 */
	protected void success(String pMessage, String pZmpTransID) {
		// Tracking
		GlobalData.getDefaultTracker().trackPaymentCompleted(getChannelName(), pZmpTransID);

		if (pMessage == null || pMessage.length() == 0) {
			pMessage = GlobalData.getStringResource(Resource.string.zingpaysdk_alert_transaction_success);
		}
		
		GlobalData.paymentResult.paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS;
		finish(pMessage);
	}

	protected void processing(String pMessage) {
		if (pMessage == null || pMessage.length() == 0) {
			pMessage = GlobalData.getStringResource(Resource.string.zingpaysdk_alert_continue_processing);
		}
		
		GlobalData.paymentResult.paymentStatus = EPaymentStatus.ZPC_TRANXSTATUS_PROCESSING;
		finish(pMessage);
	}

	protected void finish(String pMessage) {
		// Finish and set payment channel to result
		setPmcToResult();
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra(Constants.FINISH_ACT_RETURN_MESSAGE, pMessage);

		if (mOwnerActivity != null) {
			mOwnerActivity.setResult(Constants.RESULT_OK, returnIntent);
			mOwnerActivity.finish();
		} else {
			Activity basePaymentActivity = BasePaymentActivity.getCurrentActivity();
			if (basePaymentActivity instanceof PaymentGatewayActivity) {
				((PaymentGatewayActivity) basePaymentActivity).onActivityResult(
						PaymentGatewayActivity.RC_REQUEST_CHANNEL_ACTIVITY, Constants.RESULT_OK, returnIntent);
			}
		}
	}

	protected void terminateAndShowDialog(String pMessage) {
		PaymentAlertDialog alertDialog = new PaymentAlertDialog(BasePaymentActivity.getCurrentActivity());
		if (pMessage == null)
			alertDialog.showNetworkErrorAlert();
		else
			alertDialog.showAlert(pMessage);

		alertDialog.setListener(new ZPOnClickListener() {
			@Override
			public void onClickOK() {
				if (mOwnerActivity != null) {
					mOwnerActivity.finish();
				} else if (BasePaymentActivity.getCurrentActivity() instanceof PaymentGatewayActivity) {
					PaymentGatewayActivity activity = (PaymentGatewayActivity) BasePaymentActivity.getCurrentActivity();
					if (activity.isForce()) {
						activity.onBackPressed();
					}
				}
			}

			@Override
			public void onClickCancel() {

			}
		});
	}

	protected void askToRetryGetStatus(final String pZmpTransID) {
		String message = GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing_ask_to_retry);
		DialogManager.showOptionAlertDialog(message, new ZPOnClickListener() {

			@Override
			public void onClickOK() {
				TGetStatusTask getStatusTask = new TGetStatusTask(AdapterBase.this, pZmpTransID);
				getStatusTask.execute();
			}

			@Override
			public void onClickCancel() {
				terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing_get_status_fail));
			}
		}, false);
	}

	// ///////////////////////////////////////////////////////////////////////
	// //////////////////////////// LISTENER /////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////

	protected final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.i("Zmp", "AdapterBase mOnClickListener: " + view);
			onEvent(EEventType.ON_CLICK, view);
		}
	};

	private View.OnClickListener okClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.i("Zmp", "AdapterBase okClickListener");
			AdapterBase.this.onClickSubmission();
		}
	};

	protected final OnClickListener mOnSelectListener = new OnClickListener() {
		@Override
		public void onClick(View pView) {
			Log.i("Zmp", "AdapterBase Select channel: " + pView);
			onEvent(EEventType.ON_SELECT, pView);

			if (pView != null && pView instanceof VPaymentChannelButton) {
				mOwnerActivity.setPaymentMethodName(((VPaymentChannelButton) pView).getPmcName());
			}
		}
	};
}
