/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.data.InstanceBase.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.data.base;

/**
 * @author YenNLH
 * 
 */
public abstract class SingletonBase {

	/**
	 * Register this singleton at the manager
	 */
	public SingletonBase() {
		SingletonLifeCircleManager.register(this.getClass());
	}
}
