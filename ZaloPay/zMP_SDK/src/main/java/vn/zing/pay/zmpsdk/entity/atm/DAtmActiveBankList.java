/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.atm.DActiveBankList.java
 * Created date: Jan 14, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.atm;

import java.util.List;

import vn.zing.pay.zmpsdk.entity.DResponse;

/**
 * @author YenNLH
 *
 */
public class DAtmActiveBankList extends DResponse {
	public List<DAtmActiveBankItem> bankList = null;
}
