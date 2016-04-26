/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.gatewayinfo.DAppInfo.java
 * Created date: Dec 18, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.gatewayinfo;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 * 
 */
public class DAppEntity extends DBaseEntity<DAppEntity> {
	// @formatter:off
	public String appName 		= null;
	public long appID 			= Long.MIN_VALUE;
	public String appLogoUrl 	= null;
	public int status 			= -1;
	// @formatter:on
}
