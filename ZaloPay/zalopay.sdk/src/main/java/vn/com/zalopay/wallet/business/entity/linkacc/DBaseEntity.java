/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed,
 * or transmitted in any form or by any means, including photocopying, recording,
 * or other electronic or mechanical methods, without the prior written permission
 * of the publisher, except in the case of brief quotations embodied in critical reviews
 * and certain other noncommercial uses permitted by copyright law.
 * <p>
 * Project: ZingPaySDK
 * File: vn.zing.pay.zmpsdk.entity.ZPPaymentEntity.java
 * Created date: Dec 15, 2015
 * Owner: YenNLH
 */
package vn.com.zalopay.wallet.business.entity.linkacc;

import com.google.gson.Gson;

/**
 * Super class for all entity in this SDK
 *
 * @author YenNLH
 */
public abstract class DBaseEntity<T> {

    /**
     * Parse the json string input to an {@link T} Object representation of the
     * same
     *
     * @param pJson The JSON string input
     * @return {@link T} Object
     */
    @SuppressWarnings("unchecked")
    public T fromJsonString(String pJson) {
        return (T) (new Gson()).fromJson(pJson, this.getClass());
    }

    /**
     * Auto-generate the JSON string for this instance
     *
     * @return JSON String
     */
    public String toJsonString() {
        return (new Gson()).toJson(this);
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
