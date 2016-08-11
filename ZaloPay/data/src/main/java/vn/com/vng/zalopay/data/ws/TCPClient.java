package vn.com.vng.zalopay.data.ws;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteOrder;

import timber.log.Timber;

/**
 * Created by AnhHieu on 7/24/16.
 */
public class TCPClient implements SocketClient {

    private Socket mSocket;
    private Listener mListener;

    private Thread mThread;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private boolean mRun = false;

    private final Object mSendLock = new Object();

    private BufferedOutputStream mBufferedOutputStream;

    private String mHost;
    private int mPort;

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

                mSocket = new Socket();
                mSocket.setKeepAlive(true);
                mSocket.setTcpNoDelay(true);
                // mSocket.setSoTimeout(5000);
                mSocket.connect(new InetSocketAddress(mHost, mPort));

                mBufferedOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
                mListener.onConnected();

                byte[] buffer = new byte[1024];
                byte[] header = new byte[4];



                DataInputStream input = new DataInputStream(mSocket.getInputStream());
                while (mRun) {
                    int messageLength = input.readInt();
                    Timber.d("Message length: %d", messageLength);
                    if (messageLength > 0) {
                        buffer = new byte[messageLength];
                        int bytesRead = input.read(buffer);
                        Timber.d("Read %s bytes as message body", bytesRead);
                        if (bytesRead != -1) {
                            mListener.onMessage(buffer);
                        } else {
                            Timber.d("Failed to read message body. Disconnect!");
                            break;
                        }
                    } else {
                        Timber.d("messageLength is negative!");
                    }
                }

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
                try {
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                } catch (IOException ex) {
                    Timber.e(ex, "IOException");
                    mListener.onError(ex);
                }
            }

        });

        Timber.d("Starting new connection");
        mThread.start();
    }

    private static long extractLong(byte[] scanRecord, int start) {
        long longValue = scanRecord[start + 3] & 0xFF;
        longValue += (scanRecord[start + 2] & 0xFF) << 8;
        longValue += (scanRecord[start + 1] & 0xFF) << 16;
        longValue += (scanRecord[start] & 0xFF) << 24;
        return longValue;
    }

    public void disconnect() {
        mRun = false;

        mHandler.post(() -> {
            if (mBufferedOutputStream != null) {
                try {
                    mBufferedOutputStream.close();
                    mBufferedOutputStream = null;
                } catch (IOException e) {
                }
            }

            if (mSocket != null) {
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException ex) {
                    Timber.d("Error while disconnecting", ex);
                    mListener.onError(ex);
                }
            }

        });

    }

    public void send(byte[] data) {
        if (isConnected()) {
            sendFrame(data);
        }
    }

    void sendFrame(final byte[] frame) {
        mHandler.post(() -> {
            try {
                synchronized (mSendLock) {
                    if (mSocket == null) {
                        throw new IllegalStateException("Socket not connected");
                    }

                    Timber.d("send message");
                    mBufferedOutputStream.write(frame);
                    mBufferedOutputStream.flush();
                }
            } catch (IOException e) {
                Timber.e(e, "IOException");
                //Send fail
            } catch (Exception ex) {
                Timber.e(ex, "Exception");
                //Send fail
            }
        });
    }

    public boolean isRunning() {
        return mRun;
    }

    @Override
    public boolean isConnected() {
        if (mSocket == null) {
            return false;
        }

        return mSocket.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return false;
    }
}
