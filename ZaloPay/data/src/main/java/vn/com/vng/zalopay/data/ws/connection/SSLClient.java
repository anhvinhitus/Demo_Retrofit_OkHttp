package vn.com.vng.zalopay.data.ws.connection;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.UnresolvedAddressException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import timber.log.Timber;

/**
 * Created by huuhoa on 12/20/16.
 * Implement SSL socket client
 */

class SSLClient implements SocketClient {
    private final String mHostname;
    private final int mPort;
    private final Handler mConnectionHandler;
    private final Handler mEventHandler;
    private Listener mListener;
    private Socket mSslSocket = null;

    SSLClient(String hostname, int port, Listener listener) {
        mHostname = hostname;
        mPort = port;
        mListener = listener;

        // event handler thread - all socket events are processed in that thread
        // such as: data received, socket connected, socket disconnected, socket error
        // Thread for handling commands, network events, read/write queue to network connection
        HandlerThread eventHandlerThread = new HandlerThread("socket-thread");

        // connection thread - create socket connection, sending data, ...
        // are all processed in this thread
        // Thread for handling network connection
        // Modification to mConnection will only be happened in this thread
        HandlerThread connectionThread = new HandlerThread("connection-thread");

        eventHandlerThread.start();
        connectionThread.start();

        mEventHandler = new Handler(eventHandlerThread.getLooper());
        mConnectionHandler = new Handler(connectionThread.getLooper());
    }

    @Override
    public void connect() {
        Timber.d("Request to make connection");
        mConnectionHandler.post(() -> {
            if (mSslSocket != null && mSslSocket.isConnected()) {
                Timber.d("[CONNECTION] Skip create new connection");
                return;
            }
            Timber.d("Starting new connection");
            try {
                if (mSslSocket != null) {
                    mSslSocket.close();
                }

                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(null, null, new java.security.SecureRandom());
                SSLSocketFactory sf = context.getSocketFactory();
                mSslSocket = sf.createSocket(mHostname, mPort);

                if (isConnected()) {
                    postConnectedEvent();
                }

                while (true) {
                    // run until socket is closed
                    byte[] data = receiveBuffer();
                    if (data == null) {
                        continue;
                    }

                    postReceivedDataEvent(data);
                }
            } catch (UnresolvedAddressException e) {
                Timber.w(e, "Unresolved address exception");
                postErrorEvent(e);
            } catch (java.net.ConnectException e) {
                // reduce fatal level
                Timber.i(e, "ConnectException");
                postErrorEvent(e);
            } catch (ClosedChannelException e) {
                Timber.w(e, "Exception: Channel is closed");
                postErrorEvent(e);
            } catch (SocketException e) {
                Timber.e(e, "SocketException");
                postErrorEvent(e);
            } catch (AssertionError e) {
                // Caught assertion error with message: EBADF (Bad file number)
                Timber.w(e, "Assertion error");
                postErrorEvent(e);
            } catch (IOException e) {
                Timber.e(e, "IOException");
                postErrorEvent(e);
            } catch (Exception e) {
                Timber.e(e, "Exception");
                postErrorEvent(e);
            } catch (Throwable e) {
                Timber.e(e, "Throwable exception!!!");
                postErrorEvent(e);
            } finally {
                Timber.d("Stopping the connection.");
                disposeConnection();
            }
        });
    }

    @Override
    public void disconnect() {
        disposeConnection();
    }

    @Override
    public void send(byte[] data) {
        mEventHandler.post(() -> this.writeData(data));
    }

    @Override
    public boolean isConnected() {
        return mSslSocket != null && mSslSocket.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return mSslSocket != null && mSslSocket.isConnected();
    }

    private void disposeConnection() {
        mEventHandler.post(() -> {
            if (mSslSocket == null) {
                return;
            }

            try {
                mSslSocket.close();
            } catch (IOException e) {
                Timber.w("Exception while disconnect socket");
            }

            mSslSocket = null;

            if (mListener != null) {
                mListener.onDisconnected(ConnectionErrorCode.DISCONNECT_FINALIZE, "");
            }
        });
    }

    private byte[] receiveBuffer() throws IOException {
        int retByte;
        byte[] header = new byte[4];

        retByte = mSslSocket.getInputStream().read(header);
        if (retByte < 4) {
            return null;
        }

        int szBody = ByteBuffer.wrap(header).getInt();
        byte[] body = new byte[szBody];

        retByte = mSslSocket.getInputStream().read(body);
        if (retByte < szBody) {
            return null;
        }

        return body;
    }

    private void writeData(byte[] data) {
        try {

            if (mSslSocket == null) {
                throw new NullPointerException("sslSocket should not be NULL");
            }

            if (mSslSocket.isClosed()) {
                throw new ClosedChannelException();
            }

            if (!mSslSocket.isConnected()) {
                throw new ConnectException();
            }

            if (mSslSocket.isOutputShutdown()) {
                throw new ClosedChannelException();
            }

            try {
                mSslSocket.getOutputStream().write(data);
                mSslSocket.getOutputStream().flush();
            } catch (Throwable ex) {
                Timber.w(ex, "Exception while writing data to SSL socket");
                mSslSocket.close();
                postErrorEvent(ex);
            }
        } catch (Throwable e) {
            postErrorEvent(e);
        }
    }

    private void postReceivedDataEvent(byte[] data) {
        if (mListener == null) {
            return;
        }

        mEventHandler.post(() -> mListener.onMessage(data));
    }

    private void postConnectedEvent() {
        if (mListener == null) {
            return;
        }

        mEventHandler.post(() -> mListener.onConnected());
    }

    private void postErrorEvent(Throwable e) {
        if (mListener == null) {
            return;
        }

        mEventHandler.post(() -> mListener.onError(e));
    }
}
