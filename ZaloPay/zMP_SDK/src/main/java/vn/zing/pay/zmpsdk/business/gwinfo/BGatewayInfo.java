/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.gwinfo.GatewayInfo.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.gwinfo;

import java.io.File;

import android.text.TextUtils;

import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.data.base.SingletonBase;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DGatewayInfo;
import vn.zing.pay.zmpsdk.listener.ZPGetGatewayInfoListener;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * @author YenNLH
 * 
 */
public class BGatewayInfo extends SingletonBase {
	private static BGatewayInfo mGatewayInfo = null;

	/**
	 * Please use this singleton to avoid to run multi-task running on the same
	 * work getting gateway information
	 * 
	 * @return {@link BGatewayInfo} instance
	 */
	public static synchronized BGatewayInfo getInstance() {
		if (BGatewayInfo.mGatewayInfo == null)
			BGatewayInfo.mGatewayInfo = new BGatewayInfo();

		return BGatewayInfo.mGatewayInfo;
	}

	private ZPGetGatewayInfoListener mCallback;
	private boolean mIsGetGatewayInfo;

	public BGatewayInfo() {
		super();
	}

	public synchronized void execute(ZPGetGatewayInfoListener pListener) {
		this.mCallback = pListener;

		long currentTime = System.currentTimeMillis();
		long expiredTime = SharedPreferencesManager.getInstance().getGatewayInfoExpriedTime();

		String checksumSDKV = SharedPreferencesManager.getInstance().getChecksumSDKversion();
		
		Log.i(this, "==== BGatewayInfo.execute ====");

		if (currentTime > expiredTime || !Constants.VERSION.equals(checksumSDKV) || !isValidConfig()) {

			this.mCallback.onProcessing();

			// Check if the task has finished yet?
			if (!this.mIsGetGatewayInfo) {
				this.mIsGetGatewayInfo = true;

				Log.d(getClass().getName(), "Get PAYMENT_INFO from server");
				TGetGatewayInfoTask task = new TGetGatewayInfoTask(mListener);
				task.execute();
			}
		} else {
			initResource();
		}

	}

	private void initResource() {
		TInitResourceTask initResourceTask = new TInitResourceTask(this.mCallback);
		initResourceTask.execute();
	}

	private ZPGetGatewayInfoListener mListener = new ZPGetGatewayInfoListener() {

		@Override
		public void onSuccess() {
			// load json
			initResource();
			BGatewayInfo.this.mIsGetGatewayInfo = false;
		}

		@Override
		public void onProcessing() {
			mCallback.onProcessing();
		}

		@Override
		public void onError(DGatewayInfo pMessage) {
			mCallback.onError(pMessage);
			BGatewayInfo.this.mIsGetGatewayInfo = false;
		}
	};

	public static boolean isValidConfig() {
		String path = SharedPreferencesManager.getInstance().getUnzipPath();
		File file = new File(path + File.separator + "config.json");
		// Check if res is missing ??
		return !TextUtils.isEmpty(path) && file.exists();
	}
}
