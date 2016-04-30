/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.gatewayinfo.DGatewayInfo.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.gatewayinfo;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DGatewayInfo extends DBaseEntity<DGatewayInfo> {
	// @formatter:off
	public int 					returnCode 			= -1;
	public String 				returnMessage		= null;
	
	public boolean 				isUpdateAppInfo		= false;
	public String				appInfoCheckSum		= null;
	
	public DAppInfo				appInfo				= null;
	
	public boolean 				isUpdateResource	= false;
	public DSDKResource 		resource			= null;
	
	public long					expiredTime			= 600000;
	// @formatter:on
}
