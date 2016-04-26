/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.business.pay123.Adapter123Pay.java
 * Created date: Jan 27, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.pay123;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.helper.google.Base64;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;

/**
 * @author YenNLH
 * 
 */
public class Adapter123Pay extends AdapterBase {

	private static final String ADAPTER_NAME = "zpsdk_123pay";
	private GuiProcessor123Pay mGuiProcessor = null;
	private int mSelectedType = 0;

	public Adapter123Pay(PaymentChannelActivity pOwnerActivity) {
		super(pOwnerActivity);
	}

	@Override
	public void init() {
		mGuiProcessor = new GuiProcessor123Pay(mOwnerActivity, this);
	}

	@Override
	public String getChannelID() {
		return null;
	}
	
	@Override
	public String getChannelName() {
		return null;
	}

	@Override
	public String getPageName() {
		return ADAPTER_NAME;
	}

	@Override
	public String getLayoutID() {
		return Resource.layout.zpsdk_123pay;
	}

	@Override
	public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
		if (pEventType == EEventType.ON_SELECT) {
			mSelectedType = (int) pAdditionParams[0];
			IntentData intentData = new IntentData();
			intentData.account = GlobalData.getPaymentInfo().appUser;
			intentData.amount = GlobalData.getPaymentInfo().amount;
			intentData.appName = SharedPreferencesManager.getInstance().getAppName();
			intentData.orderName = GlobalData.getPaymentInfo().displayName;
			intentData.disp = GlobalData.getPaymentInfo().displayInfo;

			if (mSelectedType == 1) {
				Intent sendIntent = new Intent("vn.zing.pay.demo123pay.PAYMENT_ACTIVITY");
				sendIntent.putExtra(Intent.EXTRA_TEXT, intentData.toJsonString());
				sendIntent.setType("text/plain");
				mOwnerActivity.startActivityForResult(sendIntent, 12345);
			} else {
				try {
					mGuiProcessor.renderQrCode(Base64.encode(intentData.toJsonString().getBytes("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else if (pEventType == EEventType.ON_ACTIVITY_RESULT) {
			if ((int) pAdditionParams[0] == 12345 && (int) pAdditionParams[1] == Activity.RESULT_OK) {
				success(null, "123PayTest");
			}
		}

		return null;
	}

	@Override
	public void onFinish() {
	}

	@Override
	public void startPurchaseFlow() {
	}

	@Override
	public void onClickSubmission() {
	}

}
