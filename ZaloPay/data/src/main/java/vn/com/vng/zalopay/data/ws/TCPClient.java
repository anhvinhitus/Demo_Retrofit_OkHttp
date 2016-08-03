package vn.com.vng.zalopay.data.ws;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import timber.log.Timber;

/**
 * Created by AnhHieu on 7/24/16.
 */
public class TCPClient {

    private Socket mSocket;
    private Uri mURI;
    private Listener mListener;

    private Thread mThread;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private boolean mRun = false;

    private final Object mSendLock = new Object();

    private String incomingMessage;

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
        if (mThread != null && mThread.isAlive()) {
            return;
        }

        mRun = true;

        mThread = new Thread(() -> {
            try {
                mRun = true;


                mSocket = new Socket();
                mSocket.setKeepAlive(true);
                mSocket.setTcpNoDelay(true);
                mSocket.setSoTimeout(5000);
                mSocket.connect(new InetSocketAddress(mHost, mPort));

                byte[] buffer = new byte[1024 * 2];

                int bytesRead;

                DataInputStream input = new DataInputStream(mSocket.getInputStream());
                while (mRun) {
                    input.read(buffer);
                    mListener.onMessage(buffer);
                }

                mListener.onConnect();
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
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }
                } catch (IOException ex) {
                    Timber.e(ex, "IOException");
                    mListener.onError(ex);
                }
            }

        });

        mThread.start();
    }

    public void disconnect() {
        mRun = false;
        if (mSocket != null) {
            mHandler.post(() -> {
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException ex) {
                    Timber.d("Error while disconnecting", ex);
                    mListener.onError(ex);
                }
            });
        }
    }

    public void send(byte[] data) {
        sendFrame(data);
    }

    void sendFrame(final byte[] frame) {
        mHandler.post(() -> {
            try {
                synchronized (mSendLock) {
                    if (mSocket == null) {
                        throw new IllegalStateException("Socket not connected");
                    }
                    OutputStream outputStream = mSocket.getOutputStream();
                    outputStream.write(frame);
                    outputStream.flush();
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

    public interface Listener {
        void onConnect();

        void onMessage(String message);

        void onMessage(byte[] data);

        void onDisconnect(int code, String reason);

        void onError(Exception error);
    }
}
