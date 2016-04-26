/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.staticconfig.DDynamicSelectionView.java
 * Created date: Dec 29, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.staticconfig.page;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DDynamicSelectionViewItem extends DBaseEntity<DDynamicSelectionViewItem> {
	public String pmcID = null;
	public String pmcName = null;
	public String selectedImg = null;
	public String grayImg = null;
	public String tag = null;
}
