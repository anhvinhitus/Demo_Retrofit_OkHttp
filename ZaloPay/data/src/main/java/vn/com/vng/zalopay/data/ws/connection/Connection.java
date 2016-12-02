package vn.com.vng.zalopay.data.ws.connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.model.Event;

/**
 * Created by AnhHieu on 6/14/16.
 * Socket connection interface
 */
public abstract class Connection {

    protected State mState = State.Disconnected;

    public enum State {
        Disconnected,
        Connecting,
        Connected
    }

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    public static final int TYPE_FIELD_LENGTH = 1;
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int HEADER_LENGTH = TYPE_FIELD_LENGTH + LENGTH_FIELD_LENGTH;

    List<OnReceiverMessageListener> listCallBack;

    protected String mHost;
    protected int mPort;

    public Connection(String hostname, int port) {
        if (hostname == null || port < 0 || port > 65535) {
            throw new IllegalArgumentException("host=" + hostname + ", port=" + port);
        }
        this.mHost = hostname;
        this.mPort = port;
    }

    public abstract void connect();

    public abstract void ping();

    public abstract void disconnect();

    public abstract boolean send(int msgType, String data);

    public abstract boolean send(int msgType, byte[] data);

//    public abstract boolean send(int msgType, AbstractMessage msgData);

    public boolean isConnected() {
        return mState == State.Connected;
    }

    public boolean isConnecting() {
        return mState == State.Connecting;
    }

    public State getState() {
        return mState;
    }

    public void addReceiverListener(OnReceiverMessageListener listener) {
        if (listCallBack == null) {
            listCallBack = new ArrayList<>();
        }

        listCallBack.add(listener);
    }

    public void removeReceiverListener(OnReceiverMessageListener listener) {
        if (listCallBack != null) {
            listCallBack.remove(listener);
        }
    }

    public void clearReceiverListener() {
        if (listCallBack != null) {
            listCallBack.clear();
        }
    }

    Message postResult(Event message) {

        Message uiMsg = new Message();
        uiMsg.what = message.msgType;
        uiMsg.obj = message;

        if (messageHandler != null) {
            messageHandler.sendMessage(uiMsg);
        }
        return uiMsg;
    }

    private void onPostExecute(Event event) {
        try {
            if (listCallBack != null) {
                for (int i = listCallBack.size() - 1; i >= 0; i--) {
                    listCallBack.get(i).onReceiverEvent(event);
                }
            }
        } catch (Exception ex) {
            Timber.w(ex, "exception : ");
        }
    }

    public void cleanup() {
        messageHandler = null;
    }


    private Handler messageHandler = new MessageHandler(Looper.getMainLooper(), this);

    private static class MessageHandler extends Handler {
        private WeakReference<Connection> mConnection;

        MessageHandler(Looper looper, Connection connection) {
            super(looper);

            this.mConnection = new WeakReference<>(connection);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mConnection.get() != null) {
                mConnection.get().onPostExecute((Event) msg.obj);
            }
        }
    }
}
