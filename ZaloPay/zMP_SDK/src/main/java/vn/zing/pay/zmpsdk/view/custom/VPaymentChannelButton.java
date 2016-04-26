/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton.java
 * Created date: Dec 29, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view.custom;

import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.utils.DimensionUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * @author YenNLH
 * 
 */
public class VPaymentChannelButton extends RelativeLayout {
	private boolean mIsSelected = false;
	private String mPmcID = null;
	private String mPmcName = null;
	private ImageView mSelectedImage = null;
	private ImageView mGrayImage = null;
	private Drawable mSelectedImageBackground = null;
	private Drawable mGrayImageBackground = null;

	private VPaymentChannelGroup mPaymentChannelList = null;

	private OnClickListener mExtendClickListener = null;

	public VPaymentChannelButton(Context context) {
		super(context, null);
		init(null, 0);
	}

	public VPaymentChannelButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public VPaymentChannelButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	/**
	 * Get all view components and store them in attributes
	 */
	private View initializeLayout() {
		// Get all components
		LayoutInflater inflater = LayoutInflater.from(this.getContext().getApplicationContext());
		ViewGroup view = (ViewGroup) inflater.inflate(Resource.getLayout(Resource.layout.zpsdk_custom_radio_btn), this, false);

		mSelectedImage = (ImageView) view.findViewWithTag("mSelectedImage");
		mGrayImage = (ImageView) view.findViewWithTag("mGrayImage");

		view.removeAllViews();

		addView(mGrayImage);
		addView(mSelectedImage);

		return view;
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Init layout
		this.initializeLayout();

		// Load attributes
//		final TypedArray styleAttributes = getContext().obtainStyledAttributes(attrs,
//				getResources().getIntArray(Resource.getStyleable(Resource.styleable.VPaymentChannelButton)), defStyle, 0);
//
//		mPmcID = styleAttributes.getString(Resource.getStyleable(Resource.styleable.VPaymentChannelButton_pmcID));
//		mPmcName = styleAttributes.getString(Resource.getStyleable(Resource.styleable.VPaymentChannelButton_pmcName));
//		mGrayImage.setImageDrawable(styleAttributes.getDrawable(Resource.getStyleable(Resource.styleable.VPaymentChannelButton_srcOff)));
//		mSelectedImage.setImageDrawable(styleAttributes.getDrawable(Resource.getStyleable(Resource.styleable.VPaymentChannelButton_srcOn)));
//		styleAttributes.recycle();
//
		mGrayImageBackground = mGrayImage.getBackground();
		mSelectedImageBackground = mSelectedImage.getBackground();

		this.setClickable(true);
		this.setEnabled(true);

		this.setOnClickListener(mOnClickListener);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mExtendClickListener != null) {
				mExtendClickListener.onClick(v);
			}
			if (mPaymentChannelList != null) {
				mPaymentChannelList.onSelect(VPaymentChannelButton.this);
			}
			toggle();
		}
	};

	public void setExtendOnClickListener(OnClickListener pListener) {
		this.mExtendClickListener = pListener;
	}

	public void toggle() {
		if (mSelectedImage != null) {
			if (mIsSelected) {
				mSelectedImage.setVisibility(View.GONE);
			} else {
				mSelectedImage.setVisibility(View.VISIBLE);
			}
		} else {
			if (mIsSelected) {
				int px = (int) (DimensionUtil.pxFromDp(getContext(), 1) + 0.5f);
				mGrayImage.setPadding(px, px * 8, px, px * 8);
				setBackground(mGrayImage, mGrayImageBackground);
			} else {
				int px = (int) (DimensionUtil.pxFromDp(getContext(), 2) + 0.5f);
				mGrayImage.setPadding(px, px * 4, px, px * 4);
				setBackground(mGrayImage, mSelectedImageBackground);
			}
		}
		mIsSelected = !mIsSelected;
	}

	public void setParentPaymentChannelList(VPaymentChannelGroup pPaymentChannelList) {
		this.mPaymentChannelList = pPaymentChannelList;
	}

	public void setImage(String pGrayImg, String pSelectedImg) {
		if (pGrayImg == null) {
			// setImage(mSelectedImage, pGrayImg);
			removeView(mGrayImage);
			mGrayImage = null;
		} else {
			setImage(mGrayImage, pGrayImg);
		}

		if (pSelectedImg == null) {
			// setImage(mSelectedImage, pGrayImg);
			removeView(mSelectedImage);
			mSelectedImage = null;
		} else {
			setImage(mSelectedImage, pSelectedImg);
		}

		if (pSelectedImg == null) {
			mGrayImage.setBackgroundColor(Color.WHITE);
			mGrayImageBackground = mGrayImage.getBackground();
		}
	}

	public void dispose() {
		mSelectedImage = null;
		mGrayImage = null;
		this.removeAllViews();
	}

	/**
	 * @return the mPmcID
	 */
	public String getPmcID() {
		return mPmcID;
	}

	/**
	 * @param mPmcID
	 *            the mPmcID to set
	 */
	public void setPmcID(String mPmcID) {
		this.mPmcID = mPmcID;
	}

	/**
	 * @return the mPmcName
	 */
	public String getPmcName() {
		return mPmcName;
	}

	/**
	 * @param mPmcName
	 *            the mPmcName to set
	 */
	public void setPmcName(String mPmcName) {
		this.mPmcName = mPmcName;
	}

	@SuppressWarnings("deprecation")
	private void setImage(ImageView pImageView, String pImageName) {
		try {
			Bitmap bitmap = ResourceManager.getImage(pImageName);
			if (bitmap == null) {
				String imgName = pImageName.substring(0, pImageName.lastIndexOf('.'));
				Resources resources = this.getContext().getResources();

				final int resourceId = resources.getIdentifier(imgName, "drawable", this.getContext().getPackageName());
				Drawable drawable = resources.getDrawable(resourceId);

				pImageView.setImageDrawable(drawable);
			} else {
				pImageView.setImageBitmap(bitmap);
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	private void setBackground(ImageView pImageView, Drawable pDrawable) {
		final int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			pImageView.setBackgroundDrawable(pDrawable);
		} else {
			pImageView.setBackground(pDrawable);
		}
	}
}
