/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.ResourceManager.java
 * Created date: Dec 21, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import vn.zing.pay.zmpsdk.analysis.JavaInstanceTracker;
import vn.zing.pay.zmpsdk.data.base.SingletonBase;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DPaymentChannel;
import vn.zing.pay.zmpsdk.entity.staticconfig.DConfigFromServer;
import vn.zing.pay.zmpsdk.entity.staticconfig.DPage;
import vn.zing.pay.zmpsdk.entity.staticconfig.bank.DBankScript;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicSelectionViewItem;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DStaticViewGroup;
import vn.zing.pay.zmpsdk.view.ActivityRendering;
import vn.zing.pay.zmpsdk.view.BasePaymentActivity;

/**
 * @author YenNLH
 * 
 */
public class ResourceManager extends SingletonBase {

	private static String mUnzipPath = null;
	private static ResourceManager mCommonResourceManager = null;
	private static Map<String, ResourceManager> mResourceManagerMap = null;

	public static synchronized ResourceManager getInstance(String pPageName) {
		if (pPageName == null) {
			if (mCommonResourceManager == null)
				mCommonResourceManager = new ResourceManager(null);
			return mCommonResourceManager;
		} else {
			if (mResourceManagerMap == null) {
				mResourceManagerMap = new HashMap<String, ResourceManager>();
			}

			ResourceManager resourceManager = mResourceManagerMap.get(pPageName);

			if (resourceManager != null) {
				return resourceManager;
			}

			resourceManager = new ResourceManager(pPageName);
			mResourceManagerMap.put(pPageName, resourceManager);
			return resourceManager;
		}
	}

	/************************************************************************/
	/************************************************************************/
	/************************************************************************/

	private static String getUnzipFolderPath() throws Exception {
		if (SharedPreferencesManager.getInstance() == null)
			throw new Exception("Missing shared preferences!!!");

		if (mUnzipPath == null)
			mUnzipPath = SharedPreferencesManager.getInstance().getUnzipPath();

		return mUnzipPath;
	}

	private static String loadResourceFile(String pPathNamePrefix, String pFileName) throws Exception {
		String result = "";
		String path = getUnzipFolderPath() + File.separator
				+ ((pPathNamePrefix != null) ? (pPathNamePrefix + pFileName) : pFileName);
		File file = new File(path);

		if (file.exists()) {
			try {
				InputStream inputStream = new FileInputStream(file);
				if (inputStream != null) {
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

					String line = "";
					StringBuilder stringBuilder = new StringBuilder();

					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line);
						stringBuilder.append("\r\n");
					}

					bufferedReader.close();
					inputStreamReader.close();
					inputStream.close();

					result = stringBuilder.toString();
				}
			} catch (Exception ex) {
				Log.e(ResourceManager.class.getName(), ex.getMessage(), ex);
			}
		}
		result = result.trim();
		return result;
	}

	public static synchronized void initResource() throws Exception {
		String json = loadResourceFile(null, CONFIG_FILE);
		mConfigFromServer = (new DConfigFromServer()).fromJsonString(json);

		/**********
		 * STRING *
		 **********/
		ResourceManager commonResourceManager = getInstance(null);
		if (mConfigFromServer.stringMap != null) {
			commonResourceManager.setString(mConfigFromServer.stringMap);
		}

		/*********
		 * PAGES *
		 *********/

		if (mConfigFromServer.pageList != null) {
			for (DPage page : mConfigFromServer.pageList) {
				if (page.dynamicView != null) {
					removeUnusedDynamicView(page.dynamicView);
				}
				getInstance(page.pageName).mPageConfig = page;
			}
		}
	}

	/**
	 * Remove all dynamic views that not be available in current application.
	 * 
	 * @param pViewGroup
	 *            {@link DViewGroup} instance
	 */
	private static void removeUnusedDynamicView(DDynamicViewGroup pViewGroup) {

		if (pViewGroup.SelectionView != null) {
			String pmcIdIncluded = GlobalData.getPaymentOption().getIncludePaymentMethodType();
			Iterator<DDynamicSelectionViewItem> iter = pViewGroup.SelectionView.items.iterator();
			while (iter.hasNext()) {
				DDynamicSelectionViewItem entry = iter.next();

				if (entry != null) {

					String paymentChannelConfig = SharedPreferencesManager.getInstance().getPmcConfigByPmcID(
							String.valueOf(entry.pmcID));

					// Remove view if it is unused
					if (!TextUtils.isEmpty(paymentChannelConfig)) {
						DPaymentChannel paymentChannel = (new DPaymentChannel()).fromJsonString(paymentChannelConfig);

						if (!paymentChannel.isEnable()) {
							iter.remove();
							continue;
						}
					}

					// Remove dynamic view (payment channel) ignored by
					// application
					if (GlobalData.getPaymentOption() != null && pViewGroup.SelectionView.isFilterPmc) {
						if (pmcIdIncluded != null) {
							if (!entry.pmcID.equals(pmcIdIncluded)) {
								iter.remove();
							}
						} else if (GlobalData.getPaymentOption().getExcludePaymentMethodTypes() != null) {
							HashSet<String> excludePaymentMethodTypes = GlobalData.getPaymentOption()
									.getExcludePaymentMethodTypes();

							if (excludePaymentMethodTypes.contains(entry.pmcID)) {
								iter.remove();
							}
						}
					}
				}
			}
		}
	}

	public static String getJavascriptContent(String pJsName) {
		try {
			return loadResourceFile(PREFIX_JS, pJsName);
		} catch (Exception e) {
			Log.e(ResourceManager.class.getName(), e.getMessage(), e);
		}
		return null;
	}

	public static Bitmap getImage(String imageName) {
		if (imageName.equals(HIDE_IMG_NAME)) {
			return null;
		}

		String imgLocalPath = String.format("%s%s%s%s%s", mUnzipPath, File.separator, PREFIX_IMG, File.separator,
				imageName);
		Bitmap bitmap = BitmapFactory.decodeFile(imgLocalPath);

		if (Constants.IS_DEV) {
			JavaInstanceTracker.observe(bitmap);
		}

		return bitmap;
	}

	/************************************************************************/
	/************************************************************************/
	/************************************************************************/

	private static final String PREFIX_JS = "/js/";
	private static final String PREFIX_IMG = "/img/";
	private static final String CONFIG_FILE = "config.json";
	private static final String HIDE_IMG_NAME = "0.png";

	private static DConfigFromServer mConfigFromServer = null;

	private String mPageName = null;
	private HashMap<String, String> mStringMap = null;
	private DPage mPageConfig = null;

	public ResourceManager(String pPageName) {
		super();
		this.mPageName = pPageName;
	}

	/**
	 * @return the mPageName
	 */
	public String getmPageName() {
		return mPageName;
	}

	/**
	 * Get string value from map specified by server side. Only for common
	 * resource.
	 * 
	 * @param pKey
	 *            The key of pair
	 * @return String value
	 */
	public String getString(String pKey) {
		if (this.mStringMap != null) {
			return this.mStringMap.get(pKey);
		}
		return null;
	}

	private void setString(HashMap<String, String> pMap) {
		if (pMap != null) {
			this.mStringMap = pMap;
		}
	}

	public String getPattern(String pViewID, String pPmcID) {
		if (mConfigFromServer.pattern != null) {
			HashMap<String, String> patternMap = mConfigFromServer.pattern.get(pViewID);
			if (patternMap != null) {
				return patternMap.get(pPmcID);
			}
		}
		return null;
	}

	public String getBankByCardCode(String pCode) {
		if (mConfigFromServer.bankCardCode != null) {
			return mConfigFromServer.bankCardCode.get(pCode);
		}
		return null;
	}

	public int getMaxLengthOfCardCodePattern() {
		if (mConfigFromServer.bankCardCode == null)
			return 0;

		int ret = 0;
		for (String str : mConfigFromServer.bankCardCode.keySet()) {
			if (str.length() > ret) {
				ret = str.length();
			}
		}

		return ret;
	}

	public List<DBankScript> getBankScripts() {
		if (mConfigFromServer == null)
			return null;

		return mConfigFromServer.bankScripts;
	}

	public DStaticViewGroup getStaticView() {
		return this.mPageConfig.staticView;
	}

	public DDynamicViewGroup getDynamicView() {
		return this.mPageConfig.dynamicView;
	}

	public ActivityRendering produceRendering(BasePaymentActivity pActivity) {
		if (this.mPageConfig == null)
			return null;

		return new ActivityRendering(this, pActivity);
	}
}
