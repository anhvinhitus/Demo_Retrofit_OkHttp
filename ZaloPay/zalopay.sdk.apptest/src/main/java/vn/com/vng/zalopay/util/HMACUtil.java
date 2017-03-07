/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 */
package vn.com.vng.zalopay.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;

import vn.com.zalopay.wallet.utils.HexStringUtil;

/**
 * This class will help you a HMAC string by calculating a message
 * authentication code (MAC) involving a cryptographic hash function in
 * combination with a secret cryptographic key.
 *
 * 
 */
public class HMACUtil {

	// @formatter:off
	public final static String HMACMD5 				= "HmacMD5";
	public final static String HMACSHA1 			= "HmacSHA1";
	public final static String HMACSHA256 			= "HmacSHA256";
	public final static String HMACSHA512 			= "HmacSHA512";
	public final static Charset UTF8CHARSET 		= Charset.forName("UTF-8");
	
	public final static LinkedList<String> HMACS 	= new LinkedList<String>(Arrays.asList("UnSupport", "HmacSHA256", "HmacMD5", "HmacSHA384", "HMacSHA1", "HmacSHA512"));
	// @formatter:on

	/**
	 * Calculating a message authentication code (MAC) involving a cryptographic
	 * hash function in combination with a secret cryptographic key.
	 * 
	 * @param algorithm
	 *            A cryptographic hash function (such as MD5 or SHA-1)
	 * 
	 * @param key
	 *            A secret cryptographic key
	 * 
	 * @param data
	 *            The message to be authenticated
	 * 
	 * @return The cryptographic strength of the HMAC depends upon the
	 *         cryptographic strength of the underlying hash function, the size
	 *         of its hash output, and on the size and quality of the key.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static byte[] HMacEncode(final String algorithm, final String key, final String data) {
		Mac macGenerator = null;
		try {
			macGenerator = Mac.getInstance(algorithm);
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(UTF8CHARSET), algorithm);
				macGenerator.init(signingKey);
			} else {
				SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), algorithm);
				macGenerator.init(signingKey);
			}

		} catch (Exception ex) {
		}

		if (macGenerator == null) {
			return null;
		}

		byte[] dataByte = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			dataByte = data.getBytes(UTF8CHARSET);
		} else {
			try {
				dataByte = data.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return macGenerator.doFinal(dataByte);
	}

	/**
	 * Calculating a message authentication code (MAC) involving a cryptographic
	 * hash function in combination with a secret cryptographic key.
	 * 
	 * The result will be represented base64-encoded string.
	 * 
	 * @param algorithm
	 *            A cryptographic hash function (such as MD5 or SHA-1)
	 * 
	 * @param key
	 *            A secret cryptographic key
	 * 
	 * @param data
	 *            The message to be authenticated
	 * 
	 * @return Base64-encoded HMAC String
	 */
	public static String HMacBase64Encode(final String algorithm, final String key, final String data) {
		byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
		if (hmacEncodeBytes == null) {
			return null;
		}
		return Base64.encodeToString(hmacEncodeBytes, Base64.DEFAULT);
	}

	/**
	 * Calculating a message authentication code (MAC) involving a cryptographic
	 * hash function in combination with a secret cryptographic key.
	 * 
	 * The result will be represented hex string.
	 * 
	 * @param algorithm
	 *            A cryptographic hash function (such as MD5 or SHA-1)
	 * 
	 * @param key
	 *            A secret cryptographic key
	 * 
	 * @param data
	 *            The message to be authenticated
	 * 
	 * @return Hex HMAC String
	 */
	public static String HMacHexStringEncode(final String algorithm, final String key, final String data) {
		byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
		if (hmacEncodeBytes == null) {
			return null;
		}
		return HexStringUtil.byteArrayToHexString(hmacEncodeBytes);
	}
}
