/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.ZPPaymentOption.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;

import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.utils.Log;

/**
 * This class is used to describe what payment channel user wants to directly
 * enter or what channels user wants to disable
 * 
 * @author YenNLH
 * 
 */
public class ZPPaymentOption extends DBaseEntity<ZPPaymentOption> {

	/**
	 * Payment method type user wants to directly enter
	 */
	private EPaymentChannel includePaymentMethodType = null;

	/**
	 * Payment method types user wants to disable
	 */
	private List<EPaymentChannel> excludePaymentMethodTypes = null;
	private HashSet<String> mExcludePmcIdSet = null;

	public ZPPaymentOption() {
	}

	public ZPPaymentOption(EPaymentChannel includeChannel) {
		if (includeChannel == EPaymentChannel.MERGE_CARD) {
			excludePaymentMethodTypes = new ArrayList<>();
			excludePaymentMethodTypes.add(EPaymentChannel.ATM);
			excludePaymentMethodTypes.add(EPaymentChannel.CREDIT_CARD);
			excludePaymentMethodTypes.add(EPaymentChannel.GOOGLE_WALLET);
			excludePaymentMethodTypes.add(EPaymentChannel.SMS);
		} else {
			this.includePaymentMethodType = includeChannel;
		}
	}

	// public ZPPaymentOption(EPaymentChannel includeChannel,
	// List<EPaymentChannel> excludeChannels) {
	// this.includePaymentMethodType = includeChannel;
	// this.excludePaymentMethodTypes = excludeChannels;
	// }

	/**
	 * @return the includePaymentMethodType
	 */
	public String getIncludePaymentMethodType() {
		if (includePaymentMethodType == null)
			return null;

		String pmcIDStr = GlobalData.getStringResource(includePaymentMethodType.toString());
		return pmcIDStr;
	}

	/**
	 * @return the excludePaymentMethodTypes
	 */
	public HashSet<String> getExcludePaymentMethodTypes() {
		if (excludePaymentMethodTypes == null)
			return null;

		if (mExcludePmcIdSet == null) {
			mExcludePmcIdSet = new HashSet<String>();
			for (EPaymentChannel paymentMethodType : excludePaymentMethodTypes) {
				String pmcIDStr = GlobalData.getStringResource(paymentMethodType.toString());
				try {
					mExcludePmcIdSet.add(pmcIDStr);
				} catch (Exception ex) {
					Log.e(this, ex);
				}
			}
		}
		return mExcludePmcIdSet;
	}

	/**
	 * Parse the json string input to an {@link ZPPaymentOption} Object
	 * representation of the same
	 * 
	 * @param pJson
	 *            The JSON string input
	 * 
	 * @return {@link ZPPaymentOption} Object
	 */
	public static ZPPaymentOption fromJson(String pJson) {
		return (new Gson()).fromJson(pJson, ZPPaymentOption.class);
	}
}
