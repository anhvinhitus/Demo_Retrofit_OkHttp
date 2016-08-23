package vn.com.vng.zalopay.data.ws;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by huuhoa on 8/11/16.
 * Non-blocking socket communication
 */
class SocketChannelConnection {
    private static final int REASON_FINALIZE = 1;
    private static final int REASON_TRIGGER_DISCONNECT = 2;
    private static final int REASON_READ_ERROR = 3;
    private static final int REASON_WRITE_ERROR = 4;
    private static final int REASON_CONNECTION_ERROR = 5;

    private final ConnectionListenable mListenable;
    private final List<ByteBuffer> mWriteQueue = new LinkedList<>();
    private final ByteBuffer mReadBuffer = ByteBuffer.allocate(4096);
    // A list of ChangeRequest instances
    private final List<ChangeRequest> mChangeRequests = new LinkedList<>();

    boolean isConnected() {
        return mConnectionState == ConnectionState.CONNECTED;
    }

    boolean isConnecting() {
        return mConnectionState == ConnectionState.CONNECTING;
    }

    public void close() {
        handleDisconnected(REASON_TRIGGER_DISCONNECT);
    }

    private enum ConnectionState {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    private ConnectionState mConnectionState;
    private Selector mSelector;
    private SocketChannel mChannel;
    private String mAddress;
    private int mPort;

    interface ConnectionListenable {
        void onConnected();
        void onReceived(byte[] data);
        void onDisconnected(int reason);
    }

    SocketChannelConnection(String address, int port, ConnectionListenable listenable) {
        mListenable = listenable;

        mAddress = address;
        mPort = port;

        mConnectionState = ConnectionState.NOT_CONNECTED;
    }


    boolean startConnect() throws IOException {
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

            mSelector = Selector.open();
            mChannel = SocketChannel.open();
            mChannel.configureBlocking(false);
            mChannel.socket().setKeepAlive(true);
            mChannel.socket().setSoTimeout(10000);
            mChannel.socket().setTcpNoDelay(true);
            mChannel.register(mSelector, SelectionKey.OP_CONNECT);
            mChannel.connect(new InetSocketAddress(mAddress, mPort));
            return true;
        }
    }

    void run() {
        Timber.d("Waiting for data");
        try {
            while (!Thread.interrupted()) {
                processChangeRequests();

                // wait for events, timeout after 30 seconds
//                Timber.d("Begin select");
                int select = mSelector.select();
//                int select = mSelector.select(30000);
//                Timber.d("After select: %s", select);
//                if (select == 0) {
//                    Timber.d("Timeout after 30s - connection may be lost");
//                }
                if (select > 0) {
                    if (!processSelectedKeys()) {
                        break;
                    }
                }
            }
        } catch (ClosedSelectorException e) {
            // selector has been closed
        } catch (IOException e) {
            handleDisconnected(REASON_TRIGGER_DISCONNECT);
        } finally {
            handleDisconnected(REASON_FINALIZE);
        }
    }

    private boolean processSelectedKeys() throws IOException {
        // work on selected keys
        Iterator keys = mSelector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = (SelectionKey) keys.next();

            // this is necessary to prevent the same key from coming up
            // again the next time around.
            keys.remove();

            Timber.d("selector is fired");
            if (!key.isValid()) {
                continue;
            }

            if (key.isConnectable()) {
                Timber.d("OP_CONNECT is fired");
                if (!this.handleConnect(key)) {
                    return false;
                }
            } else if (key.isReadable()) {
                Timber.d("OP_READ is fired");
                if (!this.read(key)) {
                    return false;
                }
            } else if (key.isWritable()) {
                Timber.d("OP_WRITE is fired");
                this.handleWrite(key);
            }
        }

        return true;
    }

    private void processChangeRequests() {
        // Process any pending changes
        synchronized (mChangeRequests) {
            for (ChangeRequest change : mChangeRequests) {
                switch (change.type) {
                    case ChangeRequest.CHANGEOPS:
                        SelectionKey key = mChannel.keyFor(mSelector);
                        key.interestOps(change.ops);
                }
            }
            mChangeRequests.clear();
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        synchronized (mWriteQueue) {
            // Write until there's not more data ...
            while (!mWriteQueue.isEmpty()) {
                ByteBuffer buf = mWriteQueue.get(0);
                int byteWritten = channel.write(buf);
                Timber.d("Byte written: %s", byteWritten);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    Timber.d("Remaining %s byte to be written again", buf.remaining());
                    break;
                }
                mWriteQueue.remove(0);
            }

            if (mWriteQueue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private boolean handleConnect(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();

            // Finish the connection. If the connection operation failed
            // this will raise an IOException.
            if (channel.finishConnect()) {
                Timber.d("connection made");
                mConnectionState = ConnectionState.CONNECTED;
                mListenable.onConnected();
                key.interestOps(SelectionKey.OP_READ);
            } else {
                Timber.d("connection is not ready");
            }

            return true;
        } catch (IOException e) {
            Timber.d(e, "exception while handling connection");
            mConnectionState = ConnectionState.NOT_CONNECTED;

            // Cancel the channel's registration with our selector
            key.cancel();

            mListenable.onDisconnected(REASON_CONNECTION_ERROR);
            return false;
        }
    }

    private boolean read(SelectionKey key) throws IOException {
        // READ: get the channel
        SocketChannel channel = (SocketChannel) key.channel();

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        BufferedOutputStream outputStream = new BufferedOutputStream(outputData);
        byte[] buffer = new byte[mReadBuffer.capacity()];
        while (true) {
            // clear buffer for reading
            mReadBuffer.clear();
            int numRead = -1;
            numRead = channel.read(mReadBuffer);

            if (numRead < 0) {
                Socket socket = channel.socket();
                SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                Timber.d("Connection closed by client: %s", remoteAddr);

                key.cancel();
                handleDisconnected(REASON_READ_ERROR);
                return false;
            }

            mReadBuffer.flip();

            int remaining = mReadBuffer.remaining();
            mReadBuffer.get(buffer, 0, remaining);
            outputStream.write(buffer, 0, remaining);

            Timber.v("Read %s byte of data. Read buffer: %s %s", numRead, mReadBuffer.position(), mReadBuffer.limit());
            if (numRead < mReadBuffer.capacity()) {
                Timber.v("Finish reading data");
                break;
            }
        }

        outputStream.flush();
        byte[] data = outputData.toByteArray();
        Timber.d("Read %s bytes", data.length);

        outputStream.close();
        outputData.close();
        mListenable.onReceived(data);
        return true;
    }

    private void handleDisconnected(int reason) {
        if (mConnectionState != ConnectionState.CONNECTED &&
                mConnectionState != ConnectionState.CONNECTING) {
            return;
        }

        try {
            mConnectionState = ConnectionState.DISCONNECTED;
            mSelector.close();
            mChannel.socket().close();
            mChannel.close();
        } catch (IOException e) {
            Timber.w(e, "Exception while disconnect connection");
        } finally {
            mListenable.onDisconnected(reason);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        handleDisconnected(REASON_FINALIZE);
        super.finalize();
    }

    void write(byte[] frame) {
        if (frame == null || frame.length == 0) {
            Timber.d("Empty data. Skipping");
            return;
        }

        Timber.d("QUEUE: %s bytes to be sent", frame.length);
        synchronized (mChangeRequests) {
            // Indicate we want the interest ops set changed
            mChangeRequests.add(new ChangeRequest(ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (mWriteQueue) {
                ByteBuffer buffer = ByteBuffer.wrap(frame);
                mWriteQueue.add(buffer);
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        mSelector.wakeup();
    }

    private class ChangeRequest {
        static final int CHANGEOPS = 2;

        final int type;
        final int ops;

        ChangeRequest(int type, int ops) {
            this.type = type;
            this.ops = ops;
        }
    }
}
