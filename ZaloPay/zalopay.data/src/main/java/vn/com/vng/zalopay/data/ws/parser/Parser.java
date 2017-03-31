package vn.com.vng.zalopay.data.ws.parser;

import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by AnhHieu on 6/14/16.
 */
public interface Parser {
    PushMessage parserMessage(byte[] data);
}
