/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.config.DAttributes.java
 * Created date: Dec 21, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.staticconfig.page;

import java.util.List;
import java.util.Map;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DDynamicViewGroup extends DBaseEntity<DDynamicViewGroup> {
	public DDynamicSelectionViewGroup SelectionView;
	public List<DDynamicEditText> EditText;
	public Map<String, Boolean> View;
}
