package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.text.TextUtils;

import com.google.protobuf.AbstractMessage;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.parser.Parser;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class WsConnection extends Connection implements ConnectionListener {


    private NioEventLoopGroup group;
    private Channel mChannel;
    private ChannelFuture channelFuture;

    private String gcmToken;

    private final Context context;

    private int numRetry;


    private final Parser parser;
    private final UserConfig userConfig;

    private boolean isAuthenticated;

    public WsConnection(String host, int port, Context context, Parser parser, UserConfig config) {
        super(host, port);
        this.context = context;
        this.parser = parser;
        this.userConfig = config;
    }

    public void setGCMToken(String token) {
        this.gcmToken = token;
    }

    @Override
    public void connect() {
        if (mChannel != null && mChannel.isActive()) {
            return;
        }

        Timber.i("Begin connecting");
        new Thread() {
            @Override
            public void run() {
                group = new NioEventLoopGroup();
                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group);
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.handler(new ChannelFactory(context, WsConnection.this));
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.option(ChannelOption.TCP_NODELAY, true);
                    bootstrap.option(ChannelOption.SO_TIMEOUT, 5000);
                    channelFuture = bootstrap.connect(new InetSocketAddress(mHost, mPort));
                    mChannel = channelFuture.sync().channel();
                    mState = Connection.State.Connecting;
                } catch (InterruptedException e) {
                    Timber.e(e, "InterruptedException");
                    mState = Connection.State.Disconnected;
                } catch (Exception e) {
                    Timber.e(e, "Connect ws Exception");
                    mState = Connection.State.Disconnected;
                }
            }
        }.start();

    }

    @Override
    public void ping() {

    }

    @Override
    public void disconnect() {
        Timber.d("disconnect");

        if (mChannel != null && mChannel.isOpen()) {
            mChannel.close();
        }

        if (group != null) {
            group.shutdownGracefully();
        }
        mState = State.Disconnected;
    }

    @Override
    public boolean isConnected() {
        if (mChannel != null) {
            return mChannel.isActive();
        }
        return false;
    }

    @Override
    public boolean isConnecting() {
        if (mChannel != null) {
            return mChannel.isOpen();
        }
        return false;
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

        if (isConnected()) {
            try {
                Timber.d("send message to server: type = " + msgType);
                ByteBuffer bufTemp = ByteBuffer.allocate(HEADER_LENGTH + data.length);
                bufTemp.putInt(data.length + TYPE_FIELD_LENGTH);
                bufTemp.put((byte) msgType);
                bufTemp.put(data);
                mChannel.writeAndFlush(bufTemp.array());
                return true;
            } catch (Exception ex) {
            }
        }

        return false;
    }


    @Override
    public void onConnected() {
        Timber.d("onConnected");
        mState = State.Connected;
        //    numRetry = 0;
        sendAuthentication();
    }

    @Override
    public void onReceived(byte[] data) {
        Timber.d("onReceived");
        Event message = parser.parserMessage(data);
        if (message != null) {
            Timber.d("onReceived message.msgType %s", message.getMsgType());
            if (message.getMsgType() == MessageType.Response.AUTHEN_LOGIN_RESULT) {
                numRetry = 0;
            } else if (message.getMsgType() == MessageType.Response.KICK_OUT) {
                Timber.d("onReceived KICK_OUT");
                disconnect();
                return;
            } else {
                long uid = -1;
                if (message instanceof NotificationData) {
                    try {
                        uid = Long.parseLong(((NotificationData) message).userid);
                    } catch (Exception ex) {
                    }
                }

                long mtaid = message.getMtaid();
                long mtuid = message.getMtuid();

                sendFeedbackStatus(mtaid, mtuid, uid);
                postResult(message);
            }


        }
    }


    @Override
    public void onError(Exception e) {
        Timber.d("onError %s", e);
        mState = Connection.State.Disconnected;
    }

    @Override
    public void onDisconnected(int code, String message) {
        Timber.d("onDisconnected %s", code);
        mState = Connection.State.Disconnected;
        disconnect();

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

        return send(MessageType.Request.AUTHEN_LOGIN, loginMsg.build());
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean sendAuthentication() {
        if (userConfig.hasCurrentUser()) {
            User user = userConfig.getCurrentUser();
            return sendAuthentication(user.accesstoken, Long.parseLong(user.uid));
        }
        return false;
    }

    public boolean sendFeedbackStatus(long mtaid, long mtuid, long uid) {

        if (mtaid <= 0 && mtuid <= 0) {
            return true;
        }

        Timber.d("sendFeedbackStatus: mtaid %s mtuid %s uid %s");

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

        return send(MessageType.Request.FEEDBACK, statusMsg.build());
    }
}
