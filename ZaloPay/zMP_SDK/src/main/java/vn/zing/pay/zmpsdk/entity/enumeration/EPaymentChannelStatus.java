/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.EPaymentStatus.java
 * Created date: Dec 11, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.enumeration;

import com.google.gson.annotations.SerializedName;

/**
 * Represent status of a payment channel
 * 
 * @author YenNLH
 */
public enum EPaymentChannelStatus {

    @SerializedName("1")
	ENABLE(1), 
	
    @SerializedName("0")
	DISABLE(0), 

    @SerializedName("2")
	MAINTENANCE(2);

	private byte mValue = 0;

	private EPaymentChannelStatus(int pNum) {
		this.mValue = (byte) pNum;
	}

	/**
	 * Parse the integer input to get the equivalent enumeration value.
	 * 
	 * @param pString
	 *            The integer input
	 * 
	 * @return The equivalent enumeration value if any, return <code>null</code>
	 *         otherwise
	 */
	public static EPaymentChannelStatus fromInt(int pNum) {
		if (pNum < 0 || pNum > 2)
			return EPaymentChannelStatus.DISABLE;

		byte num = (byte) pNum;

		for (EPaymentChannelStatus status : EPaymentChannelStatus.values()) {
			if (status.mValue == num)
				return status;
		}
		return null;
	}

	/**
	 * Get value by integer
	 * 
	 * @return Integer value
	 */
	public int getValue() {
		return mValue;
	}
}
