package vn.com.vng.zalopay.data.ws.parser;

import vn.com.vng.zalopay.data.ws.model.Event;

/**
 * Created by AnhHieu on 6/14/16.
 */
public interface Parser {
    Event parserMessage(byte[] data);
}
