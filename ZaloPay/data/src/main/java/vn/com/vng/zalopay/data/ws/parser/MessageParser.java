package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

        if (!respMsg.hasMtaid() && !respMsg.hasMtuid()) {
            Timber.e("Notification mtaid and mtuid not have");
            return null;
        }

        if (respMsg.hasMtaid() && respMsg.hasMtuid()) {
            Timber.e("Notification mtaid and mtuid both have");
            return null;
        }

        if (!respMsg.hasData()) {
            //  Timber.e("Notification no data");
            //  return null;
        }

        Event event = null;

        if (respMsg.hasData()) {

            byte[] data = respMsg.getData().toByteArray();

            switch (respMsg.getMsgtype()) {
                case MessageType.Response.KICK_OUT:
                    event = processKickOutUser(data);
                    break;
                case MessageType.Response.PUSH_NOTIFICATION:
                    event = processPushMessage(data);
                    break;
                case MessageType.Response.AUTHEN_LOGIN_RESULT:
                    event = processAuthenticationLoginSuccess(data);
                    break;
                default:
                    break;
            }
        }

        if (event == null) {
            event = new Event();
        }

        event.setMsgType(respMsg.getMsgtype());

        if (respMsg.hasMtaid()) {
            event.setMtaid(respMsg.getMtaid());
        }

        if (respMsg.hasMtuid()) {
            event.setMtuid(respMsg.getMtuid());
        }

        if (respMsg.hasSourceid()) {
            event.setSourceid(respMsg.getSourceid());
        }

        return event;
    }

    public Event processAuthenticationLoginSuccess(byte[] data) {

        try {
            AuthenticationData event = new AuthenticationData();
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

    public Event processKickOutUser(byte[] data) {
        Timber.d("Connection was kicked out by server");
        return null;
    }

    public Event processPushMessage(byte[] data) {

        try {
            NotificationData event = new NotificationData();
            String str = new String(data);
            Timber.d("notification message :  %s", str);
            if (!TextUtils.isEmpty(str)) {
                try {
                    event = mGson.fromJson(str, NotificationData.class);
                    event.setHasData(true);
                } catch (JsonSyntaxException e) {
                    Timber.w(e, "parse notification error %s", str);
                    event = new NotificationData();
                }
            }

            return event;
        } catch (Exception ex) {
            Timber.w(ex, "Error in parsing notification message");
        }
        return null;
    }
}
