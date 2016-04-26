/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.PaymentGatewayActivity.java
 * Created date: Dec 21, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;

import vn.zing.pay.zmpsdk.business.atm.TAtmGetBankListTask;
import vn.zing.pay.zmpsdk.business.creditcard.AdapterCreditCard;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.data.base.SingletonLifeCircleManager;
import vn.zing.pay.zmpsdk.entity.ZPPaymentInfo;
import vn.zing.pay.zmpsdk.entity.ZPPaymentOption;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DGroupPaymentChannel;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DPaymentChannel;
import vn.zing.pay.zmpsdk.listener.ZPOnClickListener;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.DimensionUtil;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.utils.StringUtil;
import vn.zing.pay.zmpsdk.view.dialog.PaymentAlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author YenNLH
 * 
 */
public class PaymentGatewayActivity extends BasePaymentActivity {

	public static final int RC_REQUEST_CHANNEL_ACTIVITY = 40001;
	public static final int RC_REQUEST_FINISH_ACTIVITY = 40002;
	public static final String PAGE_NAME = "zpsdk_payment_gateway";

	static boolean isChooseAnotherChanelClicked;
	// ////////////////////////////////////////////

	private ZPPaymentInfo mPaymentInfo;

	// ////////////////////////////////////////////
	private LinearLayout listPayment;
	private HashMap<String, View> mPmcGroupViewMap = new HashMap<String, View>();
	private View chooseAnotherChannel;

	// ////////////////////////////////////////////
	private HashMap<String, DGroupPaymentChannel> mPmcGroupConfigMap = new HashMap<String, DGroupPaymentChannel>();

	private int mNumOfChannelVisible = 0;
	private boolean mIsClickedPmcGroup = false;

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
		// overridePendingTransition(Resource.anim.trans_left_in,
		// Resource.anim.trans_left_out); //Fix screen flickering on some device
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Object tempObj = GlobalData.tempObject;
		GlobalData.tempObject = null;
		if (tempObj instanceof AdapterCreditCard) {
			((AdapterCreditCard) tempObj).onEvent(EEventType.ON_NEW_INTENT, intent, this);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (GlobalData.getOwnerActivity() == null) {
			finish();
			return;
		}

		// Lost connection
		if (!ConnectionUtil.isOnline(this)) {
			Toast(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_no_connection), Toast.LENGTH_SHORT);
			finish();
			GlobalData.getPaymentListener().onCancel();
			return;
		}

		// Tracking
		GlobalData.getDefaultTracker().trackScreen(getClass().getSimpleName(), true);

		/**************************
		 * GET BANK LIST TO CACHE *
		 **************************/
		TAtmGetBankListTask getBankListTask = new TAtmGetBankListTask(null);
		getBankListTask.execute();

		try {

			/*******************
			 * GET PAYMENT INFO
			 *******************/
			mPaymentInfo = GlobalData.getPaymentInfo();

			/*********************************
			 * GET & SET ALL VIEW COMPONENTS *
			 *********************************/
			setContentView(Resource.getLayout(Resource.layout.zpsdk_payment_gateway));

			listPayment = (LinearLayout) findViewById(Resource.id.list_payment);
			listPayment.setVisibility(View.VISIBLE);

			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_telco),
					findViewById(Resource.id.zpsdk_mobile_card_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_zing_card),
					findViewById(Resource.id.zpsdk_zingcard_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_merge_card),
					findViewById(Resource.id.zpsdk_merge_card_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_atm),
					findViewById(Resource.id.zpsdk_atm_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_sms),
					findViewById(Resource.id.zpsdk_sms_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_google_wallet),
					findViewById(Resource.id.zpsdk_google_wallet_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_credit_card),
					findViewById(Resource.id.zpsdk_credit_ctl));
			mPmcGroupViewMap.put(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_123pay),
					findViewById(Resource.id.zpsdk_123pay_ctl));

			chooseAnotherChannel = findViewById(Resource.id.zpsdk_choose_another_channel);

			// View imgCredit = findViewById(Resource.id.zpsdk_ic_cardcredit);

			// Hide maintenance
			findViewById(Resource.id.payment_maintenance).setVisibility(View.GONE);

			/************************
			 * GET APPLICATION INFO *
			 ************************/
			loadApplicationInfo();
			showApplicationInfo();

			showAmount();
			showDisplayInfo();
			resizeHeader();

			// ///// ONCLICK EXIT /////////
			findViewById(Resource.id.zpsdk_exit_ctl).setOnClickListener(this.mOnClickExitListener);

			// ///// ONCLICK CHANNEL /////////
			for (View pmcGroupView : mPmcGroupViewMap.values()) {
				if (pmcGroupView != null) {
					pmcGroupView.setOnClickListener(this.mOnClickListener);
				}
			}
			chooseAnotherChannel.setOnClickListener(this.mOnClickMoreListener);

			// Remove back button
			if (findViewById(Resource.id.channel_back) != null)
				findViewById(Resource.id.channel_back).setVisibility(View.INVISIBLE);

			// Init config
			DGroupPaymentChannel parser = new DGroupPaymentChannel();
			String pmcGroupList = SharedPreferencesManager.getInstance().getGChannelConfigList();
			for (String pmcGroupID : pmcGroupList.split(Constants.COMMA)) {
				mPmcGroupConfigMap
						.put(pmcGroupID,
								parser.fromJsonString(SharedPreferencesManager.getInstance().getGroupChannelConfig(
										pmcGroupID)));
			}

			// ///////////// RENDER ////////////////
			ResourceManager.getInstance(PAGE_NAME).produceRendering(this).render();

			Log.i("PaymentGatewayActivity", "Get in caching !!");
			initGateWayChannel();
			orderPaymentChannel();

			if (isChooseAnotherChanelClicked) {
				chooseAnotherChannel.performClick();
			}

		} catch (Exception ex) {

		}
	}

	private void initGateWayChannel() throws JSONException {
		findViewById(Resource.id.gateway_root).setVisibility(View.VISIBLE);
		inflatePaymentChannel();
	}

	/**
	 * Custom viewers on the screen.
	 * 
	 * Base on the respone of server, this will decice what buttons will be
	 * shown for making a purchase.
	 * 
	 * Note: There is a mergeView including 2 payment channel (telco-card &
	 * zing-card). It will be shown in the case of both telco-zing-card
	 * visibility.
	 */
	private void inflatePaymentChannel() {

		if (isMaintenance()) {
			showMaintenanceView();
			return;
		}

		/*****************************************************
		 * PAYMENT OPTIONS: INCLUDE OR EXCLUDE SOME CHANNELs *
		 *****************************************************/
		ZPPaymentOption paymentOption = GlobalData.getPaymentOption();
		SparseArray<ArrayList<String>> pmcConfigList = SharedPreferencesManager.getInstance().getPmcConfigList();

		if (paymentOption != null) {
			if (paymentOption.getIncludePaymentMethodType() != null) {

				String pmcIDStr = paymentOption.getIncludePaymentMethodType();

				for (int i = 0; i < pmcConfigList.size(); i++) {
					int groupId = pmcConfigList.keyAt(i);
					// get the object by the key.
					ArrayList<String> channelList = pmcConfigList.get(groupId);
					if (channelList.contains(pmcIDStr)) {
						continue;
					} else {
						mPmcGroupViewMap.remove(String.valueOf(groupId));
						continue;
					}
				}
			} else if (paymentOption.getExcludePaymentMethodTypes() != null) {

				HashSet<String> removedPmcIdSet = paymentOption.getExcludePaymentMethodTypes();

				for (int i = 0; i < pmcConfigList.size(); i++) {
					int groupId = pmcConfigList.keyAt(i);
					// get the object by the key.
					ArrayList<String> channelList = pmcConfigList.get(groupId);
					Iterator<String> iter = channelList.iterator();

					while (iter.hasNext()) {
						String entry = iter.next();
						if (removedPmcIdSet.contains(entry)) {
							iter.remove();
						}
					}

					if (channelList.size() == 0) {
						mPmcGroupViewMap.remove(String.valueOf(groupId));
					}
				}
			}
		}

		/******************************************
		 * REMOVE PAYMENT CHANNEL GROUP BY POLICY *
		 ******************************************/
		String sms = GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_sms);
		String iab = GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_google_wallet);
		for (Entry<String, DGroupPaymentChannel> pmcGroupConfig : mPmcGroupConfigMap.entrySet()) {
			if (pmcGroupConfig.getValue() != null) {
				boolean isRemoved = false;

				if (!pmcGroupConfig.getValue().isEnable()
						|| !pmcGroupConfig.getValue().isAmountSupport(GlobalData.getPaymentInfo().amount)) {
					isRemoved = true;
				} else {

					// // SMS ////
					if (sms.equalsIgnoreCase(pmcGroupConfig.getKey())
							&& !ConnectionUtil.isAbleTofSendSMS(getApplicationContext())) {
						isRemoved = true;

						// // GOOGLE INAPP BILLING ////
					} else if (iab.equalsIgnoreCase(pmcGroupConfig.getKey()) && !mPaymentInfo.verifyGooglePaymentInfo()) {
						isRemoved = true;
					}
				}

				if (isRemoved) {
					mPmcGroupViewMap.remove(pmcGroupConfig.getKey());
				} else {
					View pmcGroupView = mPmcGroupViewMap.get(pmcGroupConfig.getKey());
					if (pmcGroupView != null) {
						// //// PROMOTION //////
						ImageView promotionView = (ImageView) pmcGroupView.findViewWithTag(Constants.PROMOTION_TAG);
						if (promotionView != null) {
							if (pmcGroupConfig.getValue().isPromoted()) {
								promotionView.setVisibility(View.VISIBLE);
								String imgName = GlobalData.getStringResource(Resource.string.zingpaysdk_conf_promotion_img);
								if (imgName != null) {
									double discount = pmcGroupConfig.getValue().discount;
									if ((discount - ((int) (discount))) > 0) {
										imgName = String.format(imgName, discount);
									} else {
										imgName = String.format(imgName, (int) discount);
									}
									promotionView.setImageBitmap(ResourceManager.getImage(imgName));
								}
							} else {
								promotionView.setVisibility(View.GONE);
							}
						}
					}
				}
			}
		}

		Iterator<Entry<String, View>> iter = mPmcGroupViewMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, View> entry = iter.next();
			if (!mPmcGroupConfigMap.containsKey(entry.getKey())) {
				iter.remove();
			} else if (entry.getValue() != null) {
				entry.getValue().setVisibility(View.VISIBLE);
				// Store pmcGroupId
				entry.getValue().setTag(entry.getKey());
			}
		}

		mNumOfChannelVisible = mPmcGroupViewMap.size();

		if (mNumOfChannelVisible == 0) {
			showExitGateway();
		} else if (mNumOfChannelVisible == 1) {
			findViewById(Resource.id.gateway_root).setVisibility(View.GONE);

			Entry<String, View> pmcGroupView = mPmcGroupViewMap.entrySet().iterator().next();

			if (pmcGroupView.getValue() != null) {
				pmcGroupView.getValue().performClick();
			}
		}
	}

	private void orderPaymentChannel() {

		// Re-order by list from server
		int count = 1;
		int maxNumOfChannelVisible = (DimensionUtil.isScreenPortrait(this)) ? 5 : 4;

		String pmcGroupList = SharedPreferencesManager.getInstance().getGChannelConfigList();
		Log.e("**** orderPaymentChannel:", pmcGroupList);
		boolean isHided = false;

		for (String pmcGroupID : pmcGroupList.split(Constants.COMMA)) {
			View pmcGroupView = mPmcGroupViewMap.get(pmcGroupID);
			if (pmcGroupView != null) {
				listPayment.removeView(pmcGroupView);
				listPayment.addView(pmcGroupView);

				if (count > (maxNumOfChannelVisible - 1) && mNumOfChannelVisible > maxNumOfChannelVisible) {
					isHided = true;
					pmcGroupView.setVisibility(View.GONE);
				}
				count++;
			}
		}

		if (!isHided) {
			chooseAnotherChannel.setVisibility(View.GONE);
		} else {
			listPayment.removeView(chooseAnotherChannel);
			listPayment.addView(chooseAnotherChannel);
			chooseAnotherChannel.setVisibility(View.VISIBLE);
		}

		if (DimensionUtil.isScreenPortrait(this) && !TextUtils.isEmpty(mPaymentInfo.displayInfo)) {
			View v = findViewById(Resource.id.zpsdk_method_description);
			if (v != null) {
				v.setVisibility(View.VISIBLE);
				listPayment.removeView(v);
				listPayment.addView(v);
			}
		}
	}

	private void showMaintenanceView() {
		setViewVisible(Resource.id.gateway_root, View.VISIBLE);
		setViewVisible(Resource.id.payment_maintenance, View.VISIBLE);

		setViewVisible(Resource.id.sv_list_payment, View.GONE);
		listPayment.setVisibility(View.GONE);

		((TextView) findViewById(Resource.id.maintance_title)).setText(GlobalData
				.getStringResource(Resource.string.zingpaysdk_alert_maintance));
	}

	private long checkMinPPValue(long minPPValue, String pmcID) {
		DGroupPaymentChannel channel = mPmcGroupConfigMap.get(pmcID);
		if (channel != null) {
			if (minPPValue == -1 || (channel.minPPValue != -1 && channel.minPPValue < minPPValue)) {
				return channel.minPPValue;
			}
		} else {
			DPaymentChannel pmc = GsonUtils.fromJsonString(
					SharedPreferencesManager.getInstance().getPmcConfigByPmcID(pmcID), DPaymentChannel.class);
			if (pmc != null && (minPPValue == -1 || (pmc.minPPValue != -1 && pmc.minPPValue < minPPValue))) {
				return pmc.minPPValue;
			}
		}
		return minPPValue;
	}

	private void showExitGateway() {
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		long minPPValue = -1;

		if (mPmcGroupViewMap.isEmpty() && GlobalData.getPaymentOption().getIncludePaymentMethodType() != null) {
			minPPValue = checkMinPPValue(minPPValue, GlobalData.getPaymentOption().getIncludePaymentMethodType());
		} else {
			HashSet<String> removedPmcIdSet = (GlobalData.getPaymentOption() != null) ? GlobalData.getPaymentOption()
					.getExcludePaymentMethodTypes() : null;
			for (String key : mPmcGroupConfigMap.keySet()) {
				if (removedPmcIdSet != null && !removedPmcIdSet.contains(key)) {
					minPPValue = checkMinPPValue(minPPValue, key);
				}
			}
		}
		// for (DGroupPaymentChannel channel : mPmcGroupConfigMap.values()) {
		// if (minPPValue == -1 || (channel.minPPValue != -1 &&
		// channel.minPPValue < minPPValue))
		// minPPValue = channel.minPPValue;
		// }

		final ZPPaymentListener listener = GlobalData.getPaymentListener();
		PaymentAlertDialog alertDlg = new PaymentAlertDialog(GlobalData.getOwnerActivity());
		alertDlg.setListener(new ZPOnClickListener() {

			@Override
			public void onClickOK() {
				listener.onCancel();
			}

			@Override
			public void onClickCancel() {
			}
		});
		if (minPPValue > -1) {
			alertDlg.showAlert(String.format(
					GlobalData.getStringResource(Resource.string.zingpaysdk_alert_amount_not_support),
					StringUtil.longToStringNoDecimal(minPPValue) + " VNĐ"));
		} else {
			alertDlg.showAlert(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_all_excluded));
		}
		finish();
	}

	private boolean isOffChannel(DGroupPaymentChannel pPaymentChannel) {
		if (pPaymentChannel == null)
			return true;
		return !pPaymentChannel.isEnable();
	}

	/**
	 * Check if all of channels is not available
	 * 
	 * @return {@code TRUE} if all channels are off, {@code FALSE} otherwise
	 */
	private boolean isMaintenance() {
		for (DGroupPaymentChannel paymentChannel : mPmcGroupConfigMap.values()) {
			if (!isOffChannel(paymentChannel)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isForce() {
		return mNumOfChannelVisible == 1;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		Log.d("ZMP", "onBackPressed");
		mOnClickExitListener.onClick(null);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_REQUEST_CHANNEL_ACTIVITY && data != null) {
			if (resultCode == Constants.RESULT_OK) {

				final String message = data.getStringExtra(Constants.FINISH_ACT_RETURN_MESSAGE);
				PaymentAlertDialog alertDialog = new PaymentAlertDialog(
				/*
				 * (mNumOfChannelVisible == 1) ? GlobalData.getOwnerActivity() :
				 */PaymentGatewayActivity.this);

				alertDialog.showAlert(message);
				alertDialog.setListener(new ZPOnClickListener() {
					@Override
					public void onClickOK() {

						ZPPaymentResult paymentResult = GlobalData.paymentResult;

						GlobalData.getPaymentListener().onComplete(paymentResult);

						PaymentGatewayActivity.this.finish();
					}

					@Override
					public void onClickCancel() {
					}
				});

			} else if (resultCode == Constants.RESULT_BACK) {
				// Do nothing
				// This action was performed by clear intention of previous
				// Activity's controller
			} else if (resultCode == Constants.RESULT_EXIT) {
				mOnClickExitListener.onClick(null);
			}
		} else {
			// User clicked back-button
			if (isForce()) {
				mOnClickExitListener.onClick(null);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(this, "==== onResume ====");

		if (mNumOfChannelVisible == 1 && mIsClickedPmcGroup) {
			// mOnClickExitListener.onClick(null);
		}

		Object tempObj = GlobalData.tempObject;
		GlobalData.tempObject = null;
		if (tempObj instanceof AdapterCreditCard) {
			((AdapterCreditCard) tempObj).onEvent(EEventType.ON_RESUME, this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(this, "====== onDestroy ======");

		if (mIsClickedPmcGroup) {
			GlobalData.getDefaultTracker().trackScreen("Exit", false);
		}

		SingletonLifeCircleManager.disposeAll();
	}

	// //////////////////////////////////////////////
	// ///////////////// LISTENER ///////////////////
	// //////////////////////////////////////////////

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(PaymentGatewayActivity.this, PaymentChannelActivity.class);
			intent.putExtra(GlobalData.getStringResource(Resource.string.zingpaysdk_intent_key_channel), v.getId());
			intent.putExtra("channelVisible", mNumOfChannelVisible);

			if (mNumOfChannelVisible == 1) {
				intent.putExtra("forceOffForm", true);
			}

			mIsClickedPmcGroup = true;
			startActivityForResult(intent, RC_REQUEST_CHANNEL_ACTIVITY);
		}
	};

	private View.OnClickListener mOnClickExitListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			PaymentGatewayActivity.isChooseAnotherChanelClicked = false;
			finish();
			if (GlobalData.getPaymentListener() != null)
				GlobalData.getPaymentListener().onCancel();
		}
	};

	private View.OnClickListener mOnClickMoreListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			for (Entry<String, DGroupPaymentChannel> pmcGroupConfig : mPmcGroupConfigMap.entrySet()) {
				View pmcGroupView = mPmcGroupViewMap.get(pmcGroupConfig.getKey());
				if (pmcGroupView != null) {
					pmcGroupView.setVisibility(View.VISIBLE);
				}
				chooseAnotherChannel.setVisibility(View.GONE);
			}
		}
	};
}
