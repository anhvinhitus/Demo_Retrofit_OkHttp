package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okio.ByteString;
import timber.log.Timber;
import vn.com.vng.zalopay.data.protobuf.PaymentResponseMessage;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;
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

import static vn.com.vng.zalopay.data.protobuf.ServerMessageType.PAYMENT_RESPONSE;
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
            } else if (messageType == PAYMENT_RESPONSE) {
                event = parsePaymentRequestResponse(data);
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
            ResultAuth res = ResultAuth.ADAPTER.decode(data);
            Timber.d("Result %s code %s", res.result, res.code);
            event.code = res.code;
            event.uid = res.usrid;
            event.result = res.result;
            return event;
        } catch (Exception ex) {
            Timber.w(ex, "Error while handling authentication result");
        }

        return null;
    }

    private Event parseRecoveryResponse(ByteString data) {
        try {
            DataRecoveryResponse recoverMessage = DataRecoveryResponse.ADAPTER.decode(data);
            Timber.d("parseRecoveryResponse: recoverMessage %s", recoverMessage.messages.size());

            RecoveryMessageEvent recoveryMessageEvent = new RecoveryMessageEvent();

            for (RecoveryMessage message : recoverMessage.messages) {
                NotificationData event = processRecoveryMessage(message);
                if (event != null) {
                    recoveryMessageEvent.addRecoveryMessage(event);
                }
            }

            return recoveryMessageEvent;
        } catch (Exception e) {
            Timber.e(e, "error parse recovery response");
        }

        return null;
    }

    private Event parsePaymentRequestResponse(ByteString data) {
        try {
            PaymentResponseMessage message = PaymentResponseMessage.ADAPTER.decode(data);
            Timber.d("parsePaymentRequestResponse: %s", message);
            PaymentRequestData event = new PaymentRequestData();
            event.requestid = message.requestid;
            event.resultcode = message.resultcode;
            event.resultdata = message.resultdata;

            Timber.d("Response payment request --> reqId[%s] resultCode[%s] resultData[%s]", event.requestid, event.resultcode, event.resultdata);
            return event;
        } catch (Exception e) {
            Timber.e(e, "Error ");
        }

        return null;
    }

    private NotificationData processRecoveryMessage(RecoveryMessage message) {
        Event event = processPushMessage(message.data);
        Timber.d("event %s", event);
        if (event instanceof NotificationData) {
            NotificationData notificationData = (NotificationData) event;

            if (message.mtaid != null) {
                event.mtaid = message.mtaid;
            }

            if (message.mtuid != null) {
                event.mtuid = message.mtuid;
            }

            notificationData.notificationstate = (Enums.NotificationState.UNREAD.getId());

            if (message.status == MessageStatus.READ.getValue()) {
                notificationData.notificationstate = (Enums.NotificationState.READ.getId());
            }
            return notificationData;
        }

        return null;
    }

    private Event parsePongMessage(ByteString data) {
        if (data == null) {
            return null;
        }

        try {
            ServerPongData pongData = new ServerPongData();
            MessageConnectionInfo res = MessageConnectionInfo.ADAPTER.decode(data);
            pongData.clientData = res.embeddata;
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
