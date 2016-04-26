/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.utils.Log.java
 * Created date: Dec 28, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.utils;

import vn.zing.pay.zmpsdk.data.Constants;

/**
 * @author YenNLH
 * 
 */
public class Log {
	public static final boolean IS_LOG_ENABLE = (Constants.IS_RELEASE) ? false : true;
	private static final String TAG = "ZING_PAY_SDK_LOG";

	public static void e(Object pObject, Exception pException) {
		if (IS_LOG_ENABLE)
			android.util.Log.e((pObject != null) ? pObject.getClass().getName() : TAG, pException.getMessage(),
					pException);
	}

	public static void w(Object pObject, Exception pException) {
		if (IS_LOG_ENABLE)
			android.util.Log.w((pObject != null) ? pObject.getClass().getName() : TAG, pException.getMessage(),
					pException);
	}

	public static void d(Object pObject, Exception pException) {
		if (IS_LOG_ENABLE)
			android.util.Log.d((pObject != null) ? pObject.getClass().getName() : TAG, pException.getMessage(),
					pException);
	}

	public static void i(Object pObject, Exception pException) {
		if (IS_LOG_ENABLE)
			android.util.Log.i((pObject != null) ? pObject.getClass().getName() : TAG, pException.getMessage(),
					pException);
	}

	// //////////////////////////////////////////////////////////////////

	public static void e(Object pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.e((pObject != null) ? pObject.getClass().getName() : TAG, pMessage);
	}

	public static void w(Object pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.w((pObject != null) ? pObject.getClass().getName() : TAG, pMessage);
	}

	public static void d(Object pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.d((pObject != null) ? pObject.getClass().getName() : TAG, pMessage);
	}

	public static void i(Object pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.i((pObject != null) ? pObject.getClass().getName() : TAG, pMessage);
	}

	// //////////////////////////////////////////////////////////////////

	public static void e(String pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.e((pObject != null) ? pObject : TAG, pMessage);
	}

	public static void w(String pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.w((pObject != null) ? pObject : TAG, pMessage);
	}

	public static void d(String pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.d((pObject != null) ? pObject : TAG, pMessage);
	}

	public static void i(String pObject, String pMessage) {
		if (IS_LOG_ENABLE)
			android.util.Log.i((pObject != null) ? pObject : TAG, pMessage);
	}
}
