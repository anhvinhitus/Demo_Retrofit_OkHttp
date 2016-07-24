package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;
import vn.com.vng.zalopay.domain.model.User;


/**
 * Created by AnhHieu on 6/14/16.
 * Parser for notification messages
 */
public class MessageParser implements Parser {

    final Gson mGson;
    final User user;

    public MessageParser(UserConfig userConfig, Gson gson) {
        this.mGson = gson;
        this.user = userConfig.getCurrentUser();
    }

    @Override
    public Event parserMessage(byte[] msg) {

        Event ret = null;
        if (msg.length != 0) {
            try {
                ret = processMessage(msg);
            } catch (Exception ex) {
                Timber.w(ex, " parserMessage Exception");
            }
        }

        return ret;
    }

    private Event processMessage(byte[] msg) throws Exception {
        ZPMsgProtos.DataResponseUser respMsg = ZPMsgProtos.DataResponseUser.parseFrom(msg);
        switch (respMsg.getMsgtype()) {
            case MessageType.Response.KICK_OUT:
                return processKickOutUser(respMsg.getMsgtype(), respMsg.getData().toByteArray());
            case MessageType.Response.PUSH_NOTIFICATION:
                return processPushMessage(respMsg.getMsgtype(), respMsg.getData().toByteArray());
            case MessageType.Response.AUTHEN_LOGIN_RESULT:
                return processAuthenticationLoginSuccess(respMsg.getMsgtype(), respMsg.getData().toByteArray());
            default:
        }

        return null;
    }

    public Event processAuthenticationLoginSuccess(int msgType, byte[] data) {
        try {
            AuthenticationData event = new AuthenticationData(msgType);
            ZPMsgProtos.ResultAuth res = ZPMsgProtos.ResultAuth.parseFrom(data);
            Timber.d("Result %s code %s", res.getResult(), res.getCode());
            event.code = res.getCode();
            event.uid = res.getUsrid();
            event.result = res.getResult();
            return event;
        } catch (Exception ex) {
            Timber.w(ex, "Error while handling authentication result");

        }
        return null;
    }

    public Event processKickOutUser(int msgType, byte[] data) {
        Timber.d("Connection was kicked out by server");
        return null;
    }

    public Event processPushMessage(int msgType, byte[] data) {
        String str = new String(data);
        Timber.d("got notification message: %s", str);
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        NotificationData event;
        try {
            event = mGson.fromJson(str, NotificationData.class);
            event.setMsgType(msgType);
        } catch (Exception ex) {
            Timber.w(ex, "Error in parsing notification message");
            event = null;
        }
        return event;
    }
}
