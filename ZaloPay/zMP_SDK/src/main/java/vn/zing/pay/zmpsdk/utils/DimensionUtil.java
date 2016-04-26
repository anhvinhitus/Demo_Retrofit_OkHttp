/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.helper.DimensionHelper.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.utils;

import vn.zing.pay.zmpsdk.data.GlobalData;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;

/**
 * This class is used to manage and work with dimention of device's screen
 * 
 * @author YenNLH
 * 
 */
public class DimensionUtil {
	private static String density = null;

	/**
	 * Convert from a DIP (density independent pixel) value, or a PX (pixel)
	 * value
	 * 
	 * @param dip
	 *            DIP value
	 * 
	 * @param r
	 *            Resource of application
	 * 
	 * @return Pixel value converted from input
	 */
	public static float dipsToPXs(int dip, Resources r) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
	}

	public static float getScaleFactor(Activity owner) {

		Display display = owner.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		switch (metrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			return 1f / 2;
		case DisplayMetrics.DENSITY_MEDIUM:
			return 2f / 3;
		case DisplayMetrics.DENSITY_HIGH:
		case DisplayMetrics.DENSITY_400:
		case DisplayMetrics.DENSITY_XHIGH:
		case DisplayMetrics.DENSITY_XXHIGH:
		case DisplayMetrics.DENSITY_XXXHIGH:
		default:
			return 1f;
		}
	}

	/**
	 * Get density independent pixels of current device by type name.
	 * 
	 * @param owner
	 *            The {@link Activity} object.
	 * 
	 * @return This method will:
	 *         <ul>
	 *         <li>return {@code "low"} string, if {@code DENSITY_LOW} (120 DPI)
	 *         <li>return {@code "medium"} string, if {@code DENSITY_MEDIUM}
	 *         (160 DPI)
	 *         <li>return {@code "high"} string, if {@code DENSITY_HIGH} (240
	 *         DPI)
	 *         <li>return {@code "xhigh"} string, otherwise
	 *         </ul>
	 */
	public static String getDensity(Activity owner) {
		if (density != null) {
			return density;
		}

		if (owner == null) {
			owner = GlobalData.getOwnerActivity();
		}

		if (owner == null) {
			return "xhigh";
		}

		Display display = owner.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		switch (metrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			density = "low";
		case DisplayMetrics.DENSITY_MEDIUM:
			density = "medium";
		case DisplayMetrics.DENSITY_HIGH:
			density = "high";
		case DisplayMetrics.DENSITY_400:
		case DisplayMetrics.DENSITY_XHIGH:
		case DisplayMetrics.DENSITY_XXHIGH:
		case DisplayMetrics.DENSITY_XXXHIGH:
		default:
			density = "xhigh";
		}

		return density;
	}

	public static float getDensityNumber(Context owner) {
		return owner.getResources().getDisplayMetrics().density;
	}

	public static float getPixelPadding(Context owner) {
		Resources r = owner.getResources();
		float result = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
		if (result < 1)
			return 1;

		return result;

	}

	public static float dpFromPx(Context context, float px) {
		return px / context.getResources().getDisplayMetrics().density;
	}

	public static float pxFromDp(Context context, float dp) {
		return dp * context.getResources().getDisplayMetrics().density;
	}
	
	public static boolean isScreenPortrait(Activity activity) {
	    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	    DisplayMetrics dm = new DisplayMetrics();
	    activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
	    int width = dm.widthPixels;
	    int height = dm.heightPixels;
	    boolean isPortrait;
	    // if the device's natural orientation is portrait:
	    if ((rotation == Surface.ROTATION_0
	            || rotation == Surface.ROTATION_180) && height > width ||
	        (rotation == Surface.ROTATION_90
	            || rotation == Surface.ROTATION_270) && width > height) {
	        switch(rotation) {
	            case Surface.ROTATION_0:
	                isPortrait = true; //ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_90:
	                isPortrait = false; //ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_180:
	                isPortrait = true; //ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	                break;
	            case Surface.ROTATION_270:
	                isPortrait = false; //ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	                break;
	            default:
	                isPortrait = true; //ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;              
	        }
	    }
	    // if the device's natural orientation is landscape or if the device
	    // is square:
	    else {
	        switch(rotation) {
	            case Surface.ROTATION_0:
	                isPortrait = false; //ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_90:
	                isPortrait = true; //ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_180:
	                isPortrait = false; //ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	                break;
	            case Surface.ROTATION_270:
	                isPortrait = true; //ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	                break;
	            default:
	                isPortrait = false; //ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;              
	        }
	    }

	    return isPortrait;
	}

	public static int getScreenWidth(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.widthPixels; //Math.min(metrics.widthPixels, metrics.heightPixels);
	}
	
	public static int getScreenHeight(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.heightPixels; //Math.min(metrics.widthPixels, metrics.heightPixels);
	}
}
