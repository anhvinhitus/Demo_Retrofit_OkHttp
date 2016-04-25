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
package vn.zing.pay.zmpsdk.entity.google;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;
import android.database.Cursor;

/**
 * @author YenNLH
 *
 */
public class DGoogleIabReceipt extends DBaseEntity<DGoogleIabReceipt> {
	// @formatter:off
	public String zmpTransID 		= "";
	public String appID 			= "";
	public String signature 		= "";
	public String receipt 			= "";
	public String payload 			= "";
	public int retryCount 			= 0;
	public long time 				= 0;
	// @formatter:on
	
	public DGoogleIabReceipt(Cursor cursor) {
		zmpTransID = cursor.getString(1);
		appID = cursor.getString(2);
		signature = cursor.getString(3);
		receipt = cursor.getString(4);
		payload = cursor.getString(5);
		retryCount = cursor.getInt(6);
		time = cursor.getLong(7);
	}
	
	public DGoogleIabReceipt(String pReceipt, String pZmpTransID, String pSignature, String pPayload, String pAppID) {
		zmpTransID = pZmpTransID;
		appID = pAppID;
		signature = pSignature;
		receipt = pReceipt;
		payload = pPayload;
		retryCount = 0;
		time = System.currentTimeMillis();
	}
}
