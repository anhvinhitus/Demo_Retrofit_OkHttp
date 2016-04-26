/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.PaymentAlertDialog.java
 * Created date: Dec 17, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view.dialog;

import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.listener.ZPOnClickListener;
import vn.zing.pay.zmpsdk.utils.Log;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/**
 * The alert dialog with custom view.
 * 
 * @author YenNLH
 */
public class PaymentAlertDialog extends PaymentDialog {

	private ZPOnClickListener mListener = null;
	private String mOkBtnText = null;

	private boolean mIsHideOkButton = false;
	private boolean mIsHideCancelButton = true;

	public PaymentAlertDialog(Context context) {
		super(context);
	}

	public PaymentAlertDialog(Context context, ZPOnClickListener listener, boolean isHideOkBtn, boolean isHideCancelBtn) {
		super(context);
		this.mListener = listener;
		this.mIsHideOkButton = isHideOkBtn;
		this.mIsHideCancelButton = isHideCancelBtn;
	}

	public void setListener(ZPOnClickListener listener) {
		this.mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set layout
		setContentView(Resource.getLayout(Resource.layout.zpsdk_activity_alert));

		// Set on click event to OKBUTTON
		findViewById(Resource.id.zpsdk_ok_ctl).setOnClickListener(mOnClick);

		// Set text contents
		if (!TextUtils.isEmpty(mOkBtnText))
			((TextView) findViewById(Resource.id.zpsdk_ok_ctl)).setText(mOkBtnText);

		if (this.mIsHideCancelButton) {
			findViewById(Resource.id.zpsdk_button_devider).setVisibility(View.GONE);
			findViewById(Resource.id.zpsdk_cancel_ctl).setVisibility(View.GONE);
			findViewById(Resource.id.zpsdk_cancel_ctl).setOnClickListener(null);
		} else {
			findViewById(Resource.id.zpsdk_button_devider).setVisibility(View.VISIBLE);
			findViewById(Resource.id.zpsdk_cancel_ctl).setVisibility(View.VISIBLE);
			findViewById(Resource.id.zpsdk_cancel_ctl).setOnClickListener(mOnClick);
		}
	}

	public void setOkButtonTitle(String title) {
		this.mOkBtnText = title;

	}

	/**
	 * Start the alert dialog and display it on screen. The window is placed in
	 * the application layer and opaque.
	 * 
	 * @param message
	 *            Message content you want to show on the screen.
	 */
	public void showAlert(String message) {
		try {
			show();

			if (TextUtils.isEmpty(message)) {
				message = GlobalData.getStringResource(Resource.string.zingpaysdk_alert_network_error);
			}

			((TextView) findViewById(Resource.id.zpsdk_message_ctl)).setText(message);

			View okBtn = findViewById(Resource.id.zpsdk_ok_ctl);
			if (mIsHideOkButton) {
				okBtn.setVisibility(View.GONE);
			} else {
				okBtn.setVisibility(View.VISIBLE);
				setCancelable(false);
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}

	public void showNetworkErrorAlert() {
		showAlert(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_network_error));
	}

	/**
	 * This listener will be called when user click on the button.
	 */
	private android.view.View.OnClickListener mOnClick = new android.view.View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == Resource.getID(Resource.id.zpsdk_ok_ctl)) {
				dismiss();
				if (mListener != null) {
					mListener.onClickOK();
				}
			} else if (id == Resource.getID(Resource.id.zpsdk_cancel_ctl)) {
				dismiss();
				if (mListener != null) {
					mListener.onClickCancel();
				}
			}
		}
	};

}
