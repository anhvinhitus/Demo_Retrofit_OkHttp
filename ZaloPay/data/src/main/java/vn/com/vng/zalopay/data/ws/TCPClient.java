package vn.com.vng.zalopay.data.ws;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
                mListener.onError(e);
            } catch (IOException e) {
                Timber.e(e, "IOException");
                mListener.onError(e);
            } catch (Exception e) {
                Timber.e(e, "Exception");
                mListener.onError(e);
            } finally {
                Timber.d("Stopping the connection.");
                mRun = false;
                if (mConnection != null) {
                    mConnection.close();
                    mConnection = null;
                }
            }
        });

        Timber.d("Starting new connection");
        mThread.start();
    }

    public void disconnect() {
        mRun = false;
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
        mConnection.write(frame);
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
            mListener.onConnected();
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
                mListener.onMessage(dataBuffer);
            } else {
                Timber.d("messageLength is negative!");
            }
        }

        @Override
        public void onDisconnected(int reason) {
            mListener.onDisconnected(reason, "");
        }
    }
}
