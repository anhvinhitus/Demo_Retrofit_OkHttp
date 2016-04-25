/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.business.pay123.GuiProcessor123Pay.java
 * Created date: Jan 27, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.pay123;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;

/**
 * @author YenNLH
 * 
 */
public class GuiProcessor123Pay {
	private PaymentChannelActivity mOwnerActivity;
	private Adapter123Pay mAdapter123Pay;
	private View mInternalCtl;
	private View mExternalCtl;

	public GuiProcessor123Pay(PaymentChannelActivity pActivity, Adapter123Pay pAdapter123Pay) {
		mOwnerActivity = pActivity;
		mAdapter123Pay = pAdapter123Pay;

		init();
	}

	public void init() {
		mOwnerActivity.setPaymentMethodName("Ví 123Pay");

//		mInternalCtl = mOwnerActivity.findViewById(Resource.id.zpsdk_internal_123pay_ctl);
//		mExternalCtl = mOwnerActivity.findViewById(Resource.id.zpsdk_external_123pay_ctl);

		mInternalCtl.setOnClickListener(mOnClickInternal);
		mExternalCtl.setOnClickListener(mOnClickExternal);
	}

	public void renderQrCode(String pInput) {
		try {
//			QRCodeWriter writer = new QRCodeWriter();
//			BitMatrix matrix = writer.encode(pInput, BarcodeFormat.QR_CODE, 800, 800);
//			((ImageView) mOwnerActivity.findViewById(Resource.id.zpsdk_image_qr_code_ctl)).setImageBitmap(toBitmap(matrix));
//
//			mOwnerActivity.findViewById(Resource.id.view_root).setVisibility(View.GONE);
//			mOwnerActivity.findViewById(Resource.id.view_qr_code).setVisibility(View.VISIBLE);
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}

	/**
	 * Writes the given Matrix on a new Bitmap object.
	 * 
	 * @param matrix
	 *            the matrix to write.
	 * @return the new {@link Bitmap}-object.
	 */
	public Bitmap toBitmap(BitMatrix matrix) {
		int height = matrix.getHeight();
		int width = matrix.getWidth();
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
			}
		}
		return bmp;
	}

	private OnClickListener mOnClickInternal = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// mInternalIcon.setImageDrawable(ContextCompat.getDrawable(mOwnerActivity,
			// Resource.drawable.ico_123pay_wallet));
			// mExternalIcon
			// .setImageDrawable(ContextCompat.getDrawable(mOwnerActivity,
			// Resource.drawable.ico_123pay_wallet_gray));
			// mAdapter123Pay.onEvent(EEventType.ON_SELECT, 1);
			mAdapter123Pay.onEvent(EEventType.ON_SELECT, 1);
		}
	};

	private OnClickListener mOnClickExternal = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// mExternalIcon.setImageDrawable(ContextCompat.getDrawable(mOwnerActivity,
			// Resource.drawable.ico_123pay_wallet));
			// mInternalIcon
			// .setImageDrawable(ContextCompat.getDrawable(mOwnerActivity,
			// Resource.drawable.ico_123pay_wallet_gray));
			// mAdapter123Pay.onEvent(EEventType.ON_SELECT, 2);
			mAdapter123Pay.onEvent(EEventType.ON_SELECT, 2);
		}
	};
}
