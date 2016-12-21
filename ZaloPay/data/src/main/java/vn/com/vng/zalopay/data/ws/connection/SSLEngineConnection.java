package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import timber.log.Timber;

/**
 * Created by hieuvm on 12/20/16.
 */

public class SSLEngineConnection {

    public static final String PROTOCOL = "TLSv1.2";

    private final String address;
    private final int port;
    private final ConnectionListenable mListenable;

    private SSLEngine mEngine;

    private SocketChannel mChannel;

    private Selector mSelector;

    private Context mApplicationContext;
    private ConnectionState mConnectionState;

    public SSLEngineConnection(Context context, String address, int port, ConnectionListenable listenable) throws Exception {
        this.mApplicationContext = context;
        this.address = address;
        this.port = port;
        this.mListenable = listenable;
        mConnectionState = ConnectionState.NOT_CONNECTED;

        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(null, createTrustManager("zalopay@789", "zptrustStore"), new SecureRandom());
        mEngine = sslContext.createSSLEngine(address, port);
        mEngine.setUseClientMode(true);
    }

    private TrustManager[] createTrustManager(String password, String trustStore) throws Exception {

        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(mApplicationContext.getAssets().open(trustStore), password.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keystore);
        return tmf.getTrustManagers();
    }

    boolean startConnect() throws Exception {
        Timber.d("Start connecting");
        synchronized (this) {
            if (mConnectionState == ConnectionState.CONNECTED) {
                Timber.d("Connection is already made.");
                throw new AlreadyConnectedException();
            }

            if (mConnectionState == ConnectionState.CONNECTING) {
                Timber.d("Connection is initializing");
                throw new ConnectionPendingException();
            }

            mConnectionState = ConnectionState.CONNECTING;

            disconnect();

            //mSelector = Selector.open();
            mChannel = SocketChannel.open();
            mChannel.configureBlocking(false);
            //mChannel.socket().setKeepAlive(true);
            //mChannel.socket().setSoTimeout(10000);
            //mChannel.socket().setTcpNoDelay(true);
            //mChannel.register(mSelector, SelectionKey.OP_CONNECT);
            mChannel.connect(new InetSocketAddress(address, port));
            mEngine.beginHandshake();
            return doHandshake(mChannel, mEngine);
        }
    }

    private boolean doHandshake(SocketChannel socketChannel, SSLEngine engine) throws IOException {
        return false;
    }

    private void read(SocketChannel socketChannel, SSLEngine engine) throws Exception {

    }

    private void write(SocketChannel socketChannel, SSLEngine engine, byte[] message) throws Exception {

    }

    private void disconnect() throws Exception {
        if (mSelector != null) {
            mSelector.close();
        }
        if (mChannel != null) {
            mChannel.socket().close();
            mChannel.close();
        }
    }
}
