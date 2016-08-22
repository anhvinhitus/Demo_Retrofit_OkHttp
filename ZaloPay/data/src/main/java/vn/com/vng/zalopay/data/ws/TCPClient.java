package vn.com.vng.zalopay.data.ws;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;

import timber.log.Timber;

/**
 * Created by AnhHieu on 7/24/16.
 */
public class TCPClient implements SocketClient {
    private Listener mListener;

    private Thread mThread;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private boolean mRun = false;

    private String mHost;
    private int mPort;

    SocketChannelConnection mConnection;

    public TCPClient(String hostname, int port, Listener listener) {
        mHost = hostname;
        mPort = port;
        mListener = listener;

        mHandlerThread = new HandlerThread("socket-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void connect() {
        Timber.d("Request to make connection");
        if (mThread != null && mThread.isAlive()) {
            Timber.d("Thread running the connection is still alive. Skip create new connection");
            return;
        }

        mRun = true;

        mThread = new Thread(() -> {
            try {
                mRun = true;
                mConnection = new SocketChannelConnection(mHost, mPort, new ConnectionListener());
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
                mRun = false;
                disposeConnection();
            }
        });

        Timber.d("Starting new connection");
        mThread.start();
    }

    public void disconnect() {
        mRun = false;
        disposeConnection();
    }

    private void disposeConnection() {
        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }
    }

    public void send(byte[] data) {
        if (isConnected()) {
            sendFrame(data);
        }
    }

    private void sendFrame(final byte[] frame) {
        if (frame == null || mConnection == null) {
            return;
        }

        Timber.d("QUEUE: send message");
        postWriteData(frame);
    }

    public boolean isRunning() {
        return mRun;
    }

    @Override
    public boolean isConnected() {
        return mConnection != null && mConnection.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return mConnection != null && mConnection.isConnecting();
    }

    /**
     * Created by huuhoa on 8/12/16.
     * Implementation for connection events
     */
    private class ConnectionListener implements SocketChannelConnection.ConnectionListenable {
        @Override
        public void onConnected() {
            postConnectedEvent();
        }

        @Override
        public void onReceived(byte[] data) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int messageLength = buffer.getInt();
            Timber.d("Message length: %d", messageLength);
            if (messageLength > 0) {
                byte[] dataBuffer = new byte[messageLength];
                buffer.get(dataBuffer);
                Timber.d("Read %s bytes as message body", messageLength);
                postReceivedDataEvent(dataBuffer);
//                mListener.onMessage(dataBuffer);
            } else {
                Timber.d("messageLength is negative!");
            }
        }

        @Override
        public void onDisconnected(int reason) {
            postDisconnectedEvent(reason);
        }
    }

    private void postDisconnectedEvent(int reason) {
        if (mHandler == null || mListener == null) {
            return;
        }

        mHandler.post(() -> mListener.onDisconnected(reason, ""));
    }

    private void postWriteData(byte[] data) {
        if (mHandler == null || mConnection == null) {
            return;
        }

        mHandler.post(() -> mConnection.write(data));
    }

    private void postReceivedDataEvent(byte[] data) {
        if (mHandler == null || mListener == null) {
            return;
        }

        mHandler.post(() -> mListener.onMessage(data));
    }

    private void postConnectedEvent() {
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
