/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.utils.StringUtil.java
 * Created date: Dec 21, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author YenNLH
 *
 */
public class StringUtil {

	public static String longToStringNoDecimal(long pLongNumber) {
		return NumberFormat.getNumberInstance(Locale.US).format(pLongNumber);
    }
}
