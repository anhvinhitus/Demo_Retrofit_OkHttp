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
public class DPaymentChannel extends DBaseEntity<DPaymentChannel> {
	// @formatter:off
	public int pmcID						= 0;
	public String pmcName					= null;
	public int groupID						= 0;
	public EPaymentChannelStatus status		= EPaymentChannelStatus.DISABLE;
	public long minPPValue 					= -1;
	public long maxPPValue 					= -1;
	public double discount					= -1;
	// @formatter:on

	public DPaymentChannel fromJsonString(String pJson) {
		if (pJson == null)
			return new DPaymentChannel();
		
		return (new Gson()).fromJson(pJson, this.getClass());
	}

	public boolean isEnable() {
		return status == EPaymentChannelStatus.ENABLE;
	}
}
