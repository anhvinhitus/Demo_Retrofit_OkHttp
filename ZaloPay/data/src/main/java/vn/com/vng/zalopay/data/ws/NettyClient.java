package vn.com.vng.zalopay.data.ws;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.connection.ChannelFactory;
import vn.com.vng.zalopay.data.ws.connection.Connection;

/**
 * Created by AnhHieu on 8/10/16.
 */
public class NettyClient implements SocketClient {

    private NioEventLoopGroup group;
    private Channel mChannel;
    private ChannelFuture channelFuture;

    private String mHost;
    private int mPort;
    private Listener mListener;

    protected Connection.State mState = Connection.State.Disconnected;

    public NettyClient(String hostname, int port, Listener listener) {
        mHost = hostname;
        mPort = port;
        mListener = listener;
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
                    bootstrap.handler(new ChannelFactory(mListener));
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.option(ChannelOption.TCP_NODELAY, true);
                    bootstrap.option(ChannelOption.SO_TIMEOUT, 10000);
                    channelFuture = bootstrap.connect(new InetSocketAddress(mHost, mPort));
                    mChannel = channelFuture.sync().channel();
                    mState = Connection.State.Connecting;
                } catch (InterruptedException e) {
                    Timber.e(e, "Connect socket error with InterruptedException");
                    mState = Connection.State.Disconnected;
                    mListener.onError(e);
                } catch (Exception e) {
                    Timber.e(e, "Connect socket exception");
                    mState = Connection.State.Disconnected;
                    mListener.onError(e);
                }
            }
        }.start();
    }

    @Override
    public void disconnect() {
        if (mChannel != null && mChannel.isOpen()) {
            mChannel.close();
        }

        if (group != null) {
            group.shutdownGracefully();
        }
        mState = Connection.State.Disconnected;
    }

    @Override
    public void send(byte[] data) {
        if (isConnected()) {
            mChannel.writeAndFlush(data);
        }
    }

    @Override
    public boolean isConnected() {
        return mChannel != null && mChannel.isActive();
    }

    @Override
    public boolean isConnecting() {
        return mChannel != null && mChannel.isOpen();
    }
}
