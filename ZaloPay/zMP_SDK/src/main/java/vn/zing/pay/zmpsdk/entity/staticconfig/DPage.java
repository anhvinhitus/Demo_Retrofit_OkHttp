/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.config.DActivity.java
 * Created date: Dec 21, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.staticconfig;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DDynamicViewGroup;
import vn.zing.pay.zmpsdk.entity.staticconfig.page.DStaticViewGroup;

/**
 * @author YenNLH
 *
 */
public class DPage extends DBaseEntity<DPage> {
	public String pageName = null;
	public DStaticViewGroup staticView;
	public DDynamicViewGroup dynamicView;
}
