package vn.com.vng.zalopay.network;

/**
 * Created by huuhoa on 3/31/17.
 * Factory class to create SocketConnector
 */

public class SocketConnectorFactory {
    public static SocketConnector create(String hostname, int port, ConnectorListener listener) {
        return new SSLClientConnector(hostname, port, listener);
    }
}
