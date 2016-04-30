/**
99 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.custom.VPaymentEditText.java
 * Created date: Jan 5, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.view.custom;

import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicEditText;
import vn.zing.pay.zmpsdk.listener.ZPOnSelectionChangeListener;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * @author YenNLH
 * 
 */
public class VPaymentEditText extends EditText {
	// Change this to what you want... ' ', '-' etc..
	private static final char SPACE_SEPERATOR = '-';

	private DDynamicEditText mEditTextConfig = null;
	private AdapterBase mAdapter = null;
	private boolean mIsTextGroup = true;
	private boolean mIsPattern = false;
	private String mPattern = null;

	private ZPOnSelectionChangeListener mSelectionChangeListener = null;

	public VPaymentEditText(Context context) {
		super(context, null);
		init(null, 0);
	}

	public VPaymentEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public VPaymentEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		if (this.getContext() instanceof PaymentChannelActivity) {
			mAdapter = ((PaymentChannelActivity) getContext()).getAdapter();
		}
		if (mIsTextGroup) {
			this.addTextChangedListener(mTextFormater);
		}
		this.setClickable(true);
		this.setEnabled(true);
	}

	public void init(DDynamicEditText pEditText, AdapterBase pAdapter) {
		mIsPattern = pEditText.pattern;
		mEditTextConfig = pEditText;
		mAdapter = pAdapter;

		if (mIsPattern) {
			this.setOnFocusChangeListener(mOnFocusChangeListener);
		}

		if (mIsTextGroup) {
			this.addTextChangedListener(mTextFormater);
		}
	}

	public boolean isValid() {
		if (mIsPattern && mAdapter != null) {
			mPattern = ResourceManager.getInstance(null).getPattern(mEditTextConfig.id, mAdapter.getChannelID());
			if (mPattern == null) {
				mPattern = ResourceManager.getInstance(null).getPattern(mEditTextConfig.id, "all");
			}

			if (this.getText().length() == 0) {
				return true;
			}

			if (mPattern != null) {
				String text = getString();

				if (text.matches(mPattern)) {
					Log.i(VPaymentEditText.this, "**** " + mEditTextConfig.id + " MATCH ****");
					return true;
				} else {
					Log.i(VPaymentEditText.this, "**** " + mEditTextConfig.id + " NOT MATCH ****");
				}
			}
			return false;
		}
		return true;
	}

	public boolean checkPattern() {
		if (!isValid()) {
			DialogManager.showAlertDialog(mEditTextConfig.errMess);
			return false;
		}
		return true;
	}
	
	public void setGroupText(boolean pIsEnabled) {
		mIsTextGroup = pIsEnabled;
	}

	public String getString() {
		if (mIsTextGroup) {
			return getText().toString().replace(String.valueOf(SPACE_SEPERATOR), "");
		} else {
			return getText().toString();
		}
	}

	public void setOnSelectionChangeListener(ZPOnSelectionChangeListener pListener) {
		mSelectionChangeListener = pListener;
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		
		if (mSelectionChangeListener != null) {
			mSelectionChangeListener.onSelectionChanged(selStart, selEnd);
		}
	}

	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				checkPattern();
			}
		}
	};

	private TextWatcher mTextFormater = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if (mIsTextGroup) {
				// Remove spacing char
				if (s.length() > 0 && (s.length() % 5) == 0) {
					final char c = s.charAt(s.length() - 1);
					if (SPACE_SEPERATOR == c) {
						s.delete(s.length() - 1, s.length());
					}
				}

				// Insert char where needed.
				if (s.length() > 0 && (s.length() % 5) == 0) {
					char c = s.charAt(s.length() - 1);
					// Only if its a digit where there should be a space we
					// insert a space
					if (Character.isDigit(c)
							&& TextUtils.split(s.toString(), String.valueOf(SPACE_SEPERATOR)).length <= 3) {

						InputFilter[] filters = s.getFilters(); // save filters
						s.setFilters(new InputFilter[] {}); // clear filters
						s.insert(s.length() - 1, String.valueOf(SPACE_SEPERATOR));
						s.setFilters(filters); // restore filters
					}
				}
			}
		}
	};

	// http://stackoverflow.com/questions/14069501/edittext-causing-memory-leak/27231817#27231817
	//
	// @Override
	// public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	// return null;
	// }
	//
	// @Override
	// protected boolean getDefaultEditable() {
	// return true;
	// }
	//
	// @Override
	// protected MovementMethod getDefaultMovementMethod() {
	// return ArrowKeyMovementMethod.getInstance();
	// }
	//
	// @Override
	// public Editable getText() {
	// return (Editable) super.getText();
	// }
	//
	// @Override
	// public void setText(CharSequence text, BufferType type) {
	// super.setText(text, BufferType.EDITABLE);
	// }
	//
	// /**
	// * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
	// */
	// public void setSelection(int start, int stop) {
	// Selection.setSelection(getText(), start, stop);
	// }
	//
	// /**
	// * Convenience for {@link Selection#setSelection(Spannable, int)}.
	// */
	// public void setSelection(int index) {
	// Selection.setSelection(getText(), index);
	// }
	//
	// /**
	// * Convenience for {@link Selection#selectAll}.
	// */
	// public void selectAll() {
	// Selection.selectAll(getText());
	// }
	//
	// /**
	// * Convenience for {@link Selection#extendSelection}.
	// */
	// public void extendSelection(int index) {
	// Selection.extendSelection(getText(), index);
	// }
	//
	// @Override
	// public void setEllipsize(TextUtils.TruncateAt ellipsis) {
	// if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
	// throw new
	// IllegalArgumentException("EditText cannot use the ellipsize mode "
	// + "TextUtils.TruncateAt.MARQUEE");
	// }
	// super.setEllipsize(ellipsis);
	// }
}
