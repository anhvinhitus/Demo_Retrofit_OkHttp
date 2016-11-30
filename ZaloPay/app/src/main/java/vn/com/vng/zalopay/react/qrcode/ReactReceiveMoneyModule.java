package vn.com.vng.zalopay.react.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.notification.NotificationType;

/**
 * Created by hieuvm on 11/21/16.
 */

public class ReactReceiveMoneyModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {
    private User mUser;
    private EventBus mEventBus;

    ReactReceiveMoneyModule(ReactApplicationContext reactContext, User user, EventBus eventBus) {
        super(reactContext);
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mMessageToUserId = new HashSet<>();

        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayRM";
    }

    @ReactMethod
    public void generate(String amount, String message, Promise promise) {
        WritableMap item = Arguments.createMap();
        if (TextUtils.isEmpty(amount) || !TextUtils.isDigitsOnly(amount)) {
            item.putInt("code", -1);
            item.putString("message", "Amount invalid");
        } else {
            item.putInt("code", 1);
            item.putString("avatar", mUser.avatar);
            try {
                item.putString("qr", generateQrContent(Long.valueOf(amount), message));
            } catch (NumberFormatException e) {
                //empty
            }
        }

        promise.resolve(item);
    }


    public String generateQrContent(long amount, String message) {

        try {
            List<String> fields = new ArrayList<>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Constants.QRCode.RECEIVE_MONEY);
            fields.add(String.valueOf(Constants.QRCode.RECEIVE_MONEY));

            jsonObject.put("uid", Long.parseLong(mUser.zaloPayId));
            fields.add(mUser.zaloPayId);

            if (amount > 0) {
                jsonObject.put("amount", amount);
                fields.add(String.valueOf(amount));
            }

            if (!TextUtils.isEmpty(message)) {
                String messageBase64 = Base64.encodeToString(message.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP);
                jsonObject.put("message", messageBase64);
                fields.add(messageBase64);
            }

            String checksum = Utils.sha256(fields.toArray(new String[0])).substring(0, 8);
            Timber.d("generateQrContent: checksum %s", checksum);
            jsonObject.put("checksum", checksum);
            return jsonObject.toString();
        } catch (Exception ex) {
            Timber.d(ex, "generate content");
            return "";
        }
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onHostResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onHostPause() {
        mEventBus.unregister(this);
        mMessageToUserId.clear();
    }

    @Override
    public void onHostDestroy() {

    }

    private Set<Long> mMessageToUserId;

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onReceiverMoney(NotificationData notify) {
        if (notify.appid == 1 &&
                notify.notificationtype == NotificationType.APP_P2P_NOTIFICATION &&
                isEqualCurrentUser(notify.destuserid) &&
                !mMessageToUserId.contains(notify.getMtuid())) {

            JsonObject embedData = notify.getEmbeddata();

            if (embedData == null) {
                return;
            }

            mMessageToUserId.add(notify.getMtuid());

            int type = embedData.has("type") ? embedData.get("type").getAsInt() : 0;

            if (type == Constants.QRCode.RECEIVE_MONEY) {
                try {
                    handleNotifications(notify, embedData);
                } catch (Exception e) {
                    Timber.e(e, "handle Notifications");
                }
            }
        }
    }

    private boolean isEqualCurrentUser(String zalopayId) {
        return !TextUtils.isEmpty(zalopayId) && mUser.zaloPayId.equals(zalopayId);
    }

    private void handleNotifications(NotificationData notify, JsonObject embedData) throws Exception {

        String senderDisplayName = embedData.get("displayname").getAsString();
        String senderAvatar = embedData.get("avatar").getAsString();
        int progress = embedData.get("mt_progress").getAsInt();
        String transId = embedData.has("transid") ? embedData.get("transid").getAsString() : null;

        String zaloPayId = notify.getUserid();

        long amount = embedData.has("amount") ? embedData.get("amount").getAsLong() : 0;

        Timber.d("Receiver profile: %s - %s", senderDisplayName, senderAvatar);

        String eventName = null;
        WritableMap data = null;
        switch (progress) {
            case Constants.MoneyTransfer.STAGE_PRETRANSFER:
                Timber.d("Stage: Pre transfer");
                eventName = "RM_UserScanQR";
                data = transform(embedData);
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED:
                Timber.d("Stage: Transfer succeeded with amount %s", amount);
                eventName = "RM_UserSentMoney";
                data = transform(embedData);
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_FAILED:
                Timber.d("Stage: Transfer failed");
                eventName = "";
                data = null;
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL:
                Timber.d("Stage: Transfer canceled");
                eventName = "RM_UserCancel";
                data = transform(embedData);
                break;
        }

        Timber.d("handleNotifications: %s", data);

        if (!TextUtils.isEmpty(eventName) && data != null) {
            sendEvent(eventName, data);
        }
    }

    private void sendEvent(String eventName, WritableMap map) {
        ReactApplicationContext reactContext = getReactApplicationContext();
        if (reactContext == null) {
            return;
        }

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, map);
    }

    private WritableMap transform(JsonObject embedData) {
        WritableMap data = Arguments.createMap();
        WritableMap content = Arguments.createMap();

        for (Map.Entry<String, JsonElement> entry : embedData.entrySet()) {

            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                content.putString(key, value.getAsString());
            }
        }

        data.putMap("data", content);

        return data;
    }
}
