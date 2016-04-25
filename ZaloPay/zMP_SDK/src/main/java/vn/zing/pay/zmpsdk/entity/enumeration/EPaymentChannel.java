/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.EPaymentMethodType.java
 * Created date: Dec 11, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.enumeration;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to declare the payment method type for a payment order
 * 
 * @author YenNLH
 * 
 */
public enum EPaymentChannel {
	// @formatter:off
	ZING_CARD				("zingpaysdk_conf_gwinfo_channel_zing_card"),
	TELCO_MOBI				("zingpaysdk_conf_gwinfo_channel_telco_mobi"),
	TELCO_VIETTEL			("zingpaysdk_conf_gwinfo_channel_telco_viettel"),
	TELCO_VINAPHONE			("zingpaysdk_conf_gwinfo_channel_telco_vina"),
	ATM						("zingpaysdk_conf_gwinfo_channel_atm"),
	SMS						("zingpaysdk_conf_gwinfo_channel_sms"),
	GOOGLE_WALLET			("zingpaysdk_conf_gwinfo_channel_google_wallet"),
	CREDIT_CARD				("zingpaysdk_conf_gwinfo_channel_credit_card"),
	MERGE_CARD				("MERGE_CARD");
//	ZING_XU					("zingpaysdk_conf_gwinfo_channel_zing_wallet"),
//	REDEEM_CODE				("REDEEM_CODE"),
//	MERGE_CARD				("MERGE_CARD"),
	// @formatter:on

	private final String name;

	private EPaymentChannel(String pName) {
		name = pName;
	}

	/**
	 * Compares this object to the specified string. The result is {@code true}
	 * if and only if the argument is not {@code null} and is a {@code String}
	 * object that represents the same meaning as this object.
	 * 
	 * @param pOtherName
	 *            The string to compare this {@code EPaymentChannel} against
	 * 
	 * @return {@code true} if the given string represents a
	 *         {@code EPaymentChannel} equivalent to this instance,
	 *         {@code false} otherwise
	 */
	public boolean equalsName(String pOtherName) {
		return (pOtherName == null) ? false : name.equals(pOtherName);
	}

	/**
	 * Returns a string containing a concise, human-readable description of this
	 * object. In this case, the enum constant's name is returned.
	 * 
	 * @overrides toString() in Enum
	 * 
	 * @return a printable representation of this object.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Parse the string input to get the equivalent enumeration value.
	 * 
	 * @param pString
	 *            The string input
	 * 
	 * @return The equivalent enumeration value if any, return <code>null</code>
	 *         otherwise
	 */
	public static EPaymentChannel fromString(String pString) {
		if (pString != null) {
			for (EPaymentChannel value : EPaymentChannel.values()) {
				if (pString.equalsIgnoreCase(value.name)) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * Combine all the value in this enumeration to a list.
	 * 
	 * @return The instance of {@link List} being full of values of this
	 *         enumeration
	 */
	public static List<EPaymentChannel> all() {
		List<EPaymentChannel> all = new ArrayList<EPaymentChannel>();
		for (EPaymentChannel type : EPaymentChannel.values()) {
			all.add(type);
		}
		return all;
	}
}
