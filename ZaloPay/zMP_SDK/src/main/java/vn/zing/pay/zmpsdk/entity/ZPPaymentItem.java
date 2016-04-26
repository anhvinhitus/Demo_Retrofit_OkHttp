/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.ZPPaymentItem.java
 * Created date: Dec 11, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity;

import com.google.gson.Gson;

/**
 * This class will hold the detail information about an item which you want to make a purchase
 * 
 * @author YenNLH
 *
 */
public class ZPPaymentItem extends DBaseEntity<ZPPaymentItem> {
	public String itemID;
	public String itemName;
	public long itemPrice;
	public long itemQuantity;
	
	/**
	 * Parse the json string input to an {@link ZPPaymentItem} Object
	 * representation of the same
	 * 
	 * @param pJson
	 *            The JSON string input
	 * 
	 * @return {@link ZPPaymentItem} Object
	 */
	public static ZPPaymentItem fromJson(String pJson) {
		return (new Gson()).fromJson(pJson, ZPPaymentItem.class);
	}
}
