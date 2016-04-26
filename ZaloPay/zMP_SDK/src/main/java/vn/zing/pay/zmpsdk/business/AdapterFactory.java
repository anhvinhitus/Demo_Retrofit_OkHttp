/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed, 
 * or transmitted in any form or by any means, including photocopying, recording, 
 * or other electronic or mechanical methods, without the prior written permission 
 * of the publisher, except in the case of brief quotations embodied in critical reviews 
 * and certain other noncommercial uses permitted by copyright law.
 *
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.business.AdapterFactory.java
 * Created date: Dec 23, 2015
 * Owner: YenNLH
 */
package vn.zing.pay.zmpsdk.business;

import vn.zing.pay.zmpsdk.business.atm.AdapterATM;
import vn.zing.pay.zmpsdk.business.card.AdapterCard;
import vn.zing.pay.zmpsdk.business.creditcard.AdapterCreditCard;
import vn.zing.pay.zmpsdk.business.inappbilling.AdapterGoogleInappBilling;
import vn.zing.pay.zmpsdk.business.pay123.Adapter123Pay;
import vn.zing.pay.zmpsdk.business.sms.AdapterSMS;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.PaymentChannelActivity;

/**
 * @author YenNLH
 * 
 */
public class AdapterFactory {

	public static AdapterBase produce(PaymentChannelActivity owner) {
		int channel = owner.getIntent().getIntExtra(
				GlobalData.getStringResource(Resource.string.zingpaysdk_intent_key_channel), 0);

		AdapterBase adapter = null;
		if (channel == Resource.getID(Resource.id.zpsdk_google_wallet_ctl)) {
			Log.i("Zmp", "AdapterFactory.produce adapter=AdapterGoogleInappBilling");
			adapter = new AdapterGoogleInappBilling(owner);
		} else if (channel == Resource.getID(Resource.id.zpsdk_atm_ctl)) {
			Log.i("Zmp", "AdapterFactory.produce adapter=AdapterATM");
			adapter = new AdapterATM(owner);
		} else if (channel == Resource.getID(Resource.id.zpsdk_merge_card_ctl) || channel == Resource.getID(Resource.id.zpsdk_mobile_card_ctl)
				|| channel == Resource.getID(Resource.id.zpsdk_zingcard_ctl)) {
			Log.i("Zmp", "AdapterFactory.produce adapter=AdapterCard");
			adapter = new AdapterCard(owner);
		} else if (channel == Resource.getID(Resource.id.zpsdk_sms_ctl)) {
			Log.i("Zmp", "AdapterFactory.produce adapter=AdapterSMS");
			adapter = new AdapterSMS(owner);
		} else if (channel == Resource.getID(Resource.id.zpsdk_credit_ctl)) {
			Log.i("Zmp", "AdapterFactory.produce adapter=AdapterCreditCard");
			adapter = new AdapterCreditCard(owner);
		} else if (channel == Resource.getID(Resource.id.zpsdk_123pay_ctl)) {
			Log.i("Zmp", "AdapterFactory.produce adapter=Adapter123Pay");
			adapter = new Adapter123Pay(owner);
		} else {
			Log.i("Zmp", "AdapterFactory.produce adapter=null");
		}

		return adapter;
	}
}
