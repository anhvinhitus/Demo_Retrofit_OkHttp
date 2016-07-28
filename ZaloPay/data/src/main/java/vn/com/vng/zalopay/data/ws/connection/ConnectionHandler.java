package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import timber.log.Timber;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class ConnectionHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Context ctx;
    private final ConnectionListener listener;

    public ConnectionHandler(Context context, ConnectionListener handler) {
        this.ctx = context;
        this.listener = handler;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        Timber.d("channelRead0 %s", Thread.currentThread().getName());

        try {
            if (listener != null) {
                listener.onReceived(msg);
            }
        } catch (Exception ex) {
            Timber.e(ex, "Exception while parsing notification message");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Timber.d("channelActive");
        super.channelActive(ctx);
        if (listener != null)
            listener.onConnected();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

        Timber.d("channelUnregistered");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        Timber.d("channelReadComplete");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Timber.d("channelInactive");
        super.channelInactive(ctx);
        ctx.close();

        if (listener != null) {
            listener.onDisconnected(-1, "");
        }
    }
}
