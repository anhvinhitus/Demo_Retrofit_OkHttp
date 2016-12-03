package vn.com.vng.zalopay.data.ws.connection;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.UnresolvedAddressException;

import timber.log.Timber;

/**
 * Created by AnhHieu on 7/24/16.
 * TCP nonblocking socket
 */
class TCPClient implements SocketClient {
    private Listener mListener;

    private final Handler mConnectionHandler;

    private final Handler mEventHandler;

    private final SocketChannelConnection mConnection;

    TCPClient(String hostname, int port, Listener listener) {
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

    public void disconnect() {
        disposeConnection();
    }

    private void disposeConnection() {
        mEventHandler.post(mConnection::close);
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
        return mConnection.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return mConnection.isConnecting();
    }

    void postDisconnectedEvent(ConnectionErrorCode reason) {
        if (mListener == null) {
            return;
        }

        mEventHandler.post(() -> mListener.onDisconnected(reason, ""));
    }

    private void postWriteData(byte[] data) {
        mEventHandler.post(() -> mConnection.write(data));
    }

    void postReceivedDataEvent(byte[] data) {
        if (mListener == null) {
            return;
        }

        mEventHandler.post(() -> mListener.onMessage(data));
    }

    void postConnectedEvent() {
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
