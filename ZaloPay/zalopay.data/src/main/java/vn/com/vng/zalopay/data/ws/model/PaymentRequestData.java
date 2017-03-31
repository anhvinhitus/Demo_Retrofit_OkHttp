package vn.com.vng.zalopay.data.ws.model;

import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by hieuvm on 3/8/17.
 */

public class PaymentRequestData extends PushMessage {
    public long requestid;
    public int resultcode;
    public String resultdata;
}