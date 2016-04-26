/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.gatewayinfo.DGatewayData.java
 * Created date: Dec 22, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.gatewayinfo;

import java.util.List;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DAppInfo extends DBaseEntity<DAppInfo> {
	public DAppEntity 					appEntity 			= null;
	public List<DPaymentChannel> 		pmcList 			= null;
	public List<DGroupPaymentChannel> 	pmcGroupList 		= null;
}
