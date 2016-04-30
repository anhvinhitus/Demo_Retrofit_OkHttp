/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.custom.VPaymentChannelList.java
 * Created date: Dec 29, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view.custom;

import java.util.ArrayList;

import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicSelectionViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicSelectionViewItem;
import vn.zing.pay.zmpsdk.utils.DimensionUtil;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * @author YenNLH
 * 
 */
public class VPaymentChannelGroup extends FrameLayout {

	private LayoutInflater mInflater;
	private LinearLayout mComponent = null;
	
	private VPaymentChannelButton mSlectedPmcButton = null;
	private ArrayList<VPaymentChannelButton> mChannelButtonList = null;
	
	private boolean mIsAutoSelect = true;
	private OnClickListener mOnSelectListener = null;

	public VPaymentChannelGroup(Context context) {
		super(context, null);
		init(null, 0);
	}

	public VPaymentChannelGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public VPaymentChannelGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	/**
	 * Get all view components and store them in attributes
	 */
	private View initializeLayout() {
		// Get all components
		mInflater = LayoutInflater.from(this.getContext().getApplicationContext());
		ViewGroup view = (ViewGroup) mInflater.inflate(Resource.getLayout(Resource.layout.zpsdk_custom_radio_btn_list), this, false);

		mComponent = (LinearLayout) view.findViewById(Resource.getID(Resource.id.selection_adapter));

		// inflate content layout and add it to the relative layout as second
		// child add as second child, therefore pass index 1 (0,1,...)

		// mComponent.addView(inflater.inflate(Resource.layout.zpsdk_custom_group_selection2,
		// view, false));
		// mComponent.addView(inflater.inflate(Resource.layout.zpsdk_custom_group_selection3,
		// view, false));
		mComponent.addView(mInflater.inflate(Resource.getLayout(Resource.layout.zpsdk_custom_group_selection4), view, false));

		view.removeAllViews();

		addView(mComponent);

		return view;
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Init layout
		this.initializeLayout();
		// Load attributes
		// final TypedArray a = getContext().obtainStyledAttributes(attrs,
		// Resource.styleable.VPaymentChannelButton, defStyle, 0);

		// a.recycle();

		this.setClickable(true);
		this.setEnabled(true);
		// this.addView(view);
	}

	public void render(DDynamicSelectionViewGroup selectionView, boolean pIsSetGone) {
		mComponent.removeAllViews();

		if (selectionView == null || selectionView.items == null)
			return;

		if (selectionView.items.size() == 1) {
			VPaymentChannelButton channelButton = new VPaymentChannelButton(this.getContext());
			channelButton.setPmcID(selectionView.items.get(0).pmcID);
			channelButton.setPmcName(selectionView.items.get(0).pmcName);
			channelButton.setTag(selectionView.items.get(0).tag);
			onSelect(channelButton);
		}
		
		mIsAutoSelect = selectionView.isAutoSelect;

		// ///////////////////////////////////////////////////////////////////////////////////////

		if (mChannelButtonList == null) {
			mChannelButtonList = new ArrayList<VPaymentChannelButton>();
		} else {
			mChannelButtonList.clear();
		}

		boolean isPortrait = DimensionUtil.isScreenPortrait((Activity) this.getContext());
		int breakLine = (selectionView.isDefaultBreakLine()) ? (isPortrait ? (selectionView.items.size() == 3 ? 3 : 2)
				: 4) : selectionView.breakLine;
		int numOfRow = (selectionView.items.size() / breakLine)
				+ ((selectionView.items.size() % breakLine) > 0 ? 1 : 0);

		for (int i = 1; i <= numOfRow; i++) {
			ViewGroup row = null;
			row = inflateButtonRow(breakLine);

			mComponent.addView(row);

			for (int j = 0; j < row.getChildCount(); j++) {
				VPaymentChannelButton channelButton = (VPaymentChannelButton) row.getChildAt(j);
				mChannelButtonList.add(channelButton);
			}
		}

		int count = 0;
		for (DDynamicSelectionViewItem pmc : selectionView.items) {
			VPaymentChannelButton channelButton = mChannelButtonList.get(count);
			channelButton.setParentPaymentChannelList(this);
			channelButton.setImage(pmc.grayImg, pmc.selectedImg);
			channelButton.setPmcID(pmc.pmcID);
			channelButton.setPmcName(pmc.pmcName);
			channelButton.setTag(pmc.tag);

			count++;
		}

		if (selectionView.items.size() == 1) {
			this.setVisibility(View.GONE);
		}
		// Hide superfluous channel buttons
		if (mChannelButtonList.size() > selectionView.items.size()) {
			int visibility = (pIsSetGone) ? View.GONE : View.INVISIBLE;
			for (int i = 0; i < (mChannelButtonList.size() - selectionView.items.size()); i++) {
				mChannelButtonList.get(mChannelButtonList.size() - 1 - i).setVisibility(visibility);
			}
		}
	}

	private ViewGroup inflateButtonRow(int pNumOfButton) {
		ViewGroup row = (ViewGroup) mInflater.inflate(Resource.getLayout(Resource.layout.zpsdk_custom_group_selection), mComponent, false);
		if (pNumOfButton == 0) {
			row.removeAllViews();
		} else {
			for (int i = 0; i < (pNumOfButton - 1); i++) {
				row.addView(inflateButton());
			}
		}

		// Add padding
		if (pNumOfButton <= 2) {
			int px = (int) DimensionUtil.pxFromDp(this.getContext(), 35);
			row.setPadding(px, 0, px, 0);
		}

		return row;
	}

	private VPaymentChannelButton inflateButton() {
		ViewGroup row = (ViewGroup) mInflater.inflate(Resource.getLayout(Resource.layout.zpsdk_custom_group_selection), mComponent, false);
		VPaymentChannelButton button = (VPaymentChannelButton) row.getChildAt(0);
		row.removeAllViews();
		return button;
	}
	
	public void unSelectButton() {
		if (mSlectedPmcButton != null) {
			mSlectedPmcButton.toggle();
			mSlectedPmcButton = null;
		}
	}
	
	public void selectButton(String pPmcId) {
		for (VPaymentChannelButton button : mChannelButtonList) {
			if (!TextUtils.isEmpty(button.getPmcID()) && button.getPmcID().equalsIgnoreCase(pPmcId)) {
				button.performClick();
			}
		}
	}

	public void onSelect(VPaymentChannelButton pPaymentChannelButton) {
		if (mSlectedPmcButton != null) {
			this.mSlectedPmcButton.toggle();
		}

		this.mSlectedPmcButton = pPaymentChannelButton;

		if (mOnSelectListener != null) {
			mOnSelectListener.onClick(pPaymentChannelButton);
		}
	}

	public void setOnClickListener(OnClickListener pOnSelectListener) {
		boolean isFirst = mOnSelectListener == null;
		mOnSelectListener = pOnSelectListener;

		if (mIsAutoSelect && isFirst && mChannelButtonList != null && mChannelButtonList.size() > 0) {
			mChannelButtonList.get(0).performClick();
		}
	}
	
	public void dispose() {
		mInflater = null;
		for (VPaymentChannelButton button : mChannelButtonList) {
			button.dispose();
		}
		mComponent.removeAllViews();
	}
}
