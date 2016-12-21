package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ConnectionPendingException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import timber.log.Timber;


final class SSLSocketConnection {

    public static final String PROTOCOL = "TLSv1.2";

    boolean isConnected() {
        return skSSL != null && skSSL.isConnected();
    }

    boolean isConnecting() {
        return mConnectionState == ConnectionState.CONNECTING;
    }

    public void close() {
        handleDisconnected(ConnectionErrorCode.TRIGGER_DISCONNECT);
    }

    private final ConnectionListenable mListenable;
    private ConnectionState mConnectionState;
    private String mAddress;
    private int mPort;

    private Socket skSSL = null;
    private Context mApplicationContext;

    SSLSocketConnection(Context context, String address, int port, ConnectionListenable listenable) {
        mListenable = listenable;
        mAddress = address;
        mPort = port;
        mConnectionState = ConnectionState.NOT_CONNECTED;
        mApplicationContext = context;
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

            skSSL = createDefaultSSLSF(mAddress, mPort, "zalopay@789", "zptrustStore");
            Timber.d("Create socket success [%s]", skSSL);
            mConnectionState = ConnectionState.CONNECTED;
            mListenable.onConnected();
            return true;
        }
    }

    private Socket createDefaultSSLSF(String ip, int port, String password, String trustStore) {
        try {
            SSLContext context = SSLContext.getInstance(PROTOCOL);
            context.init(null, createTrustManager(password, trustStore), new java.security.SecureRandom());
            SSLSocketFactory sf = context.getSocketFactory();
            return sf.createSocket(ip, port);
        } catch (KeyStoreException e) {
            Timber.e(e, "KeyStore not found");
        } catch (Exception e) {
            Timber.e(e, "Create socket error");
        }

        return null;
    }

    private TrustManager[] createTrustManager(String password, String trustStore) throws Exception {

        String kstype = KeyStore.getDefaultType();
        Timber.d("KeyStore type [%s]", kstype); //BKS

        KeyStore keystore = KeyStore.getInstance("JKS");
        InputStream stream = mApplicationContext.getAssets().open(trustStore);
        keystore.load(stream, password.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keystore);

        if (stream != null) {
            stream.close();
        }
        return tmf.getTrustManagers();
    }

    void run() {
        try {
            while (skSSL != null && !skSSL.isClosed() && skSSL.isConnected()
                    && !skSSL.isInputShutdown() && !skSSL.isOutputShutdown()) {
                byte[] bytes = receiveBuffer();
                if (bytes == null) {
                    break;
                }

                mListenable.onReceived(bytes);
            }
        } catch (IOException ex) {
            //empty
        } catch (Exception e) {
            Timber.d(e, "error received");
        } finally {
            handleDisconnected(ConnectionErrorCode.DISCONNECT_FINALIZE);
        }
    }

    private void handleDisconnected(ConnectionErrorCode reason) {
        if (mConnectionState != ConnectionState.CONNECTED &&
                mConnectionState != ConnectionState.CONNECTING) {
            return;
        }

        try {
            mConnectionState = ConnectionState.DISCONNECTED;
            disconnect();
        } catch (Exception e) {
            Timber.w(e, "Exception while disconnect connection");
        } finally {
            mListenable.onDisconnected(reason);
        }
    }

    void write(byte[] frame) {
        if (frame == null || frame.length == 0) {
            return;
        }

        sendBuffer(frame);
    }

    private boolean sendBuffer(byte[] buffer) {
        if (skSSL == null) {
            return false;
        }

        if (skSSL.isClosed() || !skSSL.isConnected()) {
            return false;
        }

        if (skSSL.isOutputShutdown()) {
            return false;
        }

        try {
            skSSL.getOutputStream().write(buffer);
            skSSL.getOutputStream().flush();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] receiveBuffer() throws IOException {
        int retByte;
        byte[] header = new byte[4];

        retByte = skSSL.getInputStream().read(header);
        if (retByte < 4) {
            return null;
        }

        int szBody = ByteBuffer.wrap(header).getInt();
        byte[] body = new byte[szBody];

        retByte = skSSL.getInputStream().read(body);
        if (retByte < szBody) {
            return null;
        }
        return body;
    }

    private void disconnect() {
        if (skSSL == null) {
            return;
        }

        if (!skSSL.isClosed() || skSSL.isConnected()) {
            return;
        }

        try {
            if (!skSSL.isInputShutdown()) {
                skSSL.shutdownInput();
            }
            if (!skSSL.isOutputShutdown()) {
                skSSL.shutdownOutput();
            }
            skSSL.close();
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void finalize() throws Throwable {
        handleDisconnected(ConnectionErrorCode.DISCONNECT_FINALIZE);
        super.finalize();
    }
}
