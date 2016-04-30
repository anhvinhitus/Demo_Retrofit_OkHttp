/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.google.DPayload.java
 * Created date: Dec 31, 2015
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity.google;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class DPayload extends DBaseEntity<DPayload> {
	public String zmpTransID;
	public long amount;
	public String orgAmount;
	public String amountMicro;
	public String currency;
}
