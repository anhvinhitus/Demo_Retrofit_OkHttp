/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.atm.DAtmScriptInput.java
 * Created date: Jan 16, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.atm;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DAtmScriptInput extends DBaseEntity<DAtmScriptInput> {
	public boolean isAjax = false;
	
	public String cardHolderName;
	public String cardNumber;
	public String cardMonth;
	public String cardYear;
	public String cardPass;
	public String captcha;
	public String otp;

	public String username;
	public String password;
	
	public int selectedItemOrderNum = 1;
}
