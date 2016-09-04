package vn.com.vng.zalopay.data.ws.connection;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.net.SocketException;

import timber.log.Timber;

/**
 * Created by AnhHieu on 7/24/16.
 * TCP nonblocking socket
 */
class TCPClient implements SocketClient {
    private Listener mListener;

    /**
     * Thread for handling network connection
     * Modification to mConnection will only be happened in this thread
     */
    private HandlerThread mConnectionThread;
    private Handler mConnectionHandler;

    /**
     * Thread for handling commands, network events, read/write queue to network connection
     */
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private boolean mRun = false;

    private final SocketChannelConnection mConnection;

    public TCPClient(String hostname, int port, Listener listener) {
        mListener = listener;

        mHandlerThread = new HandlerThread("socket-thread");
        mConnectionThread = new HandlerThread("connection-thread");
        mHandlerThread.start();
        mConnectionThread.start();

        mHandler = new Handler(mHandlerThread.getLooper());
        mConnectionHandler = new Handler(mConnectionThread.getLooper());

        mConnection = new SocketChannelConnection(hostname, port, new ConnectionListener(this));
    }

    public void connect() {
        Timber.d("Request to make connection");
        mConnectionHandler.post(() -> {
            if (mConnection.isConnected() || mConnection.isConnecting()) {
                Timber.d("[CONNECTION] Skip create new connection");
                return;
            }
            Timber.d("Starting new connection");
            try {
                mConnection.startConnect();
                mConnection.run();
            } catch (SocketException e) {
                Timber.e(e, "SocketException");
                postErrorEvent(e);
            } catch (IOException e) {
                Timber.e(e, "IOException");
                postErrorEvent(e);
            } catch (Exception e) {
                Timber.e(e, "Exception");
                postErrorEvent(e);
            } finally {
                Timber.d("Stopping the connection.");
                disposeConnection();
            }
        });
    }

    public void disconnect() {
        mRun = false;
        disposeConnection();
    }

    private void disposeConnection() {
        mHandler.post(mConnection::close);
    }

    public void send(byte[] data) {
        if (isConnected()) {
            sendFrame(data);
        }
    }

    private void sendFrame(final byte[] frame) {
        if (frame == null) {
            return;
        }

        Timber.d("QUEUE: send message");
        postWriteData(frame);
    }

    @Override
    public boolean isConnected() {
        return mConnection != null && mConnection.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return mConnection != null && mConnection.isConnecting();
    }

    void postDisconnectedEvent(ConnectionErrorCode reason) {
        if (mHandler == null || mListener == null) {
            return;
        }

        mHandler.post(() -> mListener.onDisconnected(reason, ""));
    }

    private void postWriteData(byte[] data) {
        mHandler.post(() -> mConnection.write(data));
    }

    void postReceivedDataEvent(byte[] data) {
        if (mHandler == null || mListener == null) {
            return;
        }

        mHandler.post(() -> mListener.onMessage(data));
    }

    void postConnectedEvent() {
        if (mHandler == null || mListener == null) {
            return;
        }

        mHandler.post(() -> mListener.onConnected());
    }

    private void postErrorEvent(Exception e) {
        if (mHandler == null || mListener == null) {
            return;
        }

        mHandler.post(() -> mListener.onError(e));
    }
}
