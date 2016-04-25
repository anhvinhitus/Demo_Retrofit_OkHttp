/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.ActivityRendering.java
 * Created date: Dec 22, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view;

import java.util.List;
import java.util.Map.Entry;

import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicEditText;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DStaticView;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DStaticViewGroup;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelGroup;
import vn.zing.pay.zmpsdk.view.custom.VPaymentEditText;
import android.app.Activity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;

/**
 * @author YenNLH
 * 
 */
public class ActivityRendering {
	public static final int PAYMENT_CHANNEL_LIST_ID = Resource.getID(Resource.id.payment_channel_adapter);

	private ResourceManager mResourceManager = null;
	private BasePaymentActivity mOwnerActivity = null;

	public ActivityRendering(ResourceManager pResourceManager, BasePaymentActivity pOwnerActivity) {
		this.mResourceManager = pResourceManager;
		this.mOwnerActivity = pOwnerActivity;
	}

	/**
	 * @return the mOwnerActivity
	 */
	public Activity getOwnerActivity() {
		return mOwnerActivity;
	}

	/**
	 * @param mOwnerActivity
	 *            the mOwnerActivity to set
	 */
	public void setOwnerActivity(BasePaymentActivity mOwnerActivity) {
		this.mOwnerActivity = mOwnerActivity;
	}

	public void render() {
		if (mResourceManager == null)
			return;
		
		render(mResourceManager.getStaticView(), mResourceManager.getDynamicView());
	}

	public void render(DStaticViewGroup pStaticViewGroup, DDynamicViewGroup pDynamicViewGroup) {
		if (pStaticViewGroup == null && pDynamicViewGroup == null)
			return;
		
		View contentView = this.mOwnerActivity.findViewById(android.R.id.content).getRootView();

		if (contentView != null) {
			renderStaticView(pStaticViewGroup);
			renderDynamicView(pDynamicViewGroup);
		}
	}

	private void renderStaticView(DStaticViewGroup pStaticViewGroup) {
		if (pStaticViewGroup != null) {
			renderImageView(mOwnerActivity, pStaticViewGroup.ImageView);
			renderTextView(mOwnerActivity, pStaticViewGroup.TextView);
		}
	}

	private void renderDynamicView(DDynamicViewGroup pDynamicViewGroup) {
		if (pDynamicViewGroup != null) {

			if (pDynamicViewGroup.SelectionView != null) {
				VPaymentChannelGroup paymentChannelList = (VPaymentChannelGroup) mOwnerActivity
						.findViewById(PAYMENT_CHANNEL_LIST_ID);
				if (paymentChannelList != null) {
					paymentChannelList.render(pDynamicViewGroup.SelectionView, false);

					ScrollView scrollView = (ScrollView) mOwnerActivity.findViewById(Resource.id.atm_listsv);
					if (scrollView != null)
						scrollView.setVerticalFadingEdgeEnabled(true);
				}
			}

			if (pDynamicViewGroup.EditText != null && pDynamicViewGroup.EditText.size() > 0) {
				for (DDynamicEditText editText : pDynamicViewGroup.EditText) {
					View view = mOwnerActivity.findViewById(mOwnerActivity.getViewID(editText.id));
					if (view instanceof VPaymentEditText && mOwnerActivity instanceof PaymentChannelActivity) {
						VPaymentEditText paymentEditText = (VPaymentEditText) view;
						paymentEditText.init(editText, ((PaymentChannelActivity) mOwnerActivity).getAdapter());
					}
				}
			}

			if (pDynamicViewGroup.View != null && pDynamicViewGroup.View.size() > 0) {
				View view = null;
				for (Entry<String, Boolean> entry : pDynamicViewGroup.View.entrySet()) {
					if (view instanceof EditText && entry.getValue()) {
						((EditText) view).setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
					}
					
					view = mOwnerActivity.setView(entry.getKey(), entry.getValue());
					
					if (view instanceof EditText && entry.getValue()) {
						((EditText) view).setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
					}
				}
			}
		}
	}

	private void renderImageView(BasePaymentActivity pActivity, List<DStaticView> pImgViewList) {
		if (pImgViewList == null) {
			return;
		}
		for (DStaticView imgView : pImgViewList) {
			pActivity.setImage(imgView.id, ResourceManager.getImage(imgView.value));
		}
	}

	private void renderTextView(BasePaymentActivity pActivity, List<DStaticView> pTxtViewList) {
		if (pTxtViewList == null) {
			return;
		}
		for (DStaticView txtView : pTxtViewList) {
			pActivity.setText(txtView.id, txtView.value);
		}
	}
}
