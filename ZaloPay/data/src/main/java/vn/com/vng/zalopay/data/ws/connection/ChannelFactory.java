package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;

import java.nio.ByteOrder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import timber.log.Timber;

/**
 * Created by HaiNT on 3/27/2016.
 */
public class ChannelFactory extends ChannelInitializer<SocketChannel> {
    private final Context context;
    private final ConnectionListener listener;

    public ChannelFactory(Context ctx, ConnectionListener listener) {
        this.context = ctx;
        this.listener = listener;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, 8192, 0, 4, 0, 4, true));
        pipeline.addLast("bytesDecoder", new ByteArrayDecoder());
        pipeline.addLast("bytesEncoder", new ByteArrayEncoder());
        pipeline.addLast("handler", new ConnectionHandler(context, listener));
    }

}
