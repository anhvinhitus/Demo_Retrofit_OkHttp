package vn.com.vng.zalopay.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.protobuf.LogicMessages;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class ZaloPayService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    WsConnection mWsConnection;

    public ZaloPayService() {
        mWsConnection = new WsConnection(getApplicationContext());
        mWsConnection.setHandler(mMessageHandler);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timber.d("onStartCommand %s startId %s", intent, startId);
        if (!mWsConnection.isConnected()) {
            mWsConnection.connect();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public boolean sendAuthenticationLogin(String token) {
        LogicMessages.Login loginMsg = LogicMessages.Login.newBuilder()
                .setTokenKey(token)
                .build();
        return mWsConnection.send(MessageType.Request.AUTHEN_LOGIN, loginMsg);
    }


    protected final Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageType.MSG_CONNECTED_TO_SERVER:

                    break;
                case MessageType.MSG_UI_SHOW_PUSH_NOTIFICATION:
                    LogicMessages.PushNotificationInfo pushMsg = (LogicMessages.PushNotificationInfo) msg.obj;


                    break;
                default:
            }
        }
    };
}
