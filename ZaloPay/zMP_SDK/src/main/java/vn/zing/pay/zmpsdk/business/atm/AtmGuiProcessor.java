/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.AtmGuiProcessor.java
 * Created date: Jan 14, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.atm;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.entity.atm.DAtmCardCache;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DPaymentChannel;
import vn.zing.pay.zmpsdk.listener.ZPOnSelectionChangeListener;
import vn.zing.pay.zmpsdk.utils.BitmapUtil;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.GuiUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.utils.StringUtil;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelButton;
import vn.zing.pay.zmpsdk.view.custom.VPaymentChannelGroup;
import vn.zing.pay.zmpsdk.view.custom.VPaymentEditText;

/**
 * @author YenNLH
 * 
 */
public class AtmGuiProcessor {

	private static final String DEFAULT_PAGE_NAME = "zpsdk_atm_card_info_normal";
	private static final String VND = " VNĐ";

	private PaymentChannelActivity mOwnerActivity;
	private AdapterATM mAdapterATM;
	private double mDiscountPercent = -1;

	private VPaymentChannelGroup mPmcChannelGroup;
	private View mPopUpAtm;
	private View mBankSelectionButton;

	private View mNormalFormContainer;
	private VPaymentEditText mCardNumber;
	private EditText mCardHolderName;

	private VPaymentEditText mChargeAmount;
	private EditText m4LastDigits;
	private VPaymentEditText mCardMonth;
	private VPaymentEditText mCardYear;
	private EditText mCardPass;

	private EditText mOtpCode;
	private EditText mCaptchaCode;
	private ImageView mCaptchaImage;
	private View mCaptchaFrame;
	private WebView mCaptchaWebview;

	private EditText mUsername;
	private EditText mPassword;
	private int mSelectedItemOrderNumber = 0;

	private String mCurrentBankName = null;
	private int mLengthOfLastBankDetected = 0;
	private int mMaxLengthOfPattern = 0;

	private DAtmCardCache mCardCache = null;
	private Spinner mItemListSpinner;

	public AtmGuiProcessor(PaymentChannelActivity pActivity, AdapterATM pAdapterATM) {
		mOwnerActivity = pActivity;
		mAdapterATM = pAdapterATM;

		init();

		renderPromotion();
	}

	private void init() {
		// Hide charge amount label since we let user change charge amount value
		mOwnerActivity.findViewById(Resource.id.payment_method_amount).setVisibility(View.GONE);

		mBankSelectionButton = mOwnerActivity.findViewById(Resource.id.atm_select_ok);
		mPopUpAtm = mOwnerActivity.findViewById(Resource.id.popup_atm);
		if (mBankSelectionButton != null) {
			mBankSelectionButton.setOnClickListener(mOnOkBankSelectionListener);
		}

		mChargeAmount = (VPaymentEditText) mOwnerActivity.findViewById(Resource.id.chargeAmount);
		m4LastDigits = (EditText) mOwnerActivity.findViewById(Resource.id.fill_4_last_digit);
		mNormalFormContainer = mOwnerActivity.findViewById(Resource.id.normal_form_container);
		mCardNumber = (VPaymentEditText) mOwnerActivity.findViewById(Resource.id.cardNumber);
		mCardHolderName = (EditText) mOwnerActivity.findViewById(Resource.id.cardHolderName);
		mPmcChannelGroup = (VPaymentChannelGroup) mOwnerActivity.findViewById(Resource.id.payment_channel_adapter);
		mOwnerActivity.findViewById(Resource.id.date_container);

		mCardMonth = (VPaymentEditText) mOwnerActivity.findViewById(Resource.id.zpsdk_month_ctl);
		mCardYear = (VPaymentEditText) mOwnerActivity.findViewById(Resource.id.zpsdk_year_ctl);
		mCardPass = (EditText) mOwnerActivity.findViewById(Resource.id.zpsdk_card_password_ctl);
		mCardPass.setTypeface(Typeface.DEFAULT);
		mCardPass.setTransformationMethod(new PasswordTransformationMethod());

		mOtpCode = (EditText) mOwnerActivity.findViewById(Resource.id.zpsdk_otp_ctl);
		mCaptchaCode = (EditText) mOwnerActivity.findViewById(Resource.id.zpsdk_captchar_ctl);
		mCaptchaImage = (ImageView) mOwnerActivity.findViewById(Resource.id.zpsdk_captchar_img_ctl);
		mCaptchaFrame = mOwnerActivity.findViewById(Resource.id.zpsdk_captchar_wv_frame);
		mCaptchaWebview = (WebView) mOwnerActivity.findViewById(Resource.id.zpsdk_captchar_wv_ctl);

		mUsername = (EditText) mOwnerActivity.findViewById(Resource.id.zpsdk_acc_name_ctl);
		mPassword = (EditText) mOwnerActivity.findViewById(Resource.id.zpsdk_acc_password_ctl);
		mPassword.setTypeface(Typeface.DEFAULT);
		mPassword.setTransformationMethod(new PasswordTransformationMethod());

		mItemListSpinner = (Spinner) mOwnerActivity.findViewById(Resource.id.zpsdk_acc_list_ctl);

		mCardNumber.addTextChangedListener(mCardDetectionTextWatcher);
		mCardNumber.setOnFocusChangeListener(mOnFocusChangeListener);

		mChargeAmount.addTextChangedListener(mChargeAmountTextWatcher);
		m4LastDigits.addTextChangedListener(mEnabledTextWatcher);
		mCardNumber.addTextChangedListener(mEnabledTextWatcher);
		mCardHolderName.addTextChangedListener(mEnabledTextWatcher);
		mCardMonth.addTextChangedListener(mEnabledTextWatcher);
		mCardYear.addTextChangedListener(mEnabledTextWatcher);
		mCardPass.addTextChangedListener(mEnabledTextWatcher);
		mOtpCode.addTextChangedListener(mEnabledTextWatcher);
		mCaptchaCode.addTextChangedListener(mEnabledTextWatcher);
		mUsername.addTextChangedListener(mEnabledTextWatcher);
		mPassword.addTextChangedListener(mEnabledTextWatcher);

		m4LastDigits.setOnEditorActionListener(mEditorActionListener);
		mCardNumber.setOnEditorActionListener(mEditorActionListener);
		mCardHolderName.setOnEditorActionListener(mEditorActionListener);
		mCardMonth.setOnEditorActionListener(mEditorActionListener);
		mCardYear.setOnEditorActionListener(mEditorActionListener);
		mCardPass.setOnEditorActionListener(mEditorActionListener);
		mOtpCode.setOnEditorActionListener(mEditorActionListener);
		mCaptchaCode.setOnEditorActionListener(mEditorActionListener);
		mUsername.setOnEditorActionListener(mEditorActionListener);
		mPassword.setOnEditorActionListener(mEditorActionListener);

		mOwnerActivity.findViewById(Resource.id.edit_card_manual).setOnClickListener(mOnClickManualEdit);

		mMaxLengthOfPattern = ResourceManager.getInstance(null).getMaxLengthOfCardCodePattern();

		mChargeAmount.setGroupText(false);
		mChargeAmount.setText(String.valueOf(GlobalData.getPaymentInfo().amount));
		mChargeAmount.setOnSelectionChangeListener(mOnSelectionChangeListener);
		mChargeAmount.setSelection(0);
	}

	public void dispose() {
		mPmcChannelGroup.dispose();
		((ViewGroup) mOwnerActivity.findViewById(Resource.id.autofill_container)).removeAllViews();
		mAdapterATM = null;
		mOwnerActivity = null;
	}

	private void renderPromotion() {
		// Calculate discount on ATM
		String atmConfig = SharedPreferencesManager.getInstance().getATMChannelConfig();
		if (!TextUtils.isEmpty(atmConfig)) {
			DPaymentChannel channel = GsonUtils.fromJsonString(atmConfig, DPaymentChannel.class);
			mDiscountPercent = channel.discount;
		}

		View promotionGroup = mOwnerActivity.findViewById(Resource.id.zpsdk_promotion_description_group);
		if (promotionGroup != null) {
			if (mDiscountPercent > 0) {
				promotionGroup.setVisibility(View.VISIBLE);

				((TextView) mOwnerActivity.findViewById(Resource.id.zpsdk_promotion_description_percent)).setText(String
						.format("%s%%", mDiscountPercent));

				long discountedValue = (long) (getAmount() * (100d - mDiscountPercent)) / 100;
				((TextView) mOwnerActivity.findViewById(Resource.id.zpsdk_promotion_description_discount)).setText(String
						.format("%s VNĐ", StringUtil.longToStringNoDecimal(discountedValue)));
			} else {
				promotionGroup.setVisibility(View.GONE);
			}
		}
	}

	protected void resetPmc() {
		mOwnerActivity.setPaymentMethodName(GlobalData.getStringResource(Resource.string.zingpaysdk_pmc_name_atm));
		mAdapterATM.mBankCode = null;

		mLengthOfLastBankDetected = 0;
		mAdapterATM.setPageName(DEFAULT_PAGE_NAME);
		processBankType();
	}

	protected void checkCardCache() {
		mCardCache = SharedPreferencesManager.getInstance().getCardInfo();
		if (mCardCache != null) {
			// if (mAdapterATM.mBankCode == null) {
			// // This bank was diabled
			// return;
			// }

			mCardNumber.setText(mCardCache.cardNumber);
			mCardHolderName.setText(mCardCache.cardHolderName);
			mCardMonth.setText(mCardCache.cardMonth);
			mCardYear.setText(mCardCache.cardYear);

			mPmcChannelGroup.selectButton(mCardCache.bankCode);

			mNormalFormContainer.setVisibility(View.GONE);
			mOwnerActivity.findViewById(Resource.id.autofill_container).setVisibility(View.VISIBLE);
			mOwnerActivity.enableSubmitBtn(false);

			((TextView) mOwnerActivity.findViewById(Resource.id.card_serial_number)).setText(mCardCache.cardNumber
					.subSequence(0, mCardCache.cardNumber.length() - 4) + "****");
			((TextView) mOwnerActivity.findViewById(Resource.id.card_name)).setText(mCardCache.cardHolderName);
		}
	}

	protected boolean validateCardCache() {
		if (mCardCache == null) {
			return true;
		} else {
			String lastDigit = mCardCache.cardNumber.substring(mCardCache.cardNumber.length() - 4,
					mCardCache.cardNumber.length());
			return m4LastDigits.getText().toString().equals(lastDigit);
		}
	}

	protected void onSelectBank(VPaymentChannelButton view) {
		mAdapterATM.mBankCode = view.getPmcID();
		mCurrentBankName = view.getPmcName();

		enableBankSelectionButton(true);
		mAdapterATM.setPageName((String) view.getTag());
		processBankType();
	}

	public long getAmount() {
		try {
			long amount = Long.parseLong(mChargeAmount.getText().toString().replace(",", "").replace(VND, ""));
			return amount;
		} catch (Exception ex) {
			// Log.e(this, ex);
		}

		return 0;
	}

	public String getCardNumber() {
		return mCardNumber.getString();
	}

	public String getCardHolder() {
		return mCardHolderName.getText().toString();
	}

	public String getCardPass() {
		return mCardPass.getText().toString();
	}

	public String getCardMonth() {
		String month = mCardMonth.getText().toString();
		if (month.length() < 2) {
			month = "0" + month;
		}
		return month;
	}

	public String getCardYear() {
		return mCardYear.getText().toString();
	}

	public String getBankName() {
		return mCurrentBankName;
	}

	public String getUsername() {
		return mUsername.getText().toString();
	}

	public int getSelectedItemOrderNumber() {
		return mSelectedItemOrderNumber;
	}

	public String getPassword() {
		return mPassword.getText().toString();
	}

	private boolean checkValidRequiredEditText(EditText pView) {
		if (pView.getVisibility() != View.VISIBLE) {
			return true;
		}

		boolean isCheckPattern = (pView instanceof VPaymentEditText) ? ((VPaymentEditText) pView).isValid() : true;
		return isCheckPattern && (pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText()));
	}

	private void processBankType() {

		if (mCardPass != null) {
			mCardPass.setHint("Mật mã thẻ " + mCurrentBankName);
		}
		mOwnerActivity.renderByResource();
		checkEnableSubmitButton();
	}

	public String getCaptcha() {
		return mCaptchaCode.getText().toString();
	}

	public String getOtp() {
		return mOtpCode.getText().toString();
	}

	public void setCaptchaImage(String pB64Encoded) {
		if (TextUtils.isEmpty(pB64Encoded))
			return;

		Bitmap bitmap = BitmapUtil.b64ToImage(pB64Encoded);

		if (bitmap != null) {
			mCaptchaImage.setImageBitmap(bitmap);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setCaptchaUrl(String pUrl) {
		if (TextUtils.isEmpty(pUrl))
			return;

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html><head></head><body style='margin:0;padding:0'><img src='").append(pUrl)
				.append("' style='margin:0;padding:0;' width='120px' alt='' /></body>");
		mCaptchaWebview.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		WebSettings webSettings = mCaptchaWebview.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setSupportZoom(false);
		webSettings.setBuiltInZoomControls(false);
		mCaptchaWebview.setBackgroundColor(Color.TRANSPARENT);
		webSettings.setLoadWithOverviewMode(true);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			mCaptchaWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		mCaptchaWebview.loadDataWithBaseURL(mAdapterATM.getWebViewProcessor().getCurrentUrl(), sb.toString(),
				"text/html", null, null);

	}

	public void setCaptchaImage(String pB64Encoded, String pUrl) {
		if (pB64Encoded.length() > 10) {
			mCaptchaImage.setVisibility(View.VISIBLE);
			mCaptchaFrame.setVisibility(View.GONE);
			setCaptchaImage(pB64Encoded);
		} else {
			mCaptchaImage.setVisibility(View.GONE);
			mCaptchaFrame.setVisibility(View.VISIBLE);
			setCaptchaUrl(pUrl);
		}
	}

	public boolean checkEnableSubmitButton() {
		boolean is4LastDigits = checkValidRequiredEditText(m4LastDigits);

		boolean isCardCode = checkValidRequiredEditText(mCardNumber);
		boolean isCardHolder = checkValidRequiredEditText(mCardHolderName);
		boolean isCardMonth = checkValidRequiredEditText(mCardMonth);
		boolean isCardYear = checkValidRequiredEditText(mCardYear);

		boolean isCardPass = checkValidRequiredEditText(mCardPass);

		boolean isUserName = checkValidRequiredEditText(mUsername);
		boolean isPassword = checkValidRequiredEditText(mPassword);
		boolean isOtp = checkValidRequiredEditText(mOtpCode);
		boolean isCaptcha = checkValidRequiredEditText(mCaptchaCode);

		//@formatter:off
		if (
				(mOwnerActivity.findViewById(Resource.id.autofill_container).getVisibility() != View.VISIBLE || is4LastDigits)
				&&
				(mNormalFormContainer.getVisibility() != View.VISIBLE || (isCardCode && isCardHolder && isCardMonth && isCardYear))
				&&
				(mItemListSpinner.getVisibility() != View.VISIBLE || (mSelectedItemOrderNumber != 0))
				&& 
				(isCardPass && isUserName && isPassword && isOtp && isCaptcha)
				&& 
				(mAdapterATM.getBankCode() != null)
			) {
		//@formatter:on
			mOwnerActivity.enableSubmitBtn(true);
			return true;
		} else {
			mOwnerActivity.enableSubmitBtn(false);
			return false;
		}
	}

	private void matchBankByPrefixCode(String pStr) {

		String bankCode = null;

		// Find the bank having card code starting with current string
		if (mPmcChannelGroup != null) {

			if (pStr.length() <= mMaxLengthOfPattern) {
				bankCode = ResourceManager.getInstance(null).getBankByCardCode(pStr.toString());
			} else {
				for (int i = 4; i <= Math.min(pStr.length(), mMaxLengthOfPattern); i++) {
					bankCode = ResourceManager.getInstance(null).getBankByCardCode(pStr.substring(0, i));

				}
			}

			// Busted
			if (bankCode != null) {
				mPmcChannelGroup.selectButton(bankCode);
				mLengthOfLastBankDetected = pStr.length();
			}
		}

		if (bankCode == null) {
			mAdapterATM.mBankCode = null;
		}
	}

	public void enableBankSelectionButton(boolean pIsEnabled) {
		mOwnerActivity.setEnableButton(mBankSelectionButton, pIsEnabled);
	}

	public void enablePopupATM(boolean pIsEnabled) {
		if (mPopUpAtm != null) {
			if (pIsEnabled) {
				((VPaymentChannelGroup) mOwnerActivity.findViewById(Resource.id.payment_channel_adapter)).unSelectButton();

				mOwnerActivity.setEnableButton(mBankSelectionButton, false);
				mPopUpAtm.setVisibility(View.VISIBLE);

				(new Handler()).postDelayed(new Runnable() {
					@Override
					public void run() {
						GuiUtils.hideSoftKeyboard(mOwnerActivity);
					}
				}, 100);
			} else {
				mPopUpAtm.setVisibility(View.GONE);
			}
		}
	}

	public void enableChargeAmount(boolean pIsEnabled) {
		mChargeAmount.setVisibility(pIsEnabled ? View.VISIBLE : View.GONE);
	}

	public void renderItemList(String[] pItemList) {
		// Selection of the spinner
		if (pItemList == null || pItemList.length == 0) {
			mItemListSpinner.setVisibility(View.GONE);
			return;
		}

		mItemListSpinner.setVisibility(View.VISIBLE);
		// Application of the Array to the Spinner
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mOwnerActivity,
				android.R.layout.simple_spinner_item, pItemList);
		// The drop down view
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mItemListSpinner.setAdapter(spinnerArrayAdapter);
		mItemListSpinner.setOnItemSelectedListener(mItemSelectedListener);
	}

	private ZPOnSelectionChangeListener mOnSelectionChangeListener = new ZPOnSelectionChangeListener() {
		@Override
		public void onSelectionChanged(int selStart, int selEnd) {
			if (selEnd > selStart) {
				mChargeAmount.setSelection(0);
				return;
			}

			int length = mChargeAmount.getText().length();

			if (length > 9) {
				int chosenPos = length - 9;
				if (selStart >= chosenPos) {
					mChargeAmount.setSelection(chosenPos);
				}
			} else {
				mChargeAmount.setSelection(0);
			}
		}
	};

	private TextWatcher mChargeAmountTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void afterTextChanged(Editable s) {
			try {
				String text = s.toString().replace(",", "").replace(VND, "");

				long num = Long.parseLong(text);
				if (num < 10000) {
					num *= 1000;
				}
				String formatted = StringUtil.longToStringNoDecimal(num) + VND;

				if (!s.toString().equals(formatted)) {
					s.clear();
					InputFilter[] filters = s.getFilters(); // save filters
					s.setFilters(new InputFilter[] {}); // clear filters
					s.append(formatted);
					s.setFilters(filters); // restore filters
				}

				renderPromotion();
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mChargeAmount.setNextFocusForwardId(m4LastDigits.getId());
				} else {
					mChargeAmount.setNextFocusDownId(m4LastDigits.getId());
					mChargeAmount.setNextFocusRightId(m4LastDigits.getId());
					mChargeAmount.setNextFocusLeftId(m4LastDigits.getId());
					mChargeAmount.setNextFocusUpId(m4LastDigits.getId());
				}
			} catch (Exception e) {
				Log.e(this, e);
			}
		}
	};

	private View.OnClickListener mOnOkBankSelectionListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			enablePopupATM(false);
		}
	};

	private View.OnClickListener mOnClickManualEdit = new View.OnClickListener() {

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onClick(View v) {
			mCardCache = null;

			mCardNumber.setText("");
			mCardHolderName.setText("");
			mCardMonth.setText("");
			mCardYear.setText("");

			mNormalFormContainer.setVisibility(View.VISIBLE);
			mOwnerActivity.findViewById(Resource.id.autofill_container).setVisibility(View.GONE);

			resetPmc();
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mChargeAmount.setNextFocusForwardId(mCardNumber.getId());
			} else {
				mChargeAmount.setNextFocusDownId(mCardNumber.getId());
				mChargeAmount.setNextFocusRightId(mCardNumber.getId());
				mChargeAmount.setNextFocusLeftId(mCardNumber.getId());
				mChargeAmount.setNextFocusUpId(mCardNumber.getId());
			}
		}
	};

	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				String cardNumber = getCardNumber();
				// Over the length and did not find anything
				if (mAdapterATM.mBankCode == null && cardNumber.length() >= mMaxLengthOfPattern) {
					enablePopupATM(true);
					mLengthOfLastBankDetected = cardNumber.length();
				}
			}
		}
	};

	private TextWatcher mCardDetectionTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String text = getCardNumber();

			// Had a character in the detected card code deleted
			if (text.length() < mLengthOfLastBankDetected) {
				resetPmc();
			}

			matchBankByPrefixCode(text.toString());
		}
	};

	private TextWatcher mEnabledTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			checkEnableSubmitButton();
		}
	};

	private OnEditorActionListener mEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE && checkEnableSubmitButton()) {
				mAdapterATM.onClickSubmission();
			}
			return false;
		}
	};

	private OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			// An item was selected. You can retrieve the selected item using
			mSelectedItemOrderNumber = pos;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};
}
