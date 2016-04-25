/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.ZPPaymentListener.java
 * Created date: Dec 11, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.listener;

import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;

/**
 * This listener will be used to communicate between SDK and user application.
 * 
 * @author YenNLH
 * 
 */
public interface ZPPaymentListener {

	/**
	 * This method will be invoke when user completes the payment
	 * 
	 * @param pPaymentResult
	 *            The entity held all the information about this transaction
	 */
	public void onComplete(ZPPaymentResult pPaymentResult);

	/**
	 * This method will be invoke when user cancels the payment
	 */
	public void onCancel();

	public void onSMSCallBack(String appTransID);
}
