package vn.com.vng.zalopay.data.ws.message;

import android.os.Handler;
import android.os.Message;

/**
 * Created by haint3 on 29/03/2016.
 */
public class MessageHelper {

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageType.MSG_CONNECTED_TO_SERVER:
                    //  Toast.makeText(mainActivityContext, "Connect success", Toast.LENGTH_SHORT).show();
                    break;
                case MessageType.MSG_UI_SHOW_PUSH_NOTIFICATION:
                /*LogicMessages.PushNotificationInfo pushMsg = (LogicMessages.PushNotificationInfo)msg.obj;
                mainActivityContext.updatePushNotification(pushMsg.getTitle(), pushMsg.getPushInfo());*/
                    break;
                default:
            }
        }
    };

    public Handler getMessageHandler() {
        return messageHandler;
    }
}
