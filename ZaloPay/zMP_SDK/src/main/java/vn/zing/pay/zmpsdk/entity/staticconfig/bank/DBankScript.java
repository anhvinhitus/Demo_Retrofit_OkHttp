/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.staticconfig.bank.DBankScript.java
 * Created date: Jan 16, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.entity.staticconfig.bank;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DBankScript extends DBaseEntity<DBankScript> {
	public String url;
	public String autoJs;
	public String hitJs;
	public int eventID;
	public String pageCode;
}
