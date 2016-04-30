/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.sms.AdapterSMS.java
 * Created date: Jan 4, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.sms;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.sms.DSmsRegResponse;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;

/**
 * @author YenNLh
 * 
 */
public class AdapterSMS extends AdapterBase {
	private static final String ADAPTER_NAME = "zpsdk_sms";
	private static final int RC_SEND_SMS = 6969;
	private static final boolean IS_DUAL_SIM = ConnectionUtil.isDualSim(GlobalData.getApplication());
	
	private String mMno = null;
	private boolean isSentSMS = false;

	public AdapterSMS(PaymentChannelActivity pOwnerActivity) {
		super(pOwnerActivity);
		if (!IS_DUAL_SIM) {
			mMno = ConnectionUtil.getSimOperator(GlobalData.getApplication());
		} 
	}

	@Override
	public void init() {
	}

	@Override
	public void onFinish() {
	}

	@Override
	public String getChannelID() {
		return GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_sms);
	}
	
	@Override
	public String getChannelName() {
		return getChannelID();
	}

	@Override
	public String getPageName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getLayoutID() {
		if (isStartImmediately())
			return null;

		return Resource.layout.zpsdk_sms;
	}

	@Override
	public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
		if (pEventType == EEventType.ON_SELECT) {

			VPaymentChannelButton view = (VPaymentChannelButton) pAdditionParams[0];
			mMno = String.valueOf(view.getPmcID());
			mOwnerActivity.enableSubmitBtn(true);

		} else if (pEventType == EEventType.ON_SUBMIT_COMPLETED) {
			// // CLOSE DIALOG ////
			DialogManager.closeProcessDialog();

			if (pAdditionParams == null || pAdditionParams.length < 1 || pAdditionParams[0] == null) {
				terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_no_connection));
				return null;
			}

			DSmsRegResponse response = (DSmsRegResponse) pAdditionParams[0];

			if (response.returnCode < 1) {
				// Error
				terminateAndShowDialog(response.returnMessage);
			} else {
				String number = response.smsServicePhones.servicePhone;
				String smsMessage = response.smsServicePhones.smsSyntax;

				sendSMS(number, smsMessage);
			}

		} else if (pEventType == EEventType.ON_ACTIVITY_RESULT || pEventType == EEventType.ON_RESUME) {
			processing(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_sent_sms));
		}

		return null;
	}

	@Override
	public boolean isStartImmediately() {
		return !isSentSMS && !IS_DUAL_SIM;
	}

	@Override
	public void startPurchaseFlow() {
		isSentSMS = true;
		if (ConnectionUtil.isAbleTofSendSMS(mOwnerActivity)) {
			try {
				TSmsGetMessageSyntaxTask getMessageSyntaxTask = new TSmsGetMessageSyntaxTask(this);
				getMessageSyntaxTask.execute();
			} catch (Exception e) {
				terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_not_support_device));
			}
		} else {
			terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_not_support_device));
		}
	}

	@Override
	public void onClickSubmission() {
		startPurchaseFlow();
	}

	public String getMno() {
		return mMno;
	}

	private void sendSMS(String pPhoneNumber, String pMessageBody) {		
		String defApp = Settings.Secure.getString(mOwnerActivity.getContentResolver(), "sms_default_application");
		Uri smsUri = Uri.parse("smsto:" + pPhoneNumber);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setPackage(defApp);
		intent.putExtra("sms_body", pMessageBody);
		intent.putExtra("address", pPhoneNumber);
		intent.putExtra(Intent.EXTRA_TEXT, pMessageBody);
		intent.setData(smsUri);
		mOwnerActivity.startActivityForResult(intent, RC_SEND_SMS);
	}
}
