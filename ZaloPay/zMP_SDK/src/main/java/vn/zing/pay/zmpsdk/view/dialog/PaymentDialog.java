/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.PaymentDialog.java
 * Created date: Dec 17, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view.dialog;

import vn.zing.pay.zmpsdk.data.Resource;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author YenNLH
 * 
 */
public class PaymentDialog extends Dialog {
	private Object tag;

	public PaymentDialog(Context context) {
		super(context);
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public Object getTag() {
		return tag;
	}

	public View findViewById(String pName) {
		return findViewById(Resource.getID(pName));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		setCancelable(false);
	}

	@Override
	public void onAttachedToWindow() {
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED 
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
}
