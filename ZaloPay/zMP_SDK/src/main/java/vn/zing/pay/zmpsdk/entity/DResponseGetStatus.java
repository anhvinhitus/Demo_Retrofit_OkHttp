/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.DResponseGetStatuc.java
 * Created date: Jan 11, 2016
 * Owner: SEGFRY
 */
package vn.zing.pay.zmpsdk.entity;

/**
 * @author YenNLH
 * 
 */
public class DResponseGetStatus extends DResponse {
	public boolean isProcessing = false;
	public long ppValue;
}
