package vn.com.vng.zalopay.data.ws.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.Listener;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class ConnectionHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Listener listener;

    public ConnectionHandler(Listener handler) {
        this.listener = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        Timber.d("channelRead0 %s", Thread.currentThread().getName());

        try {
            if (listener != null) {
                listener.onMessage(msg);
            }
        } catch (Exception ex) {
            Timber.e(ex, "Exception while parsing notification message");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Timber.d("channelActive");
        super.channelActive(ctx);
        if (listener != null) {
            listener.onConnected();
        }
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if (listener != null) {
            listener.onError(cause);
        }
    }
}
