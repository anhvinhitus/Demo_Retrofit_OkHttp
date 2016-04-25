/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.PaymentChannelActivity.java
 * Created date: Dec 22, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view;

import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.AdapterFactory;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DStaticViewGroup;
import vn.zing.pay.zmpsdk.utils.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author YenNLH
 * 
 */
public class PaymentChannelActivity extends BasePaymentActivity {

	private AdapterBase mAdapter = null;
	private boolean mIsFirst = true;
	private boolean mIsRestart = false;

	private TextView mPaymentMethodName = null;
	private Button mSubmitButton;

	@Override
	protected void onDestroy() {
		Log.d(this, "==== onDestroy ====");
		super.onDestroy();

		if (isFinishing() && mAdapter != null) {
			mAdapter.onFinish();
			mAdapter = null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(this, "==== onNewIntent ====");
		super.onNewIntent(intent);

		Log.e(this, intent.toString());
		// this.fo
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(this, "==== onActivityResult ====");
		mAdapter.onEvent(EEventType.ON_ACTIVITY_RESULT, requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(this, "==== onCreate ====");
		mIsRestart = false;

		mAdapter = AdapterFactory.produce(this);

		renderActivity();
		mAdapter.init();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(this, "==== onRestart ====");
		mIsRestart = true;
	}

	@Override
	protected void onStart() {
		Log.d(this, "==== onStart ====");
		super.onStart();

		if (!mIsRestart) {
			if (mAdapter.isStartImmediately()) {
				mAdapter.startPurchaseFlow();
			}
		}
	}
	
	@Override
	protected void onResume() {
		Log.d(this, "==== onResume ====");
		super.onResume();
		
		if (!mIsFirst && mAdapter != null) {
			mAdapter.onEvent(EEventType.ON_RESUME, this);
		}
		
		mIsFirst = false;
	}

	public void renderActivity() {

		/********************
		 * SET CONTENT VIEW *
		 ********************/
		String layoutResID = mAdapter.getLayoutID();
		if (layoutResID != null)
			setContentView(Resource.getLayout(layoutResID));

		/************************
		 * GET APPLICATION INFO *
		 ************************/
		loadApplicationInfo();
		showApplicationInfo();

		showAmount();
		showDisplayInfo();
		resizeHeader();

		mPaymentMethodName = (TextView) findViewById(Resource.id.payment_method_name);
		mSubmitButton = (Button) findViewById(Resource.id.zpsdk_btn_submit);

		mAdapter.setOnclikListner(Resource.id.channel_back, onClickBackListener);
		mAdapter.setOnclikListner(Resource.id.zpsdk_exit_ctl, mOnClickExitListener);

		if (!mAdapter.isStartImmediately()) {
			renderByResource();
		}
		mAdapter.setListener();
	}

	public void renderByResource() {
		renderByResource(null, null);
	}

	public void renderByResource(DStaticViewGroup pAdditionStaticViewGroup, DDynamicViewGroup pAdditionDynamicViewGroup) {
		long time = System.currentTimeMillis();
		
		ResourceManager resourceManager = ResourceManager.getInstance(mAdapter.getPageName());

		if (resourceManager != null) {

			ActivityRendering acctivityRendering = resourceManager.produceRendering(this);
			if (acctivityRendering != null) {
				acctivityRendering.render();
				acctivityRendering.render(pAdditionStaticViewGroup, pAdditionDynamicViewGroup);
			} else {
				Log.i("Zmp", "PaymentChannelActivity.render acctivityRendering=null");
			}
		} else {
			Log.i("Zmp", "PaymentChannelActivity.render resourceManager=null");
		}

		enableSubmitBtn(false);
		mAdapter.setListener();
		
		Log.d(this, "++++ PaymentChannelActivity.renderByResource: Total time: " + (System.currentTimeMillis() - time));
	}

	public void setPaymentMethodName(String paymentMethodName) {
		if (mPaymentMethodName != null) {
			mPaymentMethodName.setText(paymentMethodName);
		}
	}

	public void enableSubmitBtn(boolean pIsEnabled) {
		setEnableButton(mSubmitButton, pIsEnabled);
	}

	public AdapterBase getAdapter() {
		return mAdapter;
	}

	public void setEnableButton(View pButtonView, boolean pIsEnabled) {
		if (pButtonView == null)
			return;

		if (pIsEnabled) {
			pButtonView.setEnabled(true);
			pButtonView.setBackgroundResource(Resource.getDrawable(Resource.drawable.zpsdk_border07));
		} else {
			pButtonView.setEnabled(false);
			pButtonView.setBackgroundResource(Resource.getDrawable(Resource.drawable.zpsdk_border15));
		}
	}

	protected final OnClickListener onClickBackListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.i("Zmp", "AdapterBase click_channel_back");
			finish();
		}
	};

	private View.OnClickListener mOnClickExitListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent returnIntent = new Intent();
			setResult(Constants.RESULT_EXIT, returnIntent);
			finish();
		}
	};
}
