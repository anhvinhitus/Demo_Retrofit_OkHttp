package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.parser.Parser;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class WsConnection extends Connection implements ConnectionListener {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final String TAG = "ConnectionManager";
    public static final int TYPE_FIELD_LENGTH = 1;
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int HEADER_LENGTH = TYPE_FIELD_LENGTH + LENGTH_FIELD_LENGTH;

    private int PORT = 8404;
    private String HOST = "sandbox.notify.zalopay.com.vn";
    private String loginTokenKey = "mytoken";

    private NioEventLoopGroup group;
    private Channel mChannel;
    private ChannelFuture channelFuture;

    private final Context context;
    private Handler messageHandler = null;
    private final Parser parser;

    public WsConnection(Context context, Parser parser) {
        this.context = context;
        this.parser = parser;
    }

    public void setHandler(Handler handler) {
        this.messageHandler = handler;
    }

    public void setHostPort(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    @Override
    public void connect() {
        if (mChannel != null && mChannel.isOpen()) {
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
                    channelFuture = bootstrap.connect(new InetSocketAddress(HOST, PORT));
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
                Log.i(TAG, "send message to server: type = " + msgType);
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
    }

    @Override
    public void onReceived(byte[] data) {
        Timber.d("onReceived");
        GeneratedMessage message = parser.parserMessage(data);
        if (data != null) {
        }
    }

    @Override
    public void onError(int code, String message) {
        Timber.d("onError %s", code);
        mState = Connection.State.Disconnected;
    }

    @Override
    public void onDisconnected(int code, String message) {
        Timber.d("onDisconnected %s", code);
        mState = Connection.State.Disconnected;
    }
}
