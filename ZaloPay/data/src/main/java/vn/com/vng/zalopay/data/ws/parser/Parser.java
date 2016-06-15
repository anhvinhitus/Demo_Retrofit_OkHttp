package vn.com.vng.zalopay.data.ws.parser;

import com.google.protobuf.GeneratedMessage;

/**
 * Created by AnhHieu on 6/14/16.
 */
public interface Parser {
    GeneratedMessage parserMessage(byte[] data);
}
