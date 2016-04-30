/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.Transaction.java
 * Created date: Dec 24, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity;

import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import android.database.Cursor;

/**
 * @author YenNLH
 *
 */
public class Transaction extends DBaseEntity<Transaction> {
	// @formatter:off
	public String appTransID 		= "";
	public String channel 			= "";
	public long amount 				= 0;
	public long sdkTransID 			= 0;
	public String UDID 				= "";
	public int retryCount 			= 0;
	public EPaymentStatus status 	= EPaymentStatus.ZPC_TRANXSTATUS_PROCESSING;
	public long time 				= 0;
	// @formatter:on
	
	public Transaction(Cursor cursor) {
		appTransID = cursor.getString(1);
		sdkTransID = cursor.getLong(2);
		UDID = cursor.getString(3);
		amount = Long.parseLong(cursor.getString(4));
		retryCount = Integer.parseInt(cursor.getString(5));
		status = EPaymentStatus.fromInt(cursor.getInt(6));
		time = Long.parseLong(cursor.getString(7));
	}
}
