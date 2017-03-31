package vn.com.vng.zalopay.network;

import vn.com.vng.zalopay.network.ConnectionErrorCode;

/**
 * Created by hieuvm on 12/20/16.
 */

interface NioSocketClientListener {
    void onConnected();

    void onReceived(byte[] data);

    void onDisconnected(ConnectionErrorCode reason);
}
