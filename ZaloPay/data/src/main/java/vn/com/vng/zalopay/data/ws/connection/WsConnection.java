package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.google.protobuf.AbstractMessage;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

//import io.netty.channel.ConnectTimeoutException;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.Listener;
import vn.com.vng.zalopay.data.ws.SocketClient;
import vn.com.vng.zalopay.data.ws.TCPClient;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.ServerPongData;
import vn.com.vng.zalopay.data.ws.parser.Parser;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 6/14/16.
 * Network handlers for Socket connection
 */
public class WsConnection extends Connection {
    private static final int TIMER_HEARTBEAT = 60 * 1000;
    private static final int SERVER_TIMEOUT = 2 * 60 * 1000;
    private static final int TIMER_CONNECTION_CHECK = 10 * 1000;
    private String gcmToken;

    private final Context context;

    private int numRetry;

    private final Parser parser;
    private final UserConfig userConfig;

    private final SocketClient mSocketClient;
    private TimerWrapper mHeartBeatKeeper = new TimerWrapper(new SendHeartBeatTask());
    private TimerWrapper mConnectionChecker = new TimerWrapper(new CheckConnectionTask());
    private long mServerResponse;

    // state machine for keeping track of network connection
    private int mConnectionState;
    private final Handler mConnectionHandler;

    /**
     * Next connection state.
     * If request to disconnect: set mNextConnectionState = DISCONNECT
     * If network connection is lost due to error: set mNextConnectionState = RETRY_CONNECT
     */
    private int mNextConnectionState;
    private static final int NEXTSTATE_RETRY_CONNECT = 1;
    private static final int NEXTSTATE_DISCONNECT = 2;

    public WsConnection(String host, int port, Context context, Parser parser, UserConfig config) {
        super(host, port);
        this.context = context;
        this.parser = parser;
        this.userConfig = config;
//        mSocketClient = new NettyClient(host, port, new ConnectionListener());
        mSocketClient = new TCPClient(host, port, new ConnectionListener());
        HandlerThread thread = new HandlerThread("wsconnection");
        thread.start();
        mConnectionHandler = new Handler(thread.getLooper());
    }

    public void setGCMToken(String token) {
        this.gcmToken = token;
    }

    @Override
    public void connect() {
        if (mConnectionHandler.getLooper() != Looper.myLooper()) {
            mConnectionHandler.post(() -> {
                Timber.d("Trigger new connection");
                mSocketClient.connect();
            });
        } else {
            Timber.d("Trigger new connection inside looper");
            mSocketClient.connect();
        }
    }

    @Override
    public void ping() {
        if (System.currentTimeMillis() - mServerResponse > SERVER_TIMEOUT) {
            Timber.d("Server is not responding. Try to reconnect");
            reconnect();
            return;
        }

        ZPMsgProtos.MessageConnectionInfo.Builder pingMessage = ZPMsgProtos.MessageConnectionInfo.newBuilder()
                .setUserid(getCurrentUserId())
                .setEmbeddata(System.currentTimeMillis());

        send(ZPMsgProtos.MessageType.PING_SERVER.getNumber(), pingMessage.build());
    }

    private void reconnect() {
        disconnect();

        mConnectionHandler.postDelayed(() -> {
            // this code will be executed after 1 seconds
            Timber.d("Begin to reconnect");
            mNextConnectionState = NEXTSTATE_RETRY_CONNECT;
            connect();
        }, 1000);
    }

    @Override
    public void disconnect() {
        Timber.d("disconnect");
        mNextConnectionState = NEXTSTATE_DISCONNECT;
        mSocketClient.disconnect();
        mHeartBeatKeeper.stop();
    }

    @Override
    public boolean isConnected() {
        return mSocketClient.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return mSocketClient.isConnecting();
    }

    @Override
    public boolean send(int msgType, String data) {
        return false;
    }

    @Override
    public boolean send(int msgType, AbstractMessage msgData) {
        return send(msgType, msgData.toByteArray());
    }

    @Override
    public boolean send(int msgType, byte[] data) {
        ByteBuffer bufTemp = ByteBuffer.allocate(HEADER_LENGTH + data.length);
        bufTemp.putInt(data.length + TYPE_FIELD_LENGTH);
        bufTemp.put((byte) msgType);
        bufTemp.put(data);

        mSocketClient.send(bufTemp.array());

        return true;
    }

    private boolean sendAuthentication(String token, long uid) {

        Timber.d("send authentication token %s uid %s gcmToken %s", token, uid, gcmToken);

        ZPMsgProtos.MessageLogin.Builder loginMsg = ZPMsgProtos.MessageLogin.newBuilder()
                .setToken(token)
                .setUsrid(uid)
                .setOstype(Enums.Platform.ANDROID.getId());

        if (!TextUtils.isEmpty(gcmToken)) {
            loginMsg.setDevicetoken(gcmToken);
        }

        return send(ZPMsgProtos.MessageType.AUTHEN_LOGIN.getNumber(), loginMsg.build());
    }

    private boolean sendAuthentication() {
        if (userConfig.hasCurrentUser()) {
            User user = userConfig.getCurrentUser();
            return sendAuthentication(user.accesstoken, Long.parseLong(user.uid));
        }
        return false;
    }

    private long getCurrentUserId() {
        long uid = -1;

        try {
            uid = Long.parseLong(userConfig.getCurrentUser().uid);
        } catch (Exception ex) {
            Timber.d("parse uid exception %s");
        }

        return uid;
    }

    private boolean sendFeedbackStatus(Event event) {
        long mtaid = event.getMtaid();
        long mtuid = event.getMtuid();
        long uid = getCurrentUserId();

        if (mtaid <= 0 && mtuid <= 0) {
            return true;
        }

        Timber.d("Send feedback status with mtaid %s mtuid %s uid %s", mtaid, mtuid, uid);

        ZPMsgProtos.StatusMessageClient.Builder statusMsg = ZPMsgProtos.StatusMessageClient.newBuilder()
                .setStatus(ZPMsgProtos.MessageStatus.RECEIVED.getNumber());

        if (mtaid > 0) {
            statusMsg.setMtaid(mtaid);
        }
        if (mtuid > 0) {
            statusMsg.setMtuid(mtuid);
        }
        if (uid > 0) {
            statusMsg.setUserid(uid);
        }

        return send(ZPMsgProtos.MessageType.FEEDBACK.getNumber(), statusMsg.build());
    }

    private class SendHeartBeatTask implements TimerListener {
        @Override
        public int period() {
            return TIMER_HEARTBEAT;
        }

        /**
         * Get amount of time in milliseconds before first execution.
         *
         * @return amount of time in milliseconds before first execution.
         */
        @Override
        public int delay() {
            return TIMER_HEARTBEAT;
        }

        /**
         * called when timer ticked
         */
        @Override
        public void onEvent() {
            Timber.d("Begin send heart beat");
            ping();
        }
    }

    private class CheckConnectionTask implements TimerListener {
        /**
         * Get amount of time in milliseconds between subsequent executions.
         *
         * @return amount of time in milliseconds between subsequent executions.
         */
        @Override
        public int period() {
            return TIMER_CONNECTION_CHECK;
        }

        /**
         * Get amount of time in milliseconds before first execution.
         *
         * @return amount of time in milliseconds before first execution.
         */
        @Override
        public int delay() {
            return 0;
        }

        /**
         * called when timer ticked
         */
        @Override
        public void onEvent() {
            Timber.d("Begin check connection");
            if (NetworkHelper.isNetworkAvailable(context)) {
                connect();
            }
        }
    }

    private class ServerTimeoutTask implements TimerListener {
        @Override
        public int period() {
            return SERVER_TIMEOUT;
        }

        /**
         * Get amount of time in milliseconds before first execution.
         *
         * @return amount of time in milliseconds before first execution.
         */
        @Override
        public int delay() {
            return SERVER_TIMEOUT;
        }

        /**
         * called when timer ticked
         */
        @Override
        public void onEvent() {
            Timber.d("Server is not response within agreed time. Should disconnect server and reconnect");
            mServerResponse = System.currentTimeMillis();
        }
    }


    private class ConnectionListener implements Listener {

        @Override
        public void onConnected() {
            Timber.d("onConnected");
            mState = State.Connected;
            mNextConnectionState = NEXTSTATE_RETRY_CONNECT;
            //    numRetry = 0;
            sendAuthentication();

            mConnectionChecker.stop();
            mHeartBeatKeeper.start();
            mServerResponse = System.currentTimeMillis();
        }

        @Override
        public void onMessage(byte[] data) {
            Timber.v("onReceived: %s bytes", data.length);
            Event message = parser.parserMessage(data);
            if (message == null) {
                return;
            }

            Timber.v("message.msgType %s", message.getMsgType());
            boolean needFeedback = true;

            if (message.getMsgType() == ZPMsgProtos.ServerMessageType.AUTHEN_LOGIN_RESULT.getNumber()) {
                numRetry = 0;
            } else if (message.getMsgType() == ZPMsgProtos.ServerMessageType.KICK_OUT_USER.getNumber()) {
                needFeedback = false;
                mNextConnectionState = NEXTSTATE_DISCONNECT;
                disconnect();
            } else if (message.getMsgType() == ZPMsgProtos.ServerMessageType.PONG_CLIENT.getNumber()) {
                needFeedback = false;
                mServerResponse = System.currentTimeMillis();
                Timber.v("Got pong from server. Time elapsed: %s", mServerResponse - ((ServerPongData)message).clientData);
            } else {
                postResult(message);
            }

            if (needFeedback) {
                sendFeedbackStatus(message);
            }
        }

        @Override
        public void onDisconnected(int code, String reason) {
            Timber.d("onDisconnected %s", code);
            mHeartBeatKeeper.stop();
            mState = Connection.State.Disconnected;

            mSocketClient.disconnect();

            Timber.d("Next expected network state: %s", mNextConnectionState);
            if (mNextConnectionState == NEXTSTATE_RETRY_CONNECT) {
                scheduleReconnect();
            }
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("onError %s", e);
            mState = Connection.State.Disconnected;

            if (e instanceof SocketTimeoutException) {
//            } else if (e instanceof ConnectTimeoutException) {
            } else if (e instanceof ConnectException) {
            } else if (e instanceof UnknownHostException) {
            }

            mConnectionChecker.start();
        }
    }

    private void scheduleReconnect() {
        if (!userConfig.hasCurrentUser()) {
            Timber.d("Don't have signed in user. Skip reconnect.");
            return;
        }

        if (!NetworkHelper.isNetworkAvailable(context)) {
            Timber.d("Don't have network connection. Skip reconnect. When network connection is available, there is a event that will trigger new connection");
            return;
        }

        numRetry++;
        long time = 2000 * (numRetry % 10) + (long)(1000 * Math.random());
        Timber.d("Try to reconnect after %s (milliseconds) at [%s]-th time", time, numRetry);
        mConnectionHandler.postDelayed(this::connect, time);
    }
}
