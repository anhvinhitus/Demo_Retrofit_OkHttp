/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.SingletonManager.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;

/**
 * This class is used to manage all static instance
 * 
 * @author YenNLH
 * 
 */
public class SingletonLifeCircleManager {
	private static Set<Class<?>> mClassHasStaticAttrList = null;

	/**
	 * Register an instance implementing {@link SingletonBase} class in order to
	 * manage and release all singleton instances when finished.
	 * 
	 * @param pSingletonInstance
	 */
	public static synchronized void register(Class<?> pClassHasStaticAttr) {
		if (mClassHasStaticAttrList == null) {
			mClassHasStaticAttrList = new HashSet<Class<?>>();
		}

		mClassHasStaticAttrList.add(pClassHasStaticAttr);
	}

	/**
	 * Release all singleton instances immediately.
	 */
	public static synchronized void disposeAll() {
		dispose(GlobalData.class);
		dispose(DialogManager.class);

		if (mClassHasStaticAttrList != null) {

			for (Class<?> clazz : mClassHasStaticAttrList) {
				dispose(clazz);
			}

			mClassHasStaticAttrList = null;
		}
	}

	/**
	 * Release all available static instance in this class by assigning them to
	 * {@code null}.
	 * 
	 * This step will help Java garbage collector to detect and destroy cycles
	 * of objects that refer to each other, but are not referenced by any other
	 * active objects.
	 */
	private static void dispose(Class<?> clazz) {
		Log.i("RELEASE_STATIC_OBJ", "======== Considering to :" + clazz.getName());
		
		Field[] fields = clazz.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {

			if (Modifier.isStatic(fields[i].getModifiers()) && !Modifier.isFinal(fields[i].getModifiers())
					&& !fields[i].getType().isPrimitive()) {
				try {

					if (Modifier.isPrivate(fields[i].getModifiers())) {
						fields[i].setAccessible(true);
					}

					fields[i].set(null, null);

					Log.i("RELEASE_STATIC_OBJ", "**** Release " + fields[i].getName());
				} catch (Exception e) {
					Log.e("RELEASE_STATIC_OBJ", e);
				}
			}
		}
	}
}
