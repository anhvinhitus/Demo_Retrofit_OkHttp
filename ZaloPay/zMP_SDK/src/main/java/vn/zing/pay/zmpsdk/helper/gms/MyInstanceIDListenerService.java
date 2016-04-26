/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.helper.gms.MyInstanceIDListenerService.java
 * Created date: Mar 2, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.helper.gms;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * @author YenNLH
 * 
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

	@Override
	public void onTokenRefresh() {
		// Fetch updated Instance ID token and notify of changes
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
	}
}
