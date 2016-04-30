/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.Constants.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data;

/**
 * @author YenNLH
 * 
 */
public class Constants {

	// @formatter:off
	public static final boolean IS_RELEASE 		= true;
	public static final boolean IS_DEV 			= (IS_RELEASE) ? false : true;
	public static final boolean IS_SWITCHABLE 	= (IS_RELEASE) ? false : true;
	public static final String VERSION 			= "1.3.2";

	public static final String COMMA 			= ",";
	public static final String HYPHEN 			= "-";
	public static final String TILDE 			= "~";
	public static final String PROMOTION_TAG 	= "promotion";
	
	//////////////////////////////////////////////////////////////
	////////////////////////// URL ///////////////////////////////
	//////////////////////////////////////////////////////////////
	public static enum HostType { 
		LIVE, 
		STAGING, 
		SANDBOX
	}
	
	private static String mUrlPrefix	= IS_DEV 
										? "https://sandbox.mobile.pay.zing.vn/zmpapi/" 
										: "https://mobile.pay.zing.vn/zmpapi/";
	
	public static void setUrlPrefix(HostType pHostType) {
		if (!IS_SWITCHABLE) {
			return;
		}
		
		switch (pHostType) {
			case LIVE:
				mUrlPrefix = "https://mobile.pay.zing.vn/zmpapi/";
				break;
				
			case STAGING:
				mUrlPrefix = "https://staging.mobile.pay.zing.vn/zmpapi/";
				break;
				
			case SANDBOX:
				mUrlPrefix = "https://sandbox.mobile.pay.zing.vn/zmpapi/";
				break;
				
			default:
				break;
		}
	}
	
	public static String getUrlPrefix() {
		return mUrlPrefix;
	}
	
	public static final String URL_GATEWAY_INFO 		= "gwinfo";
	public static final String URL_GET_STATUS 			= "getstatus";
	public static final String URL_GIAB_CREATE_ORDER 	= "createorder";
	public static final String URL_GIAB_VERIFY_RECEIPT 	= "verifyreceipt";
	public static final String URL_CARD_SUBMIT_CARD 	= "submitcard";
	public static final String URL_SMS_REG 				= "regsms";
	public static final String URL_CREDIT_REG 			= "creditcardcreateorder";
	public static final String URL_ATM_BANK_LIST 		= "banklist";
	public static final String URL_ATM_CREATE_ORDER 	= "atmcreateorder";
	public static final String URL_ATM_VERIFY_CARD 		= "atmverifycard";
	public static final String URL_ATM_VERIFY_OTP 		= "atmverifyotp";
	public static final String URL_ATM_SUBMIT_LOG 		= "sdkerrorreport";

	public static class ScreenSize {
		public static final String SMALL 	= "small";
		public static final String NORMAL 	= "normal";
		public static final String LARGE 	= "large";
		public static final String XLARGE 	= "xlarge";
	}

	public static class ScreenFormat {
		public static final String LONG 	= "long";
		public static final String NORMAL 	= "normal";
	}

	public static class ScreenDensity {
		public static final String LOW 		= "low";
		public static final String HIGH 	= "high";
		public static final String MEDIUM 	= "medium";
	}

	public static class BANK_PAYMENT_TYPE {
		public final static String API 		= "aapi";
		public final static String SML 		= "asml";
	}

	public static class TRACKING {
		public final static String ACTION_SUCCESS 		= "PAYMENT-SUCCESSFULLY";
		public final static String EXCEPTION			= "EXCEPTION";
	}

	public static final int RESULT_OK 		= 0x10;
	public static final int RESULT_EXIT 	= 0x12;
	public static final int RESULT_BACK 	= 0x13;

	public static final String FINISH_ACT_RETURN_MESSAGE 	= "returnMessage";
	public static final String FINISH_ACT_RETURN_PMC_NAME 	= "paymentChannel";
	public static final String FINISH_ACT_RETURN_TRANSID 	= "transID";

	public static final int MAX_INTERVAL_OF_ATM 					= 420000; // 7mins
	public static final int MAX_INTERVAL_OF_RETRY 					= 30000;
	public static final int SLEEPING_INTERVAL_OF_RETRY 				= 1000;
	public static final int SLEEPING_INTERVAL_OF_GIAB_VERIFY_RETRY 	= 300000;
	// @formatter:on
}
