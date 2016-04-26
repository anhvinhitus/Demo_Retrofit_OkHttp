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

/**
 * Represent status of a payment transaction
 * 
 * @author YenNLH
 */
public enum EPaymentStatus {
	/**
	 * Transaction is processing
	 */
	ZPC_TRANXSTATUS_PROCESSING(0),
	/**
	 * Transaction success(confirmed by application's server).
	 */
	ZPC_TRANXSTATUS_SUCCESS(1),
	/**
	 * Transaction failed.
	 */
	ZPC_TRANXSTATUS_FAIL(-1);

	private int mNum = -1;

	private EPaymentStatus(int pNum) {
		this.mNum = (byte) pNum;
	}
	
	public int getNum() {
		return mNum;
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
	public static EPaymentStatus fromInt(int pNum) {
		if (pNum < 0)
			return EPaymentStatus.ZPC_TRANXSTATUS_FAIL;

		byte num = (byte) pNum;

		for (EPaymentStatus status : EPaymentStatus.values()) {
			if (status.mNum == num)
				return status;
		}
		return null;
	}
}
