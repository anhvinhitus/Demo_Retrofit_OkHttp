package vn.com.vng.zalopay.network;

import java.io.IOException;

/**
 * Created by hieuvm on 3/14/17.
 * 
 */

public class WriteSocketException extends IOException {
    public WriteSocketException(Throwable cause) {
        super(cause);
    }
}