/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.gwinfo.InitResourceTask.java
 * Created date: Dec 21, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.gwinfo;

import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.listener.ZPGetGatewayInfoListener;
import vn.zing.pay.zmpsdk.utils.Log;
import android.os.AsyncTask;

/**
 * @author YenNLH
 * 
 */
public class TInitResourceTask extends AsyncTask<Void, Void, Boolean> {

	private ZPGetGatewayInfoListener mCallBack;

	public TInitResourceTask(ZPGetGatewayInfoListener pListener) {
		this.mCallBack = pListener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			ResourceManager.initResource();
			// Everything now is okay
			return true;
		} catch (Exception e) {
			Log.e(this,  e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result != null && result.booleanValue()) {
			this.mCallBack.onSuccess();
		} else {
			this.mCallBack.onError(null);
		}
	}
}
