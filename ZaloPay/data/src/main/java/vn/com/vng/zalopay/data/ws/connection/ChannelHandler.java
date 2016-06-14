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
public class ChannelHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Context ctx;
    private final ConnectionListener listener;

    public ChannelHandler(Context context, ConnectionListener handler) {
        this.ctx = context;
        this.listener = handler;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        if (listener != null) {
            listener.onReceived(msg);
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

        /*if (isNetworkAvailable(this.ctx)) {

        }*/
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
