package vn.com.vng.zalopay.data.ws.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.protobuf.LogicMessages;

/**
 * Created by HaiNT on 3/28/2016.
 */
public class ProcessHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Context ctx;
    private final Handler messageHandler;

    public ProcessHandler(Context context, Handler handler) {
        this.ctx = context;
        this.messageHandler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Timber.d("channelActive");
        super.channelActive(ctx);
        messageHandler.sendEmptyMessage(MessageType.MSG_CONNECTED_TO_SERVER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Timber.d("channelInactive");
        super.channelInactive(ctx);
        ctx.close();
        if (isNetworkAvailable(this.ctx)) {
            //ConnectionManager.getInstance().connected();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        Timber.d("channelRead0");

        processMessage(msg);
    }

    public void processMessage(byte[] msg) {

        if (msg.length == 0) {
            return;
        }

        try {
            LogicMessages.ServerResponse respMsg = LogicMessages.ServerResponse.parseFrom(msg);

            switch (respMsg.getMsgType()) {
                case MessageType.Response.AUTHEN_LOGIN_SUCCESS:
                    processAuthenticationLoginSuccess(respMsg.getLoginSuccess());
                    break;
                case MessageType.Response.PUSH_NOTIFICATION:
                    processPushMessage(respMsg.getPushInfo());
                    break;
                default:
            }
        } catch (InvalidProtocolBufferException ex) {
            Timber.e(ex, "ProcessMessage InvalidProtocolBufferException");
        } catch (Exception ex) {
            Timber.e(ex, "ProcessMessage exception");
        }
    }

    public void processAuthenticationLoginSuccess(LogicMessages.LoginSuccess loginSuccess) {
        Timber.d("process Authentication ");
    }

    public void processPushMessage(LogicMessages.PushNotificationInfo pushMsg) {
        Timber.d("processPushMessage");

        Message uiMsg = new Message();
        uiMsg.what = MessageType.MSG_UI_SHOW_PUSH_NOTIFICATION;
        uiMsg.obj = pushMsg;
        messageHandler.sendMessage(uiMsg);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
