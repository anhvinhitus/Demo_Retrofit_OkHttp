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
package vn.zing.pay.zmpsdk.business.creditcard;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.entity.creditcard.DCreditCardCreateOrderResponse;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.PaymentGatewayActivity;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton;

/**
 * @author YenNLh
 * 
 */
public class AdapterCreditCard extends AdapterBase {
	private static final int RC_OPEN_BROWSER_CREDIT_CARD = 0xCCCC;

	private String mPmcID = null;
	private DCreditCardCreateOrderResponse mCreateOrderResponse = null;

	public AdapterCreditCard(PaymentChannelActivity pOwnerActivity) {
		super(pOwnerActivity);
	}

	@Override
	public void init() {
	}

	@Override
	public void onFinish() {
	}

	@Override
	public String getChannelID() {
		return mPmcID; // GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_atm);
	}

	@Override
	public String getChannelName() {
		return mPmcID;
	}

	@Override
	public String getPageName() {
		return "zpsdk_credit_card";
	}

	@Override
	public boolean isStartImmediately() {
		try {
			boolean ret = ResourceManager.getInstance(getPageName()).getDynamicView().SelectionView.items.size() == 1;
			if (ret) {
				mPmcID = ResourceManager.getInstance(getPageName()).getDynamicView().SelectionView.items.get(0).pmcID;
			}
			return ret;
		} catch (Exception ex) {
			return false;
		}
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
			mPmcID = String.valueOf(view.getPmcID());
			mOwnerActivity.enableSubmitBtn(true);

		} else if (pEventType == EEventType.ON_SUBMIT_COMPLETED) {

			if (pAdditionParams == null || pAdditionParams.length == 0 || pAdditionParams[0] == null) {
				terminateAndShowDialog(null);
			} else {
				mCreateOrderResponse = (DCreditCardCreateOrderResponse) pAdditionParams[0];
				if (mCreateOrderResponse.returnCode > 0) {
					Intent intent = ConnectionUtil.getBrowserIntent(mOwnerActivity, mCreateOrderResponse.redirectUrl);
					mOwnerActivity.startActivityForResult(intent, RC_OPEN_BROWSER_CREDIT_CARD);

					Intent returnIntent = new Intent();
					mOwnerActivity.setResult(Constants.RESULT_BACK, returnIntent);
					mOwnerActivity.finish();
					mOwnerActivity = null;

					// Temporary
					GlobalData.tempObject = this;
				} else {
					terminateAndShowDialog(mCreateOrderResponse.returnMessage);
				}
			}

		} else if (pEventType == EEventType.ON_NEW_INTENT) {
			if (pAdditionParams != null && pAdditionParams.length == 2) {
				Intent intent = (Intent) pAdditionParams[0];
				PaymentGatewayActivity activity = (PaymentGatewayActivity) pAdditionParams[1];

				if (intent.getData() != null) {
					Uri uri = intent.getData();

					if (Log.IS_LOG_ENABLE) {
						Toast.makeText(activity, uri.toString(), Toast.LENGTH_LONG).show();
					}
					// Intent callback from browser (credit card)
					if (uri.getHost().equals("payment-cancel")) {
						terminateAndShowDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_credit_card_cancel));
					} else if (uri.getHost().equals("payment-complete")) {
						getStatus(mCreateOrderResponse.zmpTransID);
					}
				}
			}

		} else if (pEventType == EEventType.ON_RESUME) {
			if (pAdditionParams != null && pAdditionParams.length == 1
					&& pAdditionParams[0] instanceof PaymentGatewayActivity) {
				processing(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_resume_cc));
			}
		}

		return null;
	}

	@Override
	public void startPurchaseFlow() {
		TCreditCreateOrderTask task = new TCreditCreateOrderTask(this);
		task.execute();
	}

	@Override
	public void onClickSubmission() {
		startPurchaseFlow();
	}

	public String getMno() {
		return mPmcID;
	}
}
