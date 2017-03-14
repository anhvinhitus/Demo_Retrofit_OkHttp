package vn.com.vng.zalopay.data.ws.connection;

import android.os.Handler;
import android.os.HandlerThread;
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

    State mState = State.Disconnected;

    public enum State {
        Disconnected,
        Connecting,
        Connected
    }

    static final int MESSAGE_POST_RESULT = 1;
    static final int MESSAGE_POSt_ERROR = 2;

    public static final int TYPE_FIELD_LENGTH = 1;
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int HEADER_LENGTH = TYPE_FIELD_LENGTH + LENGTH_FIELD_LENGTH;

    private final List<OnReceiverMessageListener> listCallBack = new ArrayList<>();

    Connection() {
        HandlerThread messageThread = new HandlerThread("message-thread");
        messageThread.start();
        messageHandler = new MessageHandler(messageThread.getLooper(), this);
    }

    public abstract void connect();

    public abstract void disconnect();

    public abstract boolean send(int msgType, byte[] data);

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
        if (listCallBack.contains(listener)) {
            return;
        }

        listCallBack.add(listener);
    }

    public void removeReceiverListener(OnReceiverMessageListener listener) {
        listCallBack.remove(listener);
    }

    public void clearReceiverListener() {
        listCallBack.clear();
    }

    Message postResult(Event message) {

        Message uiMsg = new Message();
        uiMsg.what = MESSAGE_POST_RESULT;
        uiMsg.obj = message;

        if (messageHandler != null) {
            messageHandler.sendMessage(uiMsg);
        }
        return uiMsg;
    }

    Message postError(Throwable throwable) {
        Message uiMsg = new Message();
        uiMsg.what = MESSAGE_POSt_ERROR;
        uiMsg.obj = throwable;

        if (messageHandler != null) {
            messageHandler.sendMessage(uiMsg);
        }
        return uiMsg;
    }


    private void onErrorExecute(Throwable t) {
        try {
            for (int i = listCallBack.size() - 1; i >= 0; i--) {
                listCallBack.get(i).onError(t);
            }
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    private void onPostExecute(Event event) {
        try {
            for (int i = listCallBack.size() - 1; i >= 0; i--) {
                listCallBack.get(i).onReceiverEvent(event);
            }
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    public void cleanup() {
        messageHandler.removeCallbacksAndMessages(null);
    }

    private final Handler messageHandler;

    private static class MessageHandler extends Handler {
        private WeakReference<Connection> mConnection;

        MessageHandler(Looper looper, Connection connection) {
            super(looper);

            this.mConnection = new WeakReference<>(connection);
        }

        @Override
        public void handleMessage(Message msg) {

            Connection connection = mConnection.get();
            if (connection == null) {
                return;
            }

            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    connection.onPostExecute((Event) msg.obj);
                    break;
                case MESSAGE_POSt_ERROR:
                    connection.onErrorExecute((Throwable) msg.obj);
                    break;
            }
        }
    }

    public boolean isAuthentication() {
        return false;
    }

    protected String gcmToken;

    public boolean send(NotificationApiMessage message) {
        return send(message.messageCode, message.messageContent);
    }

    public void setGCMToken(String token) {
        this.gcmToken = token;
    }
}
