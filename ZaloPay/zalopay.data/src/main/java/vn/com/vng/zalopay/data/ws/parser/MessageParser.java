package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okio.ByteString;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.RecoveryMessageEvent;
import vn.com.vng.zalopay.data.ws.model.ServerPongData;
import vn.com.vng.zalopay.data.protobuf.DataRecoveryResponse;
import vn.com.vng.zalopay.data.protobuf.DataResponseUser;
import vn.com.vng.zalopay.data.protobuf.MessageConnectionInfo;
import vn.com.vng.zalopay.data.protobuf.MessageStatus;
import vn.com.vng.zalopay.data.protobuf.RecoveryMessage;
import vn.com.vng.zalopay.data.protobuf.ResultAuth;
import vn.com.vng.zalopay.data.protobuf.ServerMessageType;
import vn.com.vng.zalopay.domain.Enums;

import static vn.com.vng.zalopay.data.protobuf.ServerMessageType.RECOVERY_RESPONSE;

/**
 * Created by AnhHieu on 6/14/16.
 * Parser for notification messages
 */
public class MessageParser implements Parser {

    private final Gson mGson;

    public MessageParser(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public Event parserMessage(byte[] msg) {

        Event ret = null;
        if (msg.length != 0) {
            try {
                ret = processMessage(msg);
            } catch (Exception ex) {
                Timber.w(ex, "parserMessage Exception");
            }
        }

        return ret;
    }

    private Event processMessage(byte[] msg) throws Exception {
        DataResponseUser respMsg = DataResponseUser.ADAPTER.decode(msg);

        if (respMsg == null) {
            Timber.w("Read an encoded message from bytes : DataResponseUser is NULL");
            return null;
        }

        Event event = null;
        int msgtype = ConvertHelper.unboxValue(respMsg.msgtype, -1);

        if (respMsg.data != null && msgtype >= 0) {
            ByteString data = respMsg.data;

            ServerMessageType messageType = ServerMessageType.fromValue(msgtype);

            if (messageType == ServerMessageType.KICK_OUT_USER) {
                event = processKickOutUser(data);
            } else if (messageType == ServerMessageType.PUSH_NOTIFICATION) {
                event = processPushMessage(data);
            } else if (messageType == ServerMessageType.AUTHEN_LOGIN_RESULT) {
                event = processAuthenticationLoginSuccess(data);
            } else if (messageType == ServerMessageType.PONG_CLIENT) {
                event = parsePongMessage(data);
            } else if (messageType == RECOVERY_RESPONSE) {
                event = parseRecoveryResponse(data);
            }
        }

        if (event == null) {
            event = new Event();
        }

        event.msgType = msgtype;
        event.mtaid = ConvertHelper.unboxValue(respMsg.mtaid, 0);
        event.mtuid = ConvertHelper.unboxValue(respMsg.mtuid, 0);
        event.sourceid = ConvertHelper.unboxValue(respMsg.sourceid, 0);

        return event;
    }

    private Event processAuthenticationLoginSuccess(ByteString data) {

        try {
            ResultAuth resp = ResultAuth.ADAPTER.decode(data);
            if (resp == null) {
                Timber.w("Read an encoded message from bytes : ResultAuth is NULL");
                return null;
            }

            Timber.d("Parse ResultAuth : usrid %s result %s code %s ", resp.usrid, resp.result, resp.code);

            AuthenticationData event = new AuthenticationData();

            event.uid = ConvertHelper.unboxValue(resp.usrid, 0);
            event.result = ConvertHelper.unboxValue(resp.result, 0);
            event.code = ConvertHelper.unboxValue(resp.code, 0);
            event.msg = resp.msg;

            return event;
        } catch (Exception ex) {
            Timber.w(ex, "Error while handling authentication result");
        }

        return null;
    }

    private Event parseRecoveryResponse(ByteString data) {
        try {
            DataRecoveryResponse recoverMessage = DataRecoveryResponse.ADAPTER.decode(data);
            if (recoverMessage == null) {
                Timber.w("Read an encoded message from bytes : DataRecoveryResponse is NULL");
                return null;
            }
            Timber.d("parse recovery : recover size [%s] starttime [%s]", recoverMessage.messages.size(), recoverMessage.starttime);

            RecoveryMessageEvent recoveryMsg = new RecoveryMessageEvent();
            for (RecoveryMessage message : recoverMessage.messages) {
                NotificationData event = processRecoveryMessage(message);
                if (event != null) {
                    recoveryMsg.addRecoveryMessage(event);
                }
            }

            return recoveryMsg;

        } catch (Exception e) {
            Timber.e(e, "error parse recovery response");
        }

        return null;
    }

    private NotificationData processRecoveryMessage(RecoveryMessage message) {
        Event event = processPushMessage(message.data);

        if (event instanceof NotificationData) {

            if (message.status != null && message.status == MessageStatus.DELETED.getValue()) {
                return null;
            }

            NotificationData notify = (NotificationData) event;

            event.mtaid = ConvertHelper.unboxValue(message.mtaid, 0);
            event.mtuid = ConvertHelper.unboxValue(message.mtuid, 0);

            if (message.status != null && message.status == MessageStatus.READ.getValue()) {
                notify.notificationstate = Enums.NotificationState.READ.getId();
            } else {
                notify.notificationstate = Enums.NotificationState.UNREAD.getId();
            }

            return notify;
        }

        return null;
    }

    private Event parsePongMessage(ByteString data) {
        try {
            MessageConnectionInfo message = MessageConnectionInfo.ADAPTER.decode(data);
            if (message == null) {
                Timber.w("Read an encoded message from bytes : MessageConnectionInfo is NULL");
                return null;
            }

            ServerPongData pongData = new ServerPongData();
            pongData.clientData = ConvertHelper.unboxValue(message.embeddata, 0);
            return pongData;
        } catch (IOException e) {
            Timber.w(e, "Invalid server pong data");
            return null;
        }
    }

    private Event processKickOutUser(ByteString data) {
        Timber.d("Connection was kicked out by server");
        return null;
    }


    private Event processPushMessage(ByteString data) {

        try {
            NotificationData event;
            String str = new String(data.toByteArray());
            Timber.d("notification message :  %s", str);
            if (TextUtils.isEmpty(str)) {
                return new NotificationData();
            }

            try {
                event = mGson.fromJson(str, NotificationData.class);
                event.hasData = true;
            } catch (JsonSyntaxException e) {
                Timber.w(e, "parse notification error %s", str);
                event = new NotificationData();
            }

            return event;
        } catch (Exception ex) {
            Timber.w(ex, "Error in parsing notification message");
        }
        return null;
    }
}
