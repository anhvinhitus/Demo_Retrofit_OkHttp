package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.NotificationEntity;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;


/**
 * Created by AnhHieu on 6/14/16.
 */
public class MessageParser implements Parser {

    final Gson mGson;

    public MessageParser() {
        mGson = new Gson();
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
                return processAuthenticationLoginSuccess(respMsg.getMsgtype(), respMsg.getData().toByteArray());
            case MessageType.Response.PUSH_NOTIFICATION:
                return processPushMessage(respMsg.getMsgtype(), respMsg.getData().toByteArray());
            case MessageType.Response.AUTHEN_LOGIN_RESULT:
                return processKickOutUser(respMsg.getMsgtype(), respMsg.getData().toByteArray());
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
            Timber.w(ex, "processAuthenticationLoginSuccess");

        }
        return null;
    }

    public Event processKickOutUser(int msgType, byte[] data) {
        Timber.d("You kickedout");

        return null;
    }

    public Event processPushMessage(int msgType, byte[] data) {
        String str = new String(data);
        Timber.d("notification %s", str);
        NotificationEntity event = null;
        if (!TextUtils.isEmpty(str)) {

            try {
                event = mGson.fromJson(str, NotificationEntity.class);
                event.setMsgType(msgType);

            } catch (Exception ex) {
                Timber.w(ex, " Parse error");
                event = null;
            }
        }
        return event;
    }
}
