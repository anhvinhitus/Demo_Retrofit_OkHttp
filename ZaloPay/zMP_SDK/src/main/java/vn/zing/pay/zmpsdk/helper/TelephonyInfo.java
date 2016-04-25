/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.utils.TelephonyInfo.java
 * Created date: Dec 19, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.helper;

import java.lang.reflect.Method;

import vn.zing.pay.zmpsdk.data.base.SingletonBase;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author YenNLH
 * 
 */
public class TelephonyInfo extends SingletonBase {

	private static TelephonyInfo telephonyInfo;
	private boolean isSIM1Ready;
	private boolean isSIM2Ready;

	public boolean isSIM1Ready() {
		return isSIM1Ready;
	}

	public boolean isSIM2Ready() {
		return isSIM2Ready;
	}

	public boolean isDualSIM() {
		return isSIM1Ready && isSIM2Ready; // imsiSIM2 != null;
	}

	public boolean isDualSIMOperatorEqual(Context context) {
		if (isDualSIM()) {
			TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

			TelephonyManager tm2 = null;
			// ASUS
			try {
				tm2 = get2ndTM(context, "get2ndTm");

			} catch (GeminiMethodNotFoundException e) {
				// SAMSUNG
				try {
					tm2 = get2ndTM(context, "getSecondary");
				} catch (Exception e1) {

				}
			}
			if (tm2 != null) {

				return telephonyManager.getSimOperator().equals(tm2.getSimOperator());
			}

		}
		return false;
	}

	private TelephonyInfo() {
		super();
	}

	public static TelephonyInfo getInstance(Context context) {

		if (telephonyInfo == null) {

			telephonyInfo = new TelephonyInfo();

			TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

			telephonyInfo.isSIM1Ready = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
			telephonyInfo.isSIM2Ready = false;

			try {
				// this is for samsung devices
				telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, "getSimStateGemini", 0);
				telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, "getSimStateGemini", 1);
			} catch (GeminiMethodNotFoundException e) {
				try {
					telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, "getSimState", 0);
					telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, "getSimState", 1);
				} catch (GeminiMethodNotFoundException e1) {
					// asus devices
					try {
						TelephonyManager tm2 = get2ndTM(context, "get2ndTm");
						telephonyInfo.isSIM2Ready = tm2.getSimState() == TelephonyManager.SIM_STATE_READY;
					} catch (Exception e2) {
						// Call here for next manufacturer's predicted method
						// name if you wish
						// e2.printStackTrace();
					}
				}
			}
		}

		return telephonyInfo;
	}

	public static TelephonyManager get2ndTM(Context context, String predictedMethodName)
			throws GeminiMethodNotFoundException {
		try {
			Method method = TelephonyManager.class.getMethod(predictedMethodName, new Class<?>[0]);
			return (TelephonyManager) method.invoke(null, new Object[0]);
		} catch (Exception ex) {
			// ex.printStackTrace();
			throw new GeminiMethodNotFoundException(predictedMethodName);
		}
	}

	private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID)
			throws GeminiMethodNotFoundException {

		boolean isReady = false;

		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		try {

			Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

			Class<?>[] parameter = new Class[1];
			parameter[0] = int.class;
			Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

			Object[] obParameter = new Object[1];
			obParameter[0] = slotID;
			Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

			if (ob_phone != null) {
				int simState = Integer.parseInt(ob_phone.toString());
				if (simState == TelephonyManager.SIM_STATE_READY) {
					isReady = true;
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			throw new GeminiMethodNotFoundException(predictedMethodName);
		}

		return isReady;
	}

	private static class GeminiMethodNotFoundException extends Exception {

		private static final long serialVersionUID = -996812356902545308L;

		public GeminiMethodNotFoundException(String info) {
			super(info);
		}
	}

	public static void printTelephonyManagerMethodNamesForThisDevice(Context context) {

		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Class<?> telephonyClass;
		try {
			telephonyClass = Class.forName(telephony.getClass().getName());
			Method[] methods = telephonyClass.getMethods();
			for (int idx = 0; idx < methods.length; idx++) {
				Log.d("TelephonyInfo", "\n" + methods[idx] + " declared by " + methods[idx].getDeclaringClass());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
