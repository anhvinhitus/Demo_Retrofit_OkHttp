/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.gwinfo.GetGatewayInfoTask.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.gwinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;

import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.data.SharedPreferencesManager;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DGatewayInfo;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DGroupPaymentChannel;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DPaymentChannel;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.listener.ZPGetGatewayInfoListener;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.DimensionUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.utils.StorageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.SparseArray;

/**
 * @author YenNLH
 * 
 */
public class TGetGatewayInfoTask extends AsyncTask<Void, Void, DGatewayInfo> {
	private long mStartTime;

	private String mResourceZipFileURL;
	private String mUnzipFolder;

	private ZPGetGatewayInfoListener mCallBack;

	public TGetGatewayInfoTask(ZPGetGatewayInfoListener pListener) {
		this.mCallBack = pListener;
	}

	@Override
	protected DGatewayInfo doInBackground(Void... pParams) {
		String checksum = SharedPreferencesManager.getInstance().getChecksumSDK();
		String checksumSDKV = SharedPreferencesManager.getInstance().getChecksumSDKversion();
		String resrcVer = SharedPreferencesManager.getInstance().getResourceVersion();

		// New version of SDK was installed on this device or resource files is
		// missing, try to get all new gateway information
		if (!Constants.VERSION.equals(checksumSDKV) || !BGatewayInfo.isValidConfig()) {
			checksum = "";
			resrcVer = "";
		}

		String url = Constants.getUrlPrefix() + Constants.URL_GATEWAY_INFO;
		String mno = ConnectionUtil.getSimOperator(GlobalData.getApplication());
		StringBuilder sb = new StringBuilder();
		try {
			// @formatter:off
			HttpClientRequest request = new HttpClientRequest(Type.GET, URLDecoder.decode(url, "utf-8"));
			TAbtractPaymentTask.putBasicInfo(request);
			//.append("&code=").append(URLEncoder.encode(ZaloOAuth.Instance.getOAuthCode(), "UTF-8"))
			request.addParams("appInfoCheckSum", checksum);
			request.addParams("resourceVersion", resrcVer);
			request.addParams("dscreenType", DimensionUtil.getDensity(GlobalData.getOwnerActivity()));
			// @formatter:on

			if (!ConnectionUtil.isDualSim(GlobalData.getApplication()))
				sb.append("&mno=").append(mno);

			mStartTime = System.currentTimeMillis();
			String result = request.getText();
			request.close();

			Log.d(getClass().getName(),
					"Get INFO finished! Load time: " + String.valueOf(System.currentTimeMillis() - mStartTime) + "ms");

			if (result != null) {
				Log.d(getClass().getName(), result.toString());
				// Parse JSON
				DGatewayInfo gatewayInfo = (new DGatewayInfo()).fromJsonString(result);

				if (gatewayInfo.returnCode == 1) {
					// Set exprired time
					long expiredTime = gatewayInfo.expiredTime + System.currentTimeMillis();
					SharedPreferencesManager.getInstance().setGatewayInfoExpriedTime(expiredTime);

					if (gatewayInfo.isUpdateAppInfo) {
						// Checksum string
						SharedPreferencesManager.getInstance().setChecksumSDK(gatewayInfo.appInfoCheckSum);

						// Save status of every group channel
						StringBuilder pmcGroupIdList = new StringBuilder();
						if (gatewayInfo.appInfo.pmcGroupList != null && gatewayInfo.appInfo.pmcGroupList.size() > 0) {
							for (DGroupPaymentChannel channel : gatewayInfo.appInfo.pmcGroupList) {

								pmcGroupIdList.append(channel.groupID);
								pmcGroupIdList.append(Constants.COMMA);

								SharedPreferencesManager.getInstance().setGroupPmcConfig(channel.groupID,
										channel.toJsonString());
							}
						}
						SharedPreferencesManager.getInstance().setGChannelConfigList(pmcGroupIdList.toString());

						// Save status of every channel
						SparseArray<ArrayList<Integer>> groupToChannelListMap = new SparseArray<ArrayList<Integer>>();
						if (gatewayInfo.appInfo.pmcList != null && gatewayInfo.appInfo.pmcList.size() > 0) {
							for (DPaymentChannel channel : gatewayInfo.appInfo.pmcList) {

								// Just store enable payment channel
								if (channel.isEnable()) {
									ArrayList<Integer> channelList = groupToChannelListMap.get(channel.groupID);
									if (channelList == null) {
										channelList = new ArrayList<Integer>();
										channelList.add(channel.pmcID);
										groupToChannelListMap.put(channel.groupID, channelList);
									} else {
										channelList.add(channel.pmcID);
									}
								}

								SharedPreferencesManager.getInstance().setPmcConfig(channel.pmcID,
										channel.toJsonString());
							}
						}
						SharedPreferencesManager.getInstance().setPmcConfigList(groupToChannelListMap);

						// App icon
						if (gatewayInfo.appInfo.appEntity != null) {
							saveAppLogo(gatewayInfo.appInfo.appEntity.appLogoUrl);
							SharedPreferencesManager.getInstance().setAppName(gatewayInfo.appInfo.appEntity.appName);
						}

						if (gatewayInfo.isUpdateResource) {
							SharedPreferencesManager.getInstance().setResourceVersion(gatewayInfo.resource.rsVersion);

							// Start downloading
							this.mResourceZipFileURL = gatewayInfo.resource.rsUrl;
							if (!downloadResourceZipFile()) {
								return null;
							}
						}
					}
				}

				return gatewayInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Download the ap-to-date resource zip file from server and decompress it
	 * on storage (internal storage or external sd-card)
	 * 
	 * @return {@code TRUE} if success, {@code FALSE} otherwise
	 */
	protected boolean downloadResourceZipFile() {
		// Prepare unzip folder
		prepareUnzipFolder();

		// Start task
		mStartTime = System.currentTimeMillis();
		String resrcVer = SharedPreferencesManager.getInstance().getResourceVersion();

		if (mResourceZipFileURL == null || resrcVer == null) {
			return false;
		} else {
			long current = System.currentTimeMillis();
			StorageUtil.deleteRecursive(new File(this.mUnzipFolder));
			Log.d(getClass().getName(),
					"DeleteRecursive finished! Load time: " + String.valueOf(System.currentTimeMillis() - current)
							+ "ms");

			try {
				current = System.currentTimeMillis();

				// Clear heap
				System.gc();

				byte[] compressedBytes = HttpClientRequest.getByteArray(this.mResourceZipFileURL);
				Log.d(getClass().getName(),
						"Zip down finished! Load time: " + String.valueOf(System.currentTimeMillis() - current) + "ms");

				current = System.currentTimeMillis();
				StorageUtil.decompress(compressedBytes, this.mUnzipFolder + "/" + resrcVer);
				Log.d(getClass().getName(),
						"Decompress file finished! Load time: " + String.valueOf(System.currentTimeMillis() - current)
								+ "ms");
			} catch (NullPointerException e) {
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		SharedPreferencesManager.getInstance().setUnzipPath(this.mUnzipFolder + "/" + resrcVer);
		SharedPreferencesManager.getInstance().setChecksumSDKversion(Constants.VERSION);

		Log.d(getClass().getName(),
				"Completed! Load time: " + String.valueOf(System.currentTimeMillis() - this.mStartTime) + "ms");

		return true;
	}

	@Override
	protected void onPostExecute(DGatewayInfo result) {
		if (result == null || result.returnCode < 0) {
			this.mCallBack.onError(result);
		} else {
			// No error
			this.mCallBack.onSuccess();
		}
	}

	/**
	 * Save application logo image into storage
	 * 
	 * @param appLogoUrl
	 *            Internet location of image
	 */
	private void saveAppLogo(String appLogoUrl) {
		FileOutputStream out = null;
		try {
			out = GlobalData.getApplication().openFileOutput(
					GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_app_logo), Context.MODE_PRIVATE);

			HttpClientRequest clientRequest = new HttpClientRequest(Type.GET, appLogoUrl);
			Bitmap bmp = clientRequest.getImage();
			clientRequest.close();

			bmp.setDensity(Bitmap.DENSITY_NONE);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			Log.e(this, e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Prepare the available folder that held resource files
	 */
	private void prepareUnzipFolder() {
		File downloadFolder = GlobalData.getApplication().getDir("temp", Context.MODE_PRIVATE);

		if (StorageUtil.isExternalStorageAvailable()) {
			/*
			 * Download zip file from network, in this demo we assume all files
			 * were unziped and stored in temporary location in sdcard or
			 * internal memory
			 */
			mUnzipFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar
					+ GlobalData.getStringResource(Resource.string.zingpaysdk_conf_gwinfo_unzip_folder);

			File f = new File(mUnzipFolder);
			if (!f.isDirectory() || !f.exists()) {
				f.mkdirs();
			}

		} else {
			// Use Internal Storage to unzip file
			if (downloadFolder != null && downloadFolder.isDirectory()) {
				mUnzipFolder = downloadFolder.getAbsolutePath();
			}
		}
	}
}
