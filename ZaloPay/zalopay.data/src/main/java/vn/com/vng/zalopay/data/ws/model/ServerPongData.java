package vn.com.vng.zalopay.data.ws.model;


import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by huuhoa on 8/17/16.
 * Contains ping data that client sent
 */

public class ServerPongData extends PushMessage {
    public long clientData;
}
