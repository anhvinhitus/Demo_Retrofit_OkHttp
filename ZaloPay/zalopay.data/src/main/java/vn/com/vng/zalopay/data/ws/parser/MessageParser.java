package vn.com.vng.zalopay.data.ws.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okio.ByteString;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.network.PushMessage;
import vn.com.vng.zalopay.network.protobuf.PaymentResponseMessage;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.PaymentRequestData;
import vn.com.vng.zalopay.data.ws.model.RecoveryPushMessage;
import vn.com.vng.zalopay.data.ws.model.ServerPongData;
import vn.com.vng.zalopay.network.protobuf.DataRecoveryResponse;
import vn.com.vng.zalopay.network.protobuf.DataResponseUser;
import vn.com.vng.zalopay.network.protobuf.MessageConnectionInfo;
import vn.com.vng.zalopay.network.protobuf.MessageStatus;
import vn.com.vng.zalopay.network.protobuf.RecoveryMessage;
import vn.com.vng.zalopay.network.protobuf.ResultAuth;
import vn.com.vng.zalopay.network.protobuf.ServerMessageType;
import vn.com.vng.zalopay.domain.Enums;

import static vn.com.vng.zalopay.network.protobuf.ServerMessageType.PAYMENT_RESPONSE;
import static vn.com.vng.zalopay.network.protobuf.ServerMessageType.RECOVERY_RESPONSE;

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
    public PushMessage parserMessage(byte[] msg) {

        PushMessage ret = null;
        if (msg.length != 0) {
            try {
                ret = processMessage(msg);
            } catch (Exception ex) {
                Timber.w(ex, "parserMessage Exception");
            }
        }

        return ret;
    }

    private PushMessage processMessage(byte[] msg) throws Exception {
        DataResponseUser respMsg = DataResponseUser.ADAPTER.decode(msg);

        if (respMsg == null) {
            Timber.w("Read an encoded message from bytes : DataResponseUser is NULL");
            return null;
        }

        PushMessage pushMessage = null;
        int msgtype = ConvertHelper.unboxValue(respMsg.msgtype, -1);

        if (respMsg.data != null && msgtype >= 0) {
            ByteString data = respMsg.data;

            ServerMessageType messageType = ServerMessageType.fromValue(msgtype);
            if (messageType == ServerMessageType.KICK_OUT_USER) {
                pushMessage = processKickOutUser(data);
            } else if (messageType == ServerMessageType.PUSH_NOTIFICATION) {
                pushMessage = processPushMessage(data);
            } else if (messageType == ServerMessageType.AUTHEN_LOGIN_RESULT) {
                pushMessage = processAuthenticationLoginSuccess(data);
            } else if (messageType == ServerMessageType.PONG_CLIENT) {
                pushMessage = parsePongMessage(data);
            } else if (messageType == RECOVERY_RESPONSE) {
                pushMessage = parseRecoveryResponse(data);
            } else if (messageType == PAYMENT_RESPONSE) {
                pushMessage = parsePaymentRequestResponse(data);
            }
        }

        if (pushMessage == null) {
            pushMessage = new PushMessage();
        }

        pushMessage.msgType = msgtype;
        pushMessage.mtaid = ConvertHelper.unboxValue(respMsg.mtaid, 0);
        pushMessage.mtuid = ConvertHelper.unboxValue(respMsg.mtuid, 0);
        pushMessage.sourceid = ConvertHelper.unboxValue(respMsg.sourceid, 0);
        pushMessage.usrid = ConvertHelper.unboxValue(respMsg.usrid, 0);

        return pushMessage;
    }

    private PushMessage processAuthenticationLoginSuccess(ByteString data) {

        try {
            ResultAuth resp = ResultAuth.ADAPTER.decode(data);
            if (resp == null) {
                Timber.w("Read an encoded message from bytes : ResultAuth is NULL");
                return null;
            }

            Timber.d("Result %s code %s", resp.result, resp.code);

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

    private PushMessage parseRecoveryResponse(ByteString data) {
        try {
            DataRecoveryResponse recoverMessage = DataRecoveryResponse.ADAPTER.decode(data);
            if (recoverMessage == null) {
                Timber.w("Read an encoded message from bytes : DataRecoveryResponse is NULL");
                return null;
            }

            Timber.d("parse recovery : recover size [%s] starttime [%s]", recoverMessage.messages.size(), recoverMessage.starttime);

            RecoveryPushMessage recoverMsg = new RecoveryPushMessage();
            recoverMsg.usrid = ConvertHelper.unboxValue(recoverMessage.usrid, 0);

            for (RecoveryMessage message : recoverMessage.messages) {
                NotificationData event = processRecoveryMessage(message);
                if (event != null) {
                    recoverMsg.addRecoveryMessage(event);
                }
            }

            return recoverMsg;
        } catch (Exception e) {
            Timber.w(e, "error parse recovery response");
        }

        return null;
    }

    private PushMessage parsePaymentRequestResponse(ByteString data) {
        try {

            PaymentResponseMessage message = PaymentResponseMessage.ADAPTER.decode(data);
            if (message == null) {
                Timber.w("Read an encoded message from bytes : PaymentResponseMessage is NULL");
                return null;
            }

            PaymentRequestData event = new PaymentRequestData();
            event.requestid = ConvertHelper.unboxValue(message.requestid, 0);
            event.resultcode = ConvertHelper.unboxValue(message.resultcode, 0);
            event.usrid = ConvertHelper.unboxValue(message.usrid, 0);

            Timber.d("Parse payment request response : requestid [%s] resultdata [%s]", message.requestid, message.resultdata);

            if (message.resultdata != null) {
                event.resultdata = message.resultdata;
            }

            Timber.d("Response payment request <-- reqId: [%s], resultCode: [%s], resultData: [%s]", event.requestid, event.resultcode, event.resultdata);
            return event;
        } catch (Exception e) {
            Timber.w(e, " Parse payment request error");
        }

        return null;
    }

    private NotificationData processRecoveryMessage(RecoveryMessage message) {

        if (message.servermessagetype != null && message.servermessagetype != ServerMessageType.PUSH_NOTIFICATION.getValue()) {
            return null;
        }

        PushMessage pushMessage = processPushMessage(message.data);

        if (pushMessage instanceof NotificationData) {

            if (message.status != null && message.status == MessageStatus.DELETED.getValue()) {
                return null;
            }

            NotificationData notificationData = (NotificationData) pushMessage;
            pushMessage.mtaid = ConvertHelper.unboxValue(message.mtaid, 0);
            pushMessage.mtuid = ConvertHelper.unboxValue(message.mtuid, 0);

            if (message.status != null && message.status == MessageStatus.READ.getValue()) {
                notificationData.notificationstate = Enums.NotificationState.READ.getId();
            } else {
                notificationData.notificationstate = Enums.NotificationState.UNREAD.getId();
            }

            return notificationData;
        }

        return null;
    }

    private PushMessage parsePongMessage(ByteString data) {
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

    private PushMessage processKickOutUser(ByteString data) {
        Timber.d("Connection was kicked out by server");
        return null;
    }

    private PushMessage processPushMessage(ByteString data) {

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
