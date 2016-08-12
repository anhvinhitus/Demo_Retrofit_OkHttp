package vn.com.vng.zalopay.data.ws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
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
    private final int REASON_FINALIZE = 1;
    private final int REASON_TRIGGER_DISCONNECT = 2;
    private final int REASON_READ_ERROR = 3;
    private final int REASON_WRITE_ERROR = 4;

    private final InetSocketAddress mListenAddress;
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

    interface ConnectionListenable {
        void onConnected();
        void onReceived(byte[] data);
        void onDisconnected(int reason);
    }

    SocketChannelConnection(String address, int port, ConnectionListenable listenable) {
        mListenable = listenable;

        mListenAddress = new InetSocketAddress(address, port);
        mConnectionState = ConnectionState.NOT_CONNECTED;
    }


    boolean startConnect() throws IOException {
        Timber.d("Start connecting");
        if (mConnectionState == ConnectionState.CONNECTED) {
            Timber.d("Connection is already made.");
            throw new AlreadyConnectedException();
        }

        mSelector = Selector.open();
        mChannel = SocketChannel.open();
        mChannel.configureBlocking(false);
        mChannel.register(mSelector, SelectionKey.OP_CONNECT);
        mChannel.connect(mListenAddress);
        mConnectionState = ConnectionState.CONNECTING;
        return true;
    }

    void run() {
        Timber.d("Waiting for data");
        try {
            while (true) {
                processChangeRequests();


                // wait for events
                mSelector.select();

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
                        this.handleConnect(key);
                        continue;
                    }

                    if (key.isReadable()) {
                        Timber.d("OP_READ is fired");
                        if (!this.read(key)) {
                            break;
                        }
                    }

                    if (key.isWritable()) {
                        Timber.d("OP_WRITE is fired");
                        this.handleWrite(key);
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            handleDisconnected(REASON_TRIGGER_DISCONNECT);
        }
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
                channel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
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

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.finishConnect()) {
            Timber.d("connection made");
            mConnectionState = ConnectionState.CONNECTED;
            mListenable.onConnected();
            channel.register(mSelector, SelectionKey.OP_READ);
        } else {
            Timber.d("connection is not ready");
        }
    }

    private boolean read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        mReadBuffer.rewind();
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

        byte[] data = new byte[numRead];
        System.arraycopy(mReadBuffer.array(), 0, data, 0, numRead);
        Timber.d("Got %s bytes", data.length);

        mListenable.onReceived(data);
        return true;
    }

    private void handleDisconnected(int reason) {
        try {
            if (mConnectionState != ConnectionState.CONNECTED &&
                    mConnectionState != ConnectionState.CONNECTING) {
                return;
            }

            mConnectionState = ConnectionState.DISCONNECTED;
            mChannel.close();
            mSelector.close();
        } catch (IOException e) {
            Timber.d(e, "Exception while disconnect connection");
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
