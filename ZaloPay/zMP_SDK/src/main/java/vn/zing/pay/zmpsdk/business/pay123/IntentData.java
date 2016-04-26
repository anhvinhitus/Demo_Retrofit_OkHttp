/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.business.pay123.IntentData.java
 * Created date: Jan 28, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.business.pay123;

import vn.zing.pay.zmpsdk.entity.DBaseEntity;

/**
 * @author YenNLH
 *
 */
public class IntentData extends DBaseEntity<IntentData> {
    public String appName;
    public String account;
    public long amount;
    public String orderName;
    public String disp;
    public String signature;
}
