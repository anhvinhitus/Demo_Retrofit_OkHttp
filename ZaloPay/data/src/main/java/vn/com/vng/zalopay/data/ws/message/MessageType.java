package vn.com.vng.zalopay.data.ws.message;

/**
 * Created by haint3 on 29/03/2016.
 */
public class MessageType {

    //msg ui
    public static final int MSG_UI                          = 2000;
    public static final int MSG_CONNECTED_TO_SERVER         = MSG_UI + 1;
    public static final int MSG_UI_SHOW_PUSH_NOTIFICATION   = MSG_UI + 2;

    public static class Request {
        public static final int   AUTHEN_LOGIN              = 1;
    }

    public static class Response {
        public static final int   AUTHEN_LOGIN_RESULT       = 1;
        public static final int   KICK_OUT                  = 51;
        public static final int   PUSH_NOTIFICATION         = 101;
    }
}
