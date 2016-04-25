/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.SharePreferencesManager.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data;

import java.util.ArrayList;

import vn.zing.pay.zmpsdk.data.base.SingletonBase;
import vn.zing.pay.zmpsdk.entity.atm.DAtmCardCache;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import android.content.SharedPreferences;
import android.util.SparseArray;

/**
 * @author YenNLH
 * 
 */
public class SharedPreferencesManager extends SingletonBase {

	private static SharedPreferencesManager mSharePreferencesManager = null;

	public static synchronized SharedPreferencesManager getInstance() {
		if (mSharePreferencesManager == null)
			mSharePreferencesManager = new SharedPreferencesManager();

		return mSharePreferencesManager;
	}

	public static final String SHARE_PREFERENCES_NAME = "ZING_PAY_CONFIG";
	private SharedPreferences mCommonSharedPreferences = null;

	public SharedPreferencesManager() {
		super();
	}

	public synchronized SharedPreferences getSharedPreferences() {
		if (GlobalData.getApplication() == null) {
			return null;
		}

		if (mCommonSharedPreferences != null)
			return mCommonSharedPreferences;

		mCommonSharedPreferences = GlobalData.getApplication().getSharedPreferences(SHARE_PREFERENCES_NAME, 0);
		return mCommonSharedPreferences;
	}

	/**
	 * Retrieve a String value from the preferences.
	 * 
	 * @param pKey
	 *            The name of the preference to retrieve.
	 * 
	 * @return Returns the preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a String.
	 */
	private String getString(String pKey) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getString(pKey, null);

		return null;
	}

	public boolean setString(String pKey, String pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putString(pKey, pValue).commit();
		}

		return false;
	}

	/**
	 * Retrieve a long value from the preferences.
	 * 
	 * @param pKey
	 *            The name of the preference to retrieve.
	 * 
	 * @return Returns the preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a long.
	 */
	private long getLong(String pKey) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getLong(pKey, Long.MIN_VALUE);

		return Long.MIN_VALUE;
	}

	public boolean setLong(String pKey, long pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putLong(pKey, pValue).commit();
		}

		return false;
	}

	/**
	 * Retrieve a int value from the preferences.
	 * 
	 * @param pKey
	 *            The name of the preference to retrieve.
	 * 
	 * @return Returns the preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a int.
	 */
	private int getInt(String pKey) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getInt(pKey, Integer.MIN_VALUE);

		return Integer.MIN_VALUE;
	}

	public boolean setInt(String pKey, int pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putInt(pKey, pValue).commit();
		}

		return false;
	}

	/**
	 * Retrieve a boolean value from the preferences.
	 * 
	 * @param pKey
	 *            The name of the preference to retrieve.
	 * 
	 * @return Returns the preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a boolean.
	 */
	@SuppressWarnings("unused")
	private boolean getBoolean(String pKey, boolean defaultValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getBoolean(pKey, defaultValue);

		return defaultValue;
	}

	/*******************************************************************
	 * ************* METHOD FOR GETTING CONFIG VALUE *******************
	 *******************************************************************/

	public long getGatewayInfoExpriedTime() {
		return getLong(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_expired_time));
	}

	public boolean setGatewayInfoExpriedTime(long pValue) {
		return setLong(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_expired_time), pValue);
	}

	// ////////////////////////////////////////////////////////////////

	public String getChecksumSDKversion() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_sdk_ver));
	}

	public boolean setChecksumSDKversion(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_sdk_ver), pValue);
	}

	// ////////////////////////////////////////////////////////////////

	public String getChecksumSDK() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_sdk_checksum));
	}

	public boolean setChecksumSDK(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_sdk_checksum), pValue);
	}

	// ////////////////////////////////////////////////////////////////

	public String getUnzipPath() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_unzip_path));
	}

	public boolean setUnzipPath(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_unzip_path), pValue);
	}

	// ////////////////////////////////////////////////////////////////

	public String getResourceVersion() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_res_ver));
	}

	public boolean setResourceVersion(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_res_ver), pValue);
	}

	// ////////////////////////////////////////////////////////////////

	public String getAppName() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_app_name));
	}

	public boolean setAppName(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_app_name), pValue);
	}

	// ////////////////////////////////////////////////////////////////

	public String getUDID() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_udid));
	}

	public boolean setUDID(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_udid), pValue);
	}

	// ////////////////////////////////////////////////////////////////
	// ////////////////////// GROUP CHANNEL ///////////////////////////

	public boolean setGroupPmcConfig(int pID, String pConfig) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_prefix) + pID, pConfig);
	}

	public String getGroupChannelConfig(String pID) {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_prefix) + pID);
	}

	public String getATMGChannelConfig() {
		return getGroupChannelConfig(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_atm));
	}

	public String getCreditCardGChannelConfig() {
		return getGroupChannelConfig(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_credit_card));
	}

	public String getGoogleWalletGChannelConfig() {
		return getGroupChannelConfig(GlobalData
				.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_google_wallet));
	}

	public String getSMSGChannelConfig() {
		return getGroupChannelConfig(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_sms));
	}

	public String getTelcoGChannelConfig() {
		return getGroupChannelConfig(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_telco));
	}

	public String getZingCardGChannelConfig() {
		return getGroupChannelConfig(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_zing_card));
	}

	public boolean setGChannelConfigList(String pList) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_list), pList);
	}

	public String getGChannelConfigList() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_gchannel_list));
	}

	// ////////////////////////////////////////////////////////////////
	// ////////////////////// PAYMENT CHANNEL /////////////////////////

	public boolean setPmcConfig(int pID, String pConfig) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_prefix) + pID, pConfig);
	}

	public String getPmcConfigByPmcID(String pID) {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_prefix) + pID);
	}

	public boolean setPmcConfigList(SparseArray<ArrayList<Integer>> pList) {

		// groupID-id,id,id~groupID-id,id,id~
		StringBuilder pmcIdList = new StringBuilder();
		for (int i = 0; i < pList.size(); i++) {
			int key = pList.keyAt(i);
			// get the object by the key.
			ArrayList<Integer> channelList = pList.get(key);
			pmcIdList.append(key);
			pmcIdList.append(Constants.HYPHEN);

			for (Integer num : channelList) {
				pmcIdList.append(num);
				pmcIdList.append(Constants.COMMA);
			}
			pmcIdList.append(Constants.TILDE);
		}

		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_list),
				pmcIdList.toString());
	}

	public SparseArray<ArrayList<String>> getPmcConfigList() {
		SparseArray<ArrayList<String>> result = new SparseArray<ArrayList<String>>();
		String raw = getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_list));

		try {
			for (String groupId_pmcList : raw.split(Constants.TILDE)) {
				String[] array = groupId_pmcList.split(Constants.HYPHEN);
				int groupId = Integer.parseInt(array[0]);

				array = array[1].split(Constants.COMMA);
				ArrayList<String> pmcList = new ArrayList<>();
				for (int i = 0; i < array.length; i++) {
					pmcList.add(array[i]);
				}

				result.put(groupId, pmcList);
			}

			return result;
		} catch (Exception ex) {
			Log.e(this, ex);
		}

		return null;
	}

	public String getATMChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_atm));
	}

	public String getVisaCardChannelConfig() {
		return getPmcConfigByPmcID(GlobalData
				.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_credit_card_visa));
	}

	public String getMasterCardChannelConfig() {
		return getPmcConfigByPmcID(GlobalData
				.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_credit_card_master));
	}

	public String getJCBCardChannelConfig() {
		return getPmcConfigByPmcID(GlobalData
				.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_credit_card_jcb));
	}

	public String getGoogleWalletChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_google_wallet));
	}

	public String getSMSChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_sms));
	}

	public String getMobiChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_telco_mobi));
	}

	public String getVinaChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_telco_vina));
	}

	public String getViettelChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_telco_viettel));
	}

	public String getZingCardChannelConfig() {
		return getPmcConfigByPmcID(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_channel_zing_card));
	}

	public boolean setLastSuccessPmcGroupId(String pValue) {
		return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_last_success_pmc_id), pValue);
	}

	public String getLastSuccessPmcGroupId() {
		return getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_last_success_pmc_id));
	}

	// ////////////////////////////////////
	// LAST SUCCESSFUL CARD INFORMATION //
	// ////////////////////////////////////

	public boolean setCardInfo(DAtmCardCache pCard) {
		if (pCard == null) {
			SharedPreferences sharedPreferences = getSharedPreferences();
			return sharedPreferences.edit()
					.remove(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_last_success_atm_card)).commit();
		} else {
			setNumOfWrong4LastDigits(0);
			return setString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_last_success_atm_card),
					pCard.toJsonString());
		}
	}

	public DAtmCardCache getCardInfo() {
		String json = getString(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_last_success_atm_card));

		if (json != null) {
			return GsonUtils.fromJsonString(json, DAtmCardCache.class);
		}

		return null;
	}

	public int getNumOfWrong4LastDigits() {
		return getInt(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_num_of_wrong_4_last_digits));
	}

	public boolean setNumOfWrong4LastDigits(int pValue) {
		return setInt(GlobalData.getStringResource(Resource.string.zingpaysdk_conf_num_of_wrong_4_last_digits), pValue);
	}

	// /////////////////////////////
	// GOOGLE CLOUD MESSAGE TOKEN //
	// /////////////////////////////

	public String getGCMToken() {
		return getString(GlobalData.getStringResource(Resource.string.gcm_defaultSenderId));
	}

	public boolean setGcmToken(String pToken) {
		return setString(GlobalData.getStringResource(Resource.string.gcm_defaultSenderId), pToken);
	}

}
