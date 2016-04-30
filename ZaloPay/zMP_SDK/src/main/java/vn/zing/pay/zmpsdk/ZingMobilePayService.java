/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.ZingPayService.java
 * Created date: Dec 11, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk;

import java.util.List;

import vn.zing.pay.zmpsdk.business.gwinfo.BGatewayInfo;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.base.SingletonLifeCircleManager;
import vn.zing.pay.zmpsdk.entity.ZPPaymentInfo;
import vn.zing.pay.zmpsdk.entity.ZPPaymentItem;
import vn.zing.pay.zmpsdk.entity.ZPPaymentOption;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DGatewayInfo;
import vn.zing.pay.zmpsdk.listener.ZPGetGatewayInfoListener;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.HMACUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.PaymentGatewayActivity;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import vn.zing.pay.zmpsdk.view.dialog.PaymentAlertDialog;
import vn.zing.pay.zmpsdk.data.Resource;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author YenNLH
 * 
 */
public class ZingMobilePayService {

	public synchronized static void pay(Activity owner, ZPPaymentInfo info, ZPPaymentListener listener) {
		pay(owner, null, info, listener);
	}

	public synchronized static void pay(Activity owner, EPaymentChannel paymentMethodType, ZPPaymentInfo info,
			ZPPaymentListener listener) {
		ZPPaymentOption option =  new ZPPaymentOption(paymentMethodType/*, null*/);
		pay(owner, info, option, listener);
	}

	/**
	 * NOTE: DEPRECATED 
	 */
	private synchronized static void pay(final Activity owner, final ZPPaymentInfo info, final ZPPaymentOption option,
			ZPPaymentListener listener) {

		try {
			GlobalData.setApplication(owner, listener, info, option);
		} catch (Exception e) {
			Log.e("pay", e);
		}

		if (!ConnectionUtil.isOnline(owner)) {
			Toast.makeText(owner,
					GlobalData.getStringResource(Resource.string.zingpaysdk_alert_no_connection),
					Toast.LENGTH_LONG).show();
			SingletonLifeCircleManager.disposeAll();
			return;
		}

		String validateMessage;
		if ((validateMessage = validatePaymentParams(info)) != null) {
			// Inform
			Toast.makeText(owner, validateMessage, Toast.LENGTH_LONG).show();
			if (listener != null) {
				listener.onCancel();
			}
			return;
		}

		BGatewayInfo.getInstance().execute(new ZPGetGatewayInfoListener() {

			@Override
			public void onSuccess() {
				DialogManager.closeProcessDialog();

				Intent intent = new Intent(owner, PaymentGatewayActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				owner.startActivity(intent);
			}

			@Override
			public void onProcessing() {
				try {
					DialogManager.showProcessDialog(null,
							GlobalData.getStringResource(Resource.string.zingpaysdk_alert_processing));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(DGatewayInfo pMessage) {
				// Close dialog and remove all static instance if catch any
				// error during this first step.
				DialogManager.closeProcessDialog();

				if (pMessage != null && pMessage.returnCode < 0) {
					PaymentAlertDialog alertDialog = new PaymentAlertDialog(owner);
					alertDialog.showAlert(pMessage.returnMessage);
				} else {
					PaymentAlertDialog alertDialog = new PaymentAlertDialog(owner);
					alertDialog.showNetworkErrorAlert();
				}
				SingletonLifeCircleManager.disposeAll();
			}
		});
	}

	/**
	 * 
	 * @param pPaymentInfo
	 * @param pSecurityMode
	 * @param pSecretKey
	 * @return
	 */
	public static String generateHMAC(ZPPaymentInfo pPaymentInfo, int pSecurityMode, String pSecretKey) {
		StringBuilder stringBuilder = new StringBuilder(256);
		stringBuilder.append(pPaymentInfo.appID)/* appID */
		.append('|').append(pPaymentInfo.appTransID)/* appTransID */
		.append('|').append(pPaymentInfo.appUser)/* appUser */
		.append('|').append(pPaymentInfo.appTime)/* appTime */
		.append('|').append(buildItemMac(pPaymentInfo.items))/* item MAC */
		.append('|').append(pPaymentInfo.embedData)/* embedData */;
		// .append('|')
		// .append(pPaymentInfo.amount)/* amount */;

		String hmac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACS.get(pSecurityMode), pSecretKey,
				stringBuilder.toString());
		Log.i("~~~~ HMAC - input: ", stringBuilder.toString());
		Log.i("~~~~ HMAC - output: ", stringBuilder.toString());

		return hmac;
	}

	private static String buildItemMac(List<ZPPaymentItem> pPaymentItems) {
		if (pPaymentItems != null) {
			StringBuilder sb = new StringBuilder(256);

			for (ZPPaymentItem paymentItem : pPaymentItems) {
				sb.append(paymentItem.itemID).append(".").append(paymentItem.itemName).append(".")
						.append(paymentItem.itemPrice).append(".").append(paymentItem.itemQuantity);
			}
			return sb.toString();
		}
		return "";
	}

	/**
	 * Validate {@link ZPPaymentInfo} object of current transaction.
	 * 
	 * @param info
	 *            {@link ZPPaymentInfo} object hold all information about
	 *            current transaction.
	 * 
	 * @return {@code TRUE} if valid, otherwise {@code FALSE}
	 */
	private static String validatePaymentParams(ZPPaymentInfo info) {
		if (info == null) {
			return GlobalData.getStringResource(Resource.string.zingpaysdk_missing_app_pmt_info);
		}

		if (TextUtils.isEmpty(info.appUser))
			return GlobalData.getStringResource(Resource.string.zingpaysdk_missing_app_user);

		if (!info.appTransID.matches("[\\w-]{1,50}"))
			return GlobalData.getStringResource(Resource.string.zingpaysdk_invalid_app_trans);

		if (info.appTime <= 0)
			return GlobalData.getStringResource(Resource.string.zingpaysdk_invalid_app_time);

		if (info.amount < 0)
			return GlobalData.getStringResource(Resource.string.zingpaysdk_invalid_app_amount);

		if (info.items != null) {
			if (info.amount == 0)
				return GlobalData
						.getStringResource(Resource.string.zingpaysdk_invalid_app_items_vs_amount);

			int amount = 0;
			for (ZPPaymentItem item : info.items) {
				if (!item.itemID.matches("[\\w-\\.]{1,100}"))
					return GlobalData.getStringResource(Resource.string.zingpaysdk_invalid_app_itemID);

				if (!item.itemName.matches(".{1,100}"))
					return GlobalData
							.getStringResource(Resource.string.zingpaysdk_invalid_app_itemName);

				if (item.itemPrice <= 0)
					return GlobalData
							.getStringResource(Resource.string.zingpaysdk_invalid_app_itemPrice);

				if (item.itemQuantity <= 0)
					return GlobalData
							.getStringResource(Resource.string.zingpaysdk_invalid_app_itemQuantity);

				amount += item.itemPrice * item.itemQuantity;
			}

			if (amount != info.amount) {
				return GlobalData.getStringResource(Resource.string.zingpaysdk_invalid_app_itemID);
			}
		}/*
		 * else { return
		 * GlobalData.getStringResource(Resource.string
		 * .zingpaysdk_invalid_app_items_vs_amount); }
		 */

		return null;
	}
}
