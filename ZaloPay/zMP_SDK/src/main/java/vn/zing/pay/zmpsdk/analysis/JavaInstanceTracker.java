/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.analysis.JavaInstanceTracker.java
 * Created date: Feb 26, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.analysis;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import android.os.Handler;

import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * @author YenNLH
 * 
 */
public class JavaInstanceTracker {
	private static final long mDelayIntervalDisplay = 20000;
	private static Handler mHandler = null;
	
	public static LinkedList<WeakReference<Object>> mWeakReferences = null;
	
	static {
//		if (Constants.IS_DEV) {
//			mHandler = new Handler();
//			mHandler.postDelayed(new DisplayThread(), mDelayIntervalDisplay);
//		}
	}
	
	public static class DisplayThread implements Runnable {
		@Override
		public void run() {
			collectGarbage();
			mHandler.postDelayed(this, mDelayIntervalDisplay);
		}
	}

	public static synchronized void observe(Object pObject) {
		if (!Constants.IS_DEV) {
			return;
		}
		
		if (mWeakReferences == null) {
			mWeakReferences = new LinkedList<>();
		}

		mWeakReferences.add(new WeakReference<Object>(pObject));
	}

	public static synchronized void collectGarbage() {
		if (!Constants.IS_DEV || mWeakReferences == null) {
			return;
		}
		
		System.gc();
		Log.e("@@@@ JavaInstanceTracker @@@@", "@@@@@@@ Collect Garbage @@@@@@@");
		Iterator<WeakReference<Object>> iter = mWeakReferences.iterator();
		while (iter.hasNext()) {
			WeakReference<Object> entry = iter.next();
			
			if (entry.get() == null) {
				iter.remove();
			} else {
				Log.e("@@@@ JavaInstanceTracker @@@@", entry.isEnqueued() + " " + entry.get().toString());
			}
		}
		Log.e("@@@@ JavaInstanceTracker @@@@", "Total: " + mWeakReferences.size());
	}
}
