/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZMP_SDK
 * File: vn.zing.pay.zmpsdk.analysis.IGATracker.java
 * Created date: Mar 5, 2016
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.analysis;

/**
 * @author YenNLH
 * 
 */
public interface IGATracker {
	public void initDefaultUncaughtExceptionHandler();

	public void trackScreen(String pScreenName, boolean pIsNewSession);

	public void trackEvent(String pCategory, String pAction, String pLabel, long pValue);

	public void trackPaymentCompleted(String pChannelName, String pZmpTransID);
	
	public void trackSmsCallbackCompleted(String pAppID, String pMno, String pZmpTransID, long pPPValue);
}
