/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.TGetBankListTask.java
 * Created date: Jan 14, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.atm;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import vn.zing.pay.zmpsdk.business.AdapterBase;
import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.ResourceManager;
import vn.zing.pay.zmpsdk.entity.atm.DAtmActiveBankItem;
import vn.zing.pay.zmpsdk.entity.atm.DAtmActiveBankList;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicSelectionViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicSelectionViewItem;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * @author YenNLH
 * 
 */
public class TAtmGetBankListTask extends TAbtractPaymentTask {
	private static AdapterATM adapterATM = null;

	public static void setAdapter(AdapterATM pAdapterATM) {
		synchronized (GlobalData.getApplication()) {
			adapterATM = pAdapterATM;
		}
	}

	public static boolean isFinished() {
		synchronized (GlobalData.getApplication()) {
			return adapterATM == null;
		}
	}

	public TAtmGetBankListTask(AdapterBase adapter) {
		super(adapter);
	}

	@Override
	protected void onPreExecute() {
		// do nothing
	}

	@Override
	protected String doInBackground(Void... args) {
		Log.d(this, "--- TGetBankListTask ---");
		try {
			HttpClientRequest clientRequest = new HttpClientRequest(Type.GET, URLDecoder.decode(
					Constants.getUrlPrefix() + Constants.URL_ATM_BANK_LIST, "utf-8"));

			String result = clientRequest.getText();
			DAtmActiveBankList activeBankList = GsonUtils.fromJsonString(result, DAtmActiveBankList.class);

			if (activeBankList.returnCode == 1) {
				Map<String, DAtmActiveBankItem> map = new HashMap<String, DAtmActiveBankItem>();
				for (DAtmActiveBankItem item : activeBankList.bankList) {
					map.put(item.code, item);
				}

				DDynamicSelectionViewGroup selectionView = ResourceManager.getInstance(AdapterATM.PAGE_CARD_INFO)
						.getDynamicView().SelectionView;

				Iterator<DDynamicSelectionViewItem> iter = selectionView.items.iterator();
				while (iter.hasNext()) {
					DDynamicSelectionViewItem entry = iter.next();
					DAtmActiveBankItem item = map.get(entry.pmcID);
					if (item == null) {
						iter.remove();
					}
				}
			}

			return result;
		} catch (Exception ex) {
			Log.e(this, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			synchronized (GlobalData.getApplication()) {
				mAdapter = adapterATM;
				adapterATM = null;

				if (mAdapter != null) {
					mAdapter.onEvent(EEventType.ON_GET_BANK_LIST_COMPLETED);
				}
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}
}
