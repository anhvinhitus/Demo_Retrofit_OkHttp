package vn.com.vng.zalopay.data.ws;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.protobuf.AbstractMessage;

import java.nio.ByteBuffer;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.parser.Parser;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 7/24/16.
 */
public class SocketConnection extends Connection implements TCPClient.Listener {

    final TCPClient client;

    private String gcmToken;

    private final Context context;
    private final Parser parser;
    private final UserConfig userConfig;
    private int numRetry;

    public SocketConnection(String host, int port, Context context, Parser parser, UserConfig config) {
        super(host, port);
        this.context = context;
        this.parser = parser;
        this.userConfig = config;

        client = new TCPClient(host, port, this);
    }

    @Override
    public void connect() {
        client.connect();
    }

    @Override
    public void ping() {
    }

    @Override
    public void disconnect() {
        client.disconnect();
    }

    @Override
    public boolean send(int msgType, String data) {
        return false;
    }

    @Override
    public boolean send(int msgType, byte[] data) {
        if (isConnected()) {
            try {
                Timber.d("send message to server: type = " + msgType);
                ByteBuffer bufTemp = ByteBuffer.allocate(HEADER_LENGTH + data.length);
                bufTemp.putInt(data.length + TYPE_FIELD_LENGTH);
                bufTemp.put((byte) msgType);
                bufTemp.put(data);
                client.send(bufTemp.array());
                return true;
            } catch (Exception ex) {
                //send fail
                Timber.e(ex, "send message to server exception");
            }
        } else {
            //send fail
        }

        return false;
    }

    @Override
    public boolean send(int msgType, AbstractMessage msgData) {
        return send(msgType, msgData.toByteArray());
    }

    @Override
    public void onError(Exception e) {
        mState = Connection.State.Disconnected;
        Timber.e(e, "exception");
    }

    @Override
    public void onConnect() {
        mState = State.Connected;
        sendAuthentication();
    }

    @Override
    public void onMessage(String message) {
        Timber.d("onMessage: message %s", message);
    }

    @Override
    public void onMessage(byte[] data) {
        Event message = parser.parserMessage(data);
        if (message != null) {
            Timber.d("onReceived message.msgType %s", message.getMsgType());
            if (message.getMsgType() == MessageType.Response.AUTHEN_LOGIN_RESULT) {

            } else if (message.getMsgType() == MessageType.Response.KICK_OUT) {
                Timber.d("onReceived KICK_OUT");
                disconnect();
                return;
            } else {
                this.postResult(message);
            }
        }
    }

    @Override
    public void onDisconnect(int code, String reason) {
        mState = Connection.State.Disconnected;
        Timber.d("onDisconnect: code %s reason %s", code, reason);

        if (NetworkHelper.isNetworkAvailable(context) && userConfig.hasCurrentUser() && numRetry <= MAX_NUMBER_RETRY_CONNECT) {
            connect();
        }
        numRetry++;
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

    public boolean sendAuthentication() {
        if (userConfig.hasCurrentUser()) {
            User user = userConfig.getCurrentUser();
            return sendAuthentication(user.accesstoken, Long.parseLong(user.uid));
        }
        return false;
    }

    public void setGCMToken(String token) {
        gcmToken = token;
    }
}
