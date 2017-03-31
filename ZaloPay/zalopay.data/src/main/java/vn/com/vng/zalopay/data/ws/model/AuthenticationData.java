package vn.com.vng.zalopay.data.ws.model;

import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by AnhHieu on 6/16/16.
 */
public class AuthenticationData extends PushMessage {

    public long uid;
    public int result;
    public int code;

}
