package vn.com.vng.zalopay.data.ws.connection;

import android.text.TextUtils;
import android.util.Pair;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.protobuf.MessageConnectionInfo;
import vn.com.vng.zalopay.data.protobuf.MessageLogin;
import vn.com.vng.zalopay.data.protobuf.MessageRecoveryRequest;
import vn.com.vng.zalopay.data.protobuf.MessageStatus;
import vn.com.vng.zalopay.data.protobuf.MessageType;
import vn.com.vng.zalopay.data.protobuf.PairKeyValue;
import vn.com.vng.zalopay.data.protobuf.PaymentRequestMessage;
import vn.com.vng.zalopay.data.protobuf.RecoveryOrder;
import vn.com.vng.zalopay.data.protobuf.StatusMessageClient;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.Enums;

/**
 * Created by huuhoa on 11/10/16.
 * Helper class to create message for sending to notification server
 */

public class NotificationApiHelper {
    public static NotificationApiMessage createMessageRecovery(int count, long timeStamp) {
        Timber.d("sendMessageRecovery");
        MessageRecoveryRequest.Builder request = new MessageRecoveryRequest.Builder()
                .count(count)
                .order(RecoveryOrder.ORDER_DESCEND.getValue())
                .starttime(timeStamp);

        return new NotificationApiMessage(
                MessageType.RECOVERY_REQUEST.getValue(),
                MessageRecoveryRequest.ADAPTER.encode(request.build())
        );
    }

    public static NotificationApiMessage createPingMessage(long userId) {
        MessageConnectionInfo pingMessage = new MessageConnectionInfo.Builder()
                .userid(userId)
                .embeddata(System.currentTimeMillis())
                .build();

        return new NotificationApiMessage(
                MessageType.PING_SERVER.getValue(),
                MessageConnectionInfo.ADAPTER.encode(pingMessage)
        );
    }

    public static NotificationApiMessage createAuthenticationMessage(String token, long uid, String gcmToken) {

        Timber.d("send authentication token %s zaloPayId %s gcmToken %s", token, uid, gcmToken);

        MessageLogin.Builder loginMsg = new MessageLogin.Builder()
                .token(token)
                .usrid(uid)
                .ostype(Enums.Platform.ANDROID.getId());

        if (!TextUtils.isEmpty(gcmToken)) {
            loginMsg.devicetoken(gcmToken);
        }

        return new NotificationApiMessage(
                MessageType.AUTHEN_LOGIN.getValue(),
                MessageLogin.ADAPTER.encode(loginMsg.build())
        );
    }

    public static NotificationApiMessage createFeedbackMessage(long mtaid, long mtuid, long uid) {
        StatusMessageClient.Builder statusMsg = new StatusMessageClient.Builder()
                .status(MessageStatus.RECEIVED.getValue());

        if (mtaid > 0) {
            statusMsg.mtaid(mtaid);
        }

        if (mtuid > 0) {
            statusMsg.mtuid(mtuid);
        }

        if (uid > 0) {
            statusMsg.userid(uid);
        }

        return new NotificationApiMessage(
                MessageType.FEEDBACK.getValue(),
                StatusMessageClient.ADAPTER.encode(statusMsg.build())
        );
    }

    public static NotificationApiMessage createPaymentRequestApi(long requestId, String domain, String method, int port, String path, List<Pair<String, String>> params, List<Pair<String, String>> headers) {
        PaymentRequestMessage message = buildRequest(requestId, domain, method, port, path, params, headers);
        return new NotificationApiMessage(
                MessageType.PAYMENT_REQUEST.getValue(),
                PaymentRequestMessage.ADAPTER.encode(message));
    }

    private static PaymentRequestMessage buildRequest(long requestId, String domain, String method, int port, String path, List<Pair<String, String>> params, List<Pair<String, String>> headers) {
        PaymentRequestMessage.Builder builder = new PaymentRequestMessage.Builder()
                .domain(domain)
                .method(method)
                .path(path)
                .requestid(requestId)
                .port(port);
                ;

        List<PairKeyValue> _header = transform(headers);
        if (_header != null) {
            builder.headers(_header);
        }

        List<PairKeyValue> _param = transform(params);
        if (_param != null) {
            builder.params(_param);
        }

        return builder.build();
    }

    private static List<PairKeyValue> transform(List<Pair<String, String>> vars) {
        return Lists.transform(vars, NotificationApiHelper::transform);
    }

    private static PairKeyValue transform(Pair<String, String> var) {
        if (var == null || TextUtils.isEmpty(var.first)) {
            return null;
        }

        PairKeyValue.Builder builder = new PairKeyValue.Builder()
                .key(var.first)
                .value(TextUtils.isEmpty(var.second) ? "" : var.second);

        return builder.build();
    }
}
