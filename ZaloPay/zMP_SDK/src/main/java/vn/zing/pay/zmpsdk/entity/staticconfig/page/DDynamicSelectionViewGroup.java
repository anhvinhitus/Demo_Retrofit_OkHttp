/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.staticconfig.DDynamicSelectionViewGroup.java
 * Created date: Jan 4, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.staticconfig.page;

import java.util.List;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DDynamicSelectionViewGroup extends DBaseEntity<DDynamicSelectionViewGroup> {
	public int breakLine = 0;
	public boolean isAutoSelect = true;
	public boolean isFilterPmc = true;
	public List<DDynamicSelectionViewItem> items = null;
	
	public boolean isDefaultBreakLine() {
		return breakLine == 0;
	}
}
