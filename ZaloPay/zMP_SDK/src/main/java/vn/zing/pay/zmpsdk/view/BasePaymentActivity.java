/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.view.BaseActivity.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.view;

import java.io.FileInputStream;
import java.util.Stack;

import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.analysis.JavaInstanceTracker;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.utils.DimensionUtil;
import vn.zing.pay.zmpsdk.utils.StringUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author YenNLH
 * 
 */
public abstract class BasePaymentActivity extends FragmentActivity {
	private static Stack<BasePaymentActivity> mZingPayActivitiesStack = new Stack<BasePaymentActivity>();

	public static Activity getCurrentActivity() {
		synchronized (mZingPayActivitiesStack) {
			if (mZingPayActivitiesStack == null) {
				return GlobalData.getOwnerActivity();
			}

			if (mZingPayActivitiesStack.size() == 0) {
				return GlobalData.getOwnerActivity();
			}

			return mZingPayActivitiesStack.peek();
		}
	}

	protected void Toast(String mess, int length) {
		Toast.makeText(this, mess, length).show();
	}

	@SuppressLint("InlinedApi")
	public static int getScreenOrientation(WindowManager wmgr) {
		int rotation = wmgr.getDefaultDisplay().getRotation();
		DisplayMetrics dm = new DisplayMetrics();
		wmgr.getDefaultDisplay().getMetrics(dm);

		int width = dm.widthPixels;
		int height = dm.heightPixels;

		int orientation;
		// if the device's natural orientation is portrait:
		if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width
				|| (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_180:
				orientation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_270:
				orientation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			default:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			}
		}
		// if the device's natural orientation is landscape
		// or if the device is square:
		else {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_180:
				orientation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
				orientation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			default:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			}
		}

		return orientation;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = 12;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null)
			return null;

		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	/*************************************************/
	/*************************************************/
	/*************************************************/

	@SuppressWarnings("deprecation")
	public void setImageViewContent(int id, Drawable drawable) {
		try {
			ImageView view = (ImageView) this.findViewById(id);
			if (view != null)
				view.setBackgroundDrawable(drawable);
		} catch (Exception e) {
		}
	}

	public void setTextViewContent(String id, String text) {
		try {
			TextView view = (TextView) this.findViewById(id);
			if (view != null)
				view.setText(text);
		} catch (Exception e) {
		}
	}

	public void setViewVisible(String id, int visible) {
		try {
			View view = this.findViewById(id);
			if (view != null)
				view.setVisibility(visible);
		} catch (Exception e) {
		}
	}

	public int getViewID(String pStrID) {
		return this.getResources().getIdentifier(pStrID, "id", this.getPackageName());
	}

	public View setView(String pStrID, boolean pIsVisible) {
		final int ID = getViewID(pStrID);
		View view = this.findViewById(ID);

		if (view == null)
			return view;

		if (pIsVisible) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}

		return view;
	}

	public void setText(String pStrID, String pText) {
		final int ID = getViewID(pStrID);
		View textView = this.findViewById(ID);

		if (textView == null)
			return;

		if (textView instanceof ToggleButton)
			((ToggleButton) this.findViewById(ID)).setText(pText);
		else if (textView instanceof EditText)
			((EditText) this.findViewById(ID)).setHint(pText);
		else if (textView instanceof TextView)
			((TextView) this.findViewById(ID)).setText(pText);
	}

	public void setImage(String pStrID, Bitmap pBitmap) {
		final int ID = getViewID(pStrID);

		ImageView imageView = ((ImageView) this.findViewById(ID));

		if (imageView != null) {
			if (pBitmap == null) {
				imageView.setVisibility(View.GONE);
			} else {
				imageView.setImageBitmap(pBitmap);
			}
		}
	}

	public View findViewById(String pName) {
		return findViewById(Resource.getID(pName));
	}

	/*************************************************/
	/*************************************************/
	/*************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		synchronized (mZingPayActivitiesStack) {
			if (mZingPayActivitiesStack == null)
				mZingPayActivitiesStack = new Stack<>();

			mZingPayActivitiesStack.push(this);
			JavaInstanceTracker.observe(this);
		}

		/****************************************
		 * COMMON FEATURES FOR PAYMENT ACTIVITY *
		 ****************************************/
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// lock orientation
		int orientation = getScreenOrientation(getWindowManager());
		setRequestedOrientation(orientation);

		// Set full screen
		if (ZingMobilePayApplication.isConfigFullScreen())
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// loadApplicationInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		synchronized (mZingPayActivitiesStack) {
			mZingPayActivitiesStack.remove(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/*************************************************/
	/*************************************************/
	/************* COMMON UI PROCESSING **************/
	/*************************************************/
	/*************************************************/

	protected Drawable appIcon;
	protected String appName;

	/**
	 * Load application name and icon to attribute of this class from shared
	 * preferences. If result values are null. Try to get them from package of
	 * owner application apk.
	 */
	@SuppressWarnings("deprecation")
	protected void loadApplicationInfo() {
		appName = SharedPreferencesManager.getInstance().getAppName();

		FileInputStream inputStream;
		try {
			inputStream = openFileInput(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_app_logo));
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			bitmap.setDensity(Bitmap.DENSITY_NONE);
			appIcon = new BitmapDrawable(bitmap);
		} catch (Exception e) {
			Log.e(getClass().getName(), e.getMessage(), e);
		}

		// Try to get application info locally
		PackageManager packageManager = getApplicationContext().getPackageManager();
		ApplicationInfo applicationInfo;
		try {
			applicationInfo = packageManager.getApplicationInfo(this.getPackageName(), 0);
			if (TextUtils.isEmpty(appName))
				appName = (String) applicationInfo.loadLabel(getPackageManager());

			if (appIcon == null)
				appIcon = applicationInfo.loadIcon(getPackageManager());

		} catch (final NameNotFoundException e) {
			applicationInfo = null;
		}
	}

	protected void showApplicationInfo() {
		// ////////////
		// APP LOGO //
		// ////////////
		if (appIcon != null) {
			Bitmap roundAppIcon = getRoundedCornerBitmap(drawableToBitmap(appIcon));

			ImageView appAvatarView = (ImageView) findViewById(Resource.id.app_avatar);
			if (appAvatarView != null && roundAppIcon != null) {
				appAvatarView.setImageBitmap(roundAppIcon);
			}
		}

		// ///////////
		// APP NAME //
		// ///////////
		if (appName.length() > 0) {
			int screenLayoutSize = getResources().getConfiguration().screenLayout
					& Configuration.SCREENLAYOUT_SIZE_MASK;

			// In case of small layout screen
			if (screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_SMALL && DimensionUtil.isScreenPortrait(this))
				setTextViewContent(Resource.id.zalosdk_bill_info_ctl,
						GlobalData.getStringResource(Resource.string.zingpaysdk_activity_small_title));
			else
				setTextViewContent(Resource.id.zalosdk_bill_info_ctl, appName);
		}
	}

	protected void showAmount() {
		// /////////
		// AMOUNT //
		// /////////
		if (GlobalData.getPaymentInfo().amount > 0)
			setTextViewContent(Resource.id.payment_method_amount,
					StringUtil.longToStringNoDecimal(GlobalData.getPaymentInfo().amount) + " VNĐ");
		else
			((TextView) findViewById(Resource.id.payment_method_amount)).setVisibility(View.GONE);
	}

	protected void showDisplayInfo() {
		// Show display info
		if (!TextUtils.isEmpty(GlobalData.getPaymentInfo().displayInfo)) {
			if (GlobalData.getPaymentInfo().displayInfo.length() > 60)
				setTextViewContent(Resource.id.zpsdk_method_description,
						GlobalData.getPaymentInfo().displayInfo.substring(0, 57) + "...");
			else
				setTextViewContent(Resource.id.zpsdk_method_description, GlobalData.getPaymentInfo().displayInfo);
		}

		// Set display name
		if (!TextUtils.isEmpty(GlobalData.getPaymentInfo().displayName))
			setTextViewContent(Resource.id.item_name, GlobalData.getPaymentInfo().displayName);
	}

	protected void resizeHeader() {
		if (!DimensionUtil.isScreenPortrait(this)) { // landscape
			Log.i("Zmp", "Test resizeHeader");
			setTextViewContent(Resource.id.payment_method_name,
					GlobalData.getStringResource(Resource.string.zingpaysdk_activity_small_title));

			TextView methodName = (TextView) findViewById(Resource.id.payment_method_name);
			if (methodName != null) {
				methodName.setTypeface(null, Typeface.BOLD);
			}

			View header = this.findViewById(Resource.id.zpsdk_header);
			if (header != null && header.getVisibility() == View.VISIBLE) {
				int width = (int) (DimensionUtil.getScreenWidth(this) * 0.28);
				if (width > DimensionUtil.pxFromDp(this, 155)) {
					LayoutParams layoutParams = (LayoutParams) header.getLayoutParams();
					layoutParams.width = width;
					header.setLayoutParams(layoutParams);
				}

			}
		}
	}

	protected void performClick(int id) {
		setVisible(false);
		findViewById(id).performClick();
	}
}
