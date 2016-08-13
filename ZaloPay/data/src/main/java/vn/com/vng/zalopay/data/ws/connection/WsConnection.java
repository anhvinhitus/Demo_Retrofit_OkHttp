package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.text.TextUtils;

import com.google.protobuf.AbstractMessage;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ConnectTimeoutException;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.Listener;
import vn.com.vng.zalopay.data.ws.SocketClient;
import vn.com.vng.zalopay.data.ws.TCPClient;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.parser.Parser;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 6/14/16.
 * Network handlers for Socket connection
 */
public class WsConnection extends Connection {

    private String gcmToken;

    private final Context context;

    private int numRetry;

    private final Parser parser;
    private final UserConfig userConfig;


    private Timer mTimer;
    private TimerTask timerTask;

    private SocketClient mSocketClient;
    private HeartBeatKeeper mHeartBeatKeeper = new HeartBeatKeeper();

    public WsConnection(String host, int port, Context context, Parser parser, UserConfig config) {
        super(host, port);
        this.context = context;
        this.parser = parser;
        this.userConfig = config;
//        mSocketClient = new NettyClient(host, port, new ConnectionListener());
        mSocketClient = new TCPClient(host, port, new ConnectionListener());
    }

    public void setGCMToken(String token) {
        this.gcmToken = token;
    }

    @Override
    public void connect() {
        mSocketClient.connect();
    }

    @Override
    public void ping() {
        byte[] pingData = new byte[1];
        pingData[0] = 1;

        send(ZPMsgProtos.MessageType.FEEDBACK_VALUE, pingData);
    }

    @Override
    public void disconnect() {
        Timber.d("disconnect");
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

    private boolean sendFeedbackStatus(Event event) {
        long mtaid = event.getMtaid();
        long mtuid = event.getMtuid();
        long uid = -1;

        try {
            uid = Long.parseLong(userConfig.getCurrentUser().uid);
        } catch (Exception ex) {
            Timber.d("parse uid exception %s");
        }

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


    private void startTimerCheckConnect() {
        stopTimerCheckConnect();

        mTimer = new Timer();
        timerTask = new CheckConnectionTask();

        mTimer.schedule(timerTask, 0, 5 * 60 * 1000);
    }

    private void stopTimerCheckConnect() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private class HeartBeatKeeper {
        private Timer mHeartBeatTimer;
        private TimerTask mSendDataTask;

        void start() {
            stop();

            mHeartBeatTimer = new Timer();
            mSendDataTask = new SendHeartBeatTask();

            // Schedule for sending heart beat every 20s
            mHeartBeatTimer.schedule(mSendDataTask, 20000, 20000);
        }

        void stop() {
            if (mSendDataTask != null) {
                mSendDataTask.cancel();
                mSendDataTask = null;
            }

            if (mHeartBeatTimer != null) {
                mHeartBeatTimer.cancel();
                mHeartBeatTimer.purge();
                mHeartBeatTimer = null;
            }
        }
    }

    private class SendHeartBeatTask extends TimerTask {
        @Override
        public void run() {
            Timber.d("Begin send heart beat");
            ping();
        }
    }

    private class CheckConnectionTask extends TimerTask {

        @Override
        public void run() {
            Timber.d("Begin check connection");
            if (NetworkHelper.isNetworkAvailable(context)) {
                connect();
            }
        }
    }

    private class ConnectionListener implements Listener {

        @Override
        public void onConnected() {
            Timber.d("onConnected");
            mState = State.Connected;
            //    numRetry = 0;
            sendAuthentication();

            stopTimerCheckConnect();

            mHeartBeatKeeper.start();
        }

        @Override
        public void onMessage(byte[] data) {
            Timber.d("onReceived");
            Event message = parser.parserMessage(data);
            if (message != null) {
                Timber.d("onReceived message.msgType %s", message.getMsgType());

                if (message.getMsgType() == MessageType.Response.AUTHEN_LOGIN_RESULT) {
                    numRetry = 0;
                } else if (message.getMsgType() == MessageType.Response.KICK_OUT) {
                    disconnect();
                } else {
                    postResult(message);
                }

                sendFeedbackStatus(message);
            }
        }

        @Override
        public void onDisconnected(int code, String reason) {
            Timber.d("onDisconnected %s", code);
            mHeartBeatKeeper.stop();
            mState = Connection.State.Disconnected;

            mSocketClient.disconnect();

            if (NetworkHelper.isNetworkAvailable(context)
                    && userConfig.hasCurrentUser()
                    && numRetry <= MAX_NUMBER_RETRY_CONNECT) {
                connect();
                numRetry++;
            }
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("onError %s", e);
            mState = Connection.State.Disconnected;

            if (e instanceof SocketTimeoutException) {
            } else if (e instanceof ConnectTimeoutException) {
            } else if (e instanceof ConnectException) {
            } else if (e instanceof UnknownHostException) {
            }

            startTimerCheckConnect();
        }
    }
}
