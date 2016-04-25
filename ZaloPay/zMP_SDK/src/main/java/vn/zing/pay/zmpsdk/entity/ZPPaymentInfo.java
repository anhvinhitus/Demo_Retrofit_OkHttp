/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.ZPPaymentInfo.java
 * Created date: Dec 11, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity;

import java.util.List;

import android.text.TextUtils;

import com.google.gson.Gson;

import vn.zing.pay.zmpsdk.helper.google.IabHelper;

/**
 * This class reprensents all information about an payment order
 * 
 * @author YenNLH
 * 
 */
public class ZPPaymentInfo extends DBaseEntity<ZPPaymentInfo> {
	public long appID;
	public String appTransID;
	public long appTime;

	public long amount;
	// public long suggestAmount;
	public List<ZPPaymentItem> items;
	public String description;
	public String embedData;

	public String mac;
	public String skuID;
	public String payType = IabHelper.ITEM_TYPE_INAPP;
	// ZPSDK only supports for this item type

	public String appUser;
	public String displayName;
	public String displayInfo;

	public TelcoHighlight telcoHighlight;
	public List<OCROffChannel> ocrOffChannels;

	/**
	 * Declare what type of card you don't want to enable optical character
	 * reader feature
	 * 
	 * @author YenNLH
	 */
	public static enum OCROffChannel {
		ZING_CARD, TELCO, ATM
	}

	/**
	 * Declare what Mobile Network Operator that you want to highlight
	 * 
	 * @author YenNLH
	 */
	public static enum TelcoHighlight {
		TELCO_MOBI, TELCO_VIETTEL, TELCO_VINAPHONE,
	}

	/**
	 * Parse the json string input to an {@link ZPPaymentInfo} Object
	 * representation of the same
	 * 
	 * @param pJson
	 *            The JSON string input
	 * 
	 * @return {@link ZPPaymentInfo} Object
	 */
	public static ZPPaymentInfo fromJson(String pJson) {
		return (new Gson()).fromJson(pJson, ZPPaymentInfo.class);
	}

	public boolean verifyGooglePaymentInfo() {
		return !TextUtils.isEmpty(this.skuID) && !TextUtils.isEmpty(this.payType)
				&& this.payType.equals(IabHelper.ITEM_TYPE_INAPP);
	}
}
