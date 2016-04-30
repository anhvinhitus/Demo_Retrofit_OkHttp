/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.gatewayinfo.DPaymentChannel.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.gatewayinfo;

import com.google.gson.Gson;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannelStatus;

/**
 * @author YenNLH
 * 
 */
public class DGroupPaymentChannel extends DBaseEntity<DGroupPaymentChannel> {
	// @formatter:off
	public int groupID						= 0;
	public String groupName					= null;
	public EPaymentChannelStatus status		= EPaymentChannelStatus.DISABLE;
	public long minPPValue 					= -1;
	public long maxPPValue 					= -1;
	public double discount					= -1;
	// @formatter:on

	public DGroupPaymentChannel fromJsonString(String pJson) {
		if (pJson == null)
			return new DGroupPaymentChannel();

		return (new Gson()).fromJson(pJson, this.getClass());
	}
	
	public boolean isPromoted() {
		return discount > -1;
	}

	public boolean isEnable() {
		return status == EPaymentChannelStatus.ENABLE;
	}

	public boolean isAmountSupport(long pAmount) {
		if (pAmount <= 0) {
			return false;
		}

		if (minPPValue == -1 && maxPPValue == -1)
			return true;
		if (minPPValue == -1 && pAmount <= maxPPValue)
			return true;
		if (maxPPValue == -1 && pAmount >= minPPValue)
			return true;
		if (pAmount >= minPPValue && pAmount <= maxPPValue)
			return true;

		return false;
	}
}
