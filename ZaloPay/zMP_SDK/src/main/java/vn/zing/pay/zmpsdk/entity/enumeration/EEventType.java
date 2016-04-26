/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.enumeration.EEventType.java
 * Created date: Dec 23, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.enumeration;

/**
 * @author YenNLH
 * 
 */
public enum EEventType {
	// @formatter:off
	ON_CLICK, 				// Click back or exit button
	ON_SELECT, 				// Select item on slection view
	ON_BACK, 				// Back to previous activity
	ON_ACTIVITY_RESULT, 	// Reach onActivityResult method of activity
	ON_RESUME,				// Resume activity
	ON_NEW_INTENT, 			// Reach newIntent method of activity
	ON_PAYMENT_COMPLETED, 	// Payment compeleted
	ON_SUBMIT_COMPLETED, 	// After user clicked okButton and submission completed
	
	ON_FAIL,
	ON_GET_BANK_LIST_COMPLETED, 
	ON_CREATE_ORDER_COMPLETED, 
	ON_REQUIRE_RENDER,
	ON_VERIFY_COMPLETED,
	
	///////// GOOGLE INAPP BILLING ONLY ////////
	ON_SEPTUP_FAIL,
	ON_CONSUMPTION,
	ON_PURCHASED,
	///////// GOOGLE INAPP BILLING ONLY ////////
	
	// @formatter:on
}
