/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.atm.webview.WebViewLogger.java
 * Created date: Jan 21, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business.atm.webview;

/**
 * @author YenNLH
 * 
 */
public class WebViewLogger {
	
	private String mBankCode = null;
	private StringBuilder mWebHistory = new StringBuilder();
	
	public WebViewLogger(String pBankCode) {
		mBankCode = pBankCode;
	}
	
	public void travel(String pUrl) {
		mWebHistory.append(System.currentTimeMillis());
		mWebHistory.append(':');
		mWebHistory.append(pUrl);
		mWebHistory.append("\r\n");
	}

	public String getBankCode() {
		return mBankCode;
	}
	
	public String getHistory() {
		return mWebHistory.toString();
	}
}
