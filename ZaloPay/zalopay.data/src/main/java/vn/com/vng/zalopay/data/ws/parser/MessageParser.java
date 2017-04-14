package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okio.ByteString;
import timber.log.Timber;
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

        Event event = null;

        if (respMsg.data != null) {
            ByteString data = respMsg.data;

            ServerMessageType messageType = ServerMessageType.fromValue(respMsg.msgtype);

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

        event.msgType = respMsg.msgtype;

        if (respMsg.mtaid != null) {
            event.mtaid = respMsg.mtaid;
        }

        if (respMsg.mtuid != null) {
            event.mtuid = respMsg.mtuid;
        }

        if (respMsg.sourceid != null) {
            event.sourceid = respMsg.sourceid;
        }

        return event;
    }

    private Event processAuthenticationLoginSuccess(ByteString data) {

        try {
            AuthenticationData event = new AuthenticationData();
            ResultAuth resp = ResultAuth.ADAPTER.decode(data);

            if (resp == null) {
                return event;
            }

            Timber.d("Result %s code %s", resp.result, resp.code);
            event.result = resp.result;
            event.uid = resp.usrid;

            if (resp.code != null) {
                event.code = resp.code;
            }

            if (resp.msg != null) {
                event.msg = resp.msg;
            }

            return event;
        } catch (Exception ex) {
            Timber.w(ex, "Error while handling authentication result");
        }

        return null;
    }

    private Event parseRecoveryResponse(ByteString data) {
        try {
            DataRecoveryResponse recoverMessage = DataRecoveryResponse.ADAPTER.decode(data);
            RecoveryMessageEvent recoveryMsg = new RecoveryMessageEvent();

            Timber.d("parseRecoveryResponse: recoverMessage %s", recoverMessage.messages.size());

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
            NotificationData notificationData = (NotificationData) event;

            if (message.mtaid != null) {
                event.mtaid = message.mtaid;
            }

            if (message.mtuid != null) {
                event.mtuid = message.mtuid;
            }

            if (MessageStatus.READ.getValue() == message.status) {
                notificationData.notificationstate = Enums.NotificationState.READ.getId();
            } else {
                notificationData.notificationstate = Enums.NotificationState.UNREAD.getId();
            }

            return notificationData;
        }

        return null;
    }

    private Event parsePongMessage(ByteString data) {
        try {
            ServerPongData pongData = new ServerPongData();
            MessageConnectionInfo resp = MessageConnectionInfo.ADAPTER.decode(data);

            if (resp.embeddata != null) {
                pongData.clientData = resp.embeddata;
            }

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
