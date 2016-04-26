/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.entity.enumeration.EPaymentReturnCode.java
 * Created date: Jan 25, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.enumeration;

/**
 * @author YenNLH
 * 
 */
public enum EPaymentReturnCode {

	EXCEPTION(0),
	ATM_VERIFY_OTP_SUCCESS(13),
	ATM_RETRY_CAPTCHA(16),
	ATM_RETRY_OTP(17),
	ATM_CAPTCHA_INVALID(-50);

	private byte mValue = 3;

	private EPaymentReturnCode(int pNum) {
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
	public static EPaymentReturnCode fromInt(int pNum) {
		byte num = (byte) pNum;

		for (EPaymentReturnCode status : EPaymentReturnCode.values()) {
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
