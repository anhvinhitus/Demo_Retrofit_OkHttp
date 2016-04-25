/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.atm.DAtmScriptOutput.java
 * Created date: Jan 16, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.atm;

import android.text.TextUtils;
import vn.zing.pay.zmpsdk.entity.DBaseEntity;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DStaticViewGroup;

/**
 * @author YenNLH
 *
 */
public class DAtmScriptOutput extends DBaseEntity<DAtmScriptOutput> {
	public int eventID = 0;

	public String otpimg;
	public String otpimgsrc;
	
	public boolean shouldStop = false;
	public String message = null;
	public String info = null;
	
	public DStaticViewGroup staticView = null;
	public DDynamicViewGroup dynamicView = null;
	public String[] itemList = null;
	
	public boolean isError() {
		return (shouldStop == true && !TextUtils.isEmpty(message));
	}
}
