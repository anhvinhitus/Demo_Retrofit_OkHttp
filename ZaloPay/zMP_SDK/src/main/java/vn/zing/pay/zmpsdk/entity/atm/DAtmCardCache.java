/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.atm.DAtmCache.java
 * Created date: Jan 22, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.atm;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DAtmCardCache extends DBaseEntity<DAtmCardCache> {
	public String bankCode;
	public String cardHolderName;
	public String cardNumber;
	public String cardMonth;
	public String cardYear;
}
