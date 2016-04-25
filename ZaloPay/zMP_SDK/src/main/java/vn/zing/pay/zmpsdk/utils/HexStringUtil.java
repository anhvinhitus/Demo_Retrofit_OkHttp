/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.utils.HexStringUtil.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.utils;

import java.util.Locale;

/**
 * Convert between byte array and hexadecimal string
 * 
 * @author YenNLH
 */
public class HexStringUtil {
	// @formatter:off
	static final byte[] HEX_CHAR_TABLE = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };
	// @formatter:on

	/**
	 * Convert a byte array to a hexadecimal string
	 * 
	 * @param raw
	 *            A raw byte array
	 * 
	 * @return Hexadecimal string
	 */
	public static String byteArrayToHexString(byte[] raw) {
		byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex);
	}

	/**
	 * Convert a hexadecimal string to a byte array
	 * 
	 * @param raw
	 *            A hexadecimal string
	 * 
	 * @return The byte array
	 */
	public static byte[] hexStringToByteArray(String hex) {
		String hexstandard = hex.toLowerCase(Locale.ENGLISH);
		int sz = hexstandard.length() / 2;
		byte[] bytesResult = new byte[sz];

		int idx = 0;
		for (int i = 0; i < sz; i++) {
			bytesResult[i] = (byte) (hexstandard.charAt(idx));
			++idx;
			byte tmp = (byte) (hexstandard.charAt(idx));
			++idx;

			if (bytesResult[i] > HEX_CHAR_TABLE[9]) {
				bytesResult[i] -= ((byte) ('a') - 10);
			} else {
				bytesResult[i] -= (byte) ('0');
			}
			if (tmp > HEX_CHAR_TABLE[9]) {
				tmp -= ((byte) ('a') - 10);
			} else {
				tmp -= (byte) ('0');
			}

			bytesResult[i] = (byte) (bytesResult[i] * 16 + tmp);
		}
		return bytesResult;
	}
}
