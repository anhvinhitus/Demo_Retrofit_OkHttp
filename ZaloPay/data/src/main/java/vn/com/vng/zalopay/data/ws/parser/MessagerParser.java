package vn.com.vng.zalopay.data.ws.parser;

import com.google.protobuf.GeneratedMessage;

import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.protobuf.LogicMessages;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class MessagerParser implements Parser {

    @Override
    public GeneratedMessage parserMessage(byte[] msg) {

        GeneratedMessage ret = null;

        if (msg.length != 0) {
            try {
                ret = processMessage(msg);
            } catch (Exception ex) {
                Timber.e(ex, " parserMessage Exception");
            }
        }

        return ret;

    }

    private GeneratedMessage processMessage(byte[] msg) throws Exception {
        LogicMessages.ServerResponse respMsg = LogicMessages.ServerResponse.parseFrom(msg);

        switch (respMsg.getMsgType()) {
            case MessageType.Response.AUTHEN_LOGIN_SUCCESS:
                return processAuthenticationLoginSuccess(respMsg.getLoginSuccess());
            case MessageType.Response.PUSH_NOTIFICATION:
                return processPushMessage(respMsg.getPushInfo());
            default:
        }

        return null;
    }

    private GeneratedMessage processAuthenticationLoginSuccess(LogicMessages.LoginSuccess loginSuccess) {
        Timber.d("process Authentication ");
        return loginSuccess;
    }

    public GeneratedMessage processPushMessage(LogicMessages.PushNotificationInfo pushMsg) {
        Timber.d("processPushMessage");
        return pushMsg;
    }
}
