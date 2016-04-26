/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.card.AdapterCard.java
 * Created date: Dec 26, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.card;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.DResponseGetStatus;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.utils.StringUtil;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelGroup;
import vn.zing.pay.zmpsdk.view.custom.VPaymentEditText;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;

/**
 * 
 * @author YenNLH, HuyPVA
 * 
 */
public class AdapterCard extends AdapterBase {
	private static final String ADAPTER_NAME = "zpsdk_card";

	public VPaymentEditText mCardCode;
	public VPaymentEditText mCardSerialNo;

	public String mCurrentPmcID;

	public AdapterCard(PaymentChannelActivity pOwnerActivity) {
		super(pOwnerActivity);
	}

	public void init() {
		// Other
		mCardCode = (VPaymentEditText) mOwnerActivity.findViewById(Resource.id.cardCode);
		mCardSerialNo = (VPaymentEditText) mOwnerActivity.findViewById(Resource.id.cardSerialNo);

		if (mCardCode != null && mCardSerialNo != null) {
			mCardCode.addTextChangedListener(mTextWatcher);
			mCardSerialNo.addTextChangedListener(mTextWatcher);
			mCardSerialNo.setOnEditorActionListener(mEditorActionListener);
		}

		mOwnerActivity.enableSubmitBtn(false);
	}

	@Override
	public String getChannelID() {
		return mCurrentPmcID;
	}
	
	@Override
	public String getChannelName() {
		return mCurrentPmcID;
	}

	@Override
	public String getPageName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getLayoutID() {
		return Resource.layout.zpsdk_merge_card;
	}

	@Override
	public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
		try {
			if (pEventType == EEventType.ON_SELECT) {
				VPaymentChannelButton view = (VPaymentChannelButton) pAdditionParams[0];
				mCurrentPmcID = view.getPmcID();
			} else if (pEventType == EEventType.ON_SUBMIT_COMPLETED) {
				DialogManager.closeProcessDialog();

				if (pAdditionParams == null || pAdditionParams.length == 0) {
					DialogManager
							.showAlertDialog(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_no_connection));
					return null;
				}

				DResponseGetStatus response = (DResponseGetStatus) pAdditionParams[0];

				// ////////////////////////////
				// QUICK RESULT IN RESPONSE //
				// ////////////////////////////
				if (response.returnCode == 1) {
					// Successfull
					GlobalData.getPaymentInfo().amount = response.ppValue;
					success(getSuccessMessage(response), response.zmpTransID);
				} else if (response.returnCode < 1) {
					// Error
					DialogManager.showAlertDialog(response.returnMessage);
				}

				// //////////////////////////
				// CONTINUE TO GET STATUS //
				else if (response.returnCode > 1) {
					getStatus(response.zmpTransID);
				}
			}
		} catch (Exception ex) {
			Log.e("Zmp", ex);
		}
		return null;
	}

	@Override
	public void onFinish() {
		VPaymentChannelGroup channelGroup = (VPaymentChannelGroup) mOwnerActivity
				.findViewById(Resource.id.payment_channel_adapter);
		if (channelGroup != null) {
			channelGroup.dispose();
			channelGroup = null;
		}
	}

	@Override
	public void startPurchaseFlow() {
	}

	@Override
	public String getSuccessMessage(DResponseGetStatus pResponse) {
		return GlobalData.getStringResource(Resource.string.zingpaysdk_alert_transaction_success) + " "
				+ StringUtil.longToStringNoDecimal(pResponse.ppValue) + " VNĐ";
	}

	public String getCardCode() {
		return mCardCode.getString();
	}

	public String getCardSerialNo() {
		return mCardSerialNo.getString();
	}

	@Override
	public void onClickSubmission() {

		// Check pattern
		if (mCardCode.checkPattern() && mCardSerialNo.checkPattern()) {

			String cardCode = getCardCode();
			String cardSerialNo = getCardSerialNo();
			Log.i("Zmp", "AdapterCard.onOkClick submit cardCode=" + cardCode + ", cardSerialNo=" + cardSerialNo);

			try {
				TSubmitCardTask submitCardTask = new TSubmitCardTask(this);
				submitCardTask.execute();
			} catch (Exception e) {
				Log.e("Zmp", "AdapterCard.submitCard error");
			}
		}
	}

	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (!TextUtils.isEmpty(getCardCode()) && !TextUtils.isEmpty(getCardSerialNo())) {
				mOwnerActivity.enableSubmitBtn(true);
			} else {
				mOwnerActivity.enableSubmitBtn(false);
			}
		}
	};

	private OnEditorActionListener mEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onClickSubmission();
			}
			return false;
		}
	};
}
