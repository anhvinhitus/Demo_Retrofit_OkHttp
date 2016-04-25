/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.ZPPaymentResult.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity;

import com.google.gson.Gson;

import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.helper.google.IabResult;
import vn.zing.pay.zmpsdk.helper.google.Purchase;

/**
 * Represent all information need for the result
 * 
 * @author YenNLH
 *
 */
public class ZPPaymentResult extends DBaseEntity<ZPPaymentResult> {
	public ZPPaymentInfo paymentInfo;
	public EPaymentStatus paymentStatus;
	public String channelID;
	public String channelDetail;
	public Purchase purchase;
	public IabResult iabResult;

	public ZPPaymentResult(ZPPaymentInfo pPaymentInfo, EPaymentStatus pPaymentStatus) {
		this.paymentInfo = pPaymentInfo;
		this.paymentStatus = pPaymentStatus;
	}
	
	/**
	 * Parse the json string input to an {@link ZPPaymentResult} Object
	 * representation of the same
	 * 
	 * @param pJson
	 *            The JSON string input
	 * 
	 * @return {@link ZPPaymentResult} Object
	 */
	public static ZPPaymentResult fromJson(String pJson) {
		return (new Gson()).fromJson(pJson, ZPPaymentResult.class);
	}
}
