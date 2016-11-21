package vn.com.vng.zalopay.react.qrcode;

import android.text.TextUtils;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 11/21/16.
 */

public class QrCodeReceiveMoneyModule extends ReactContextBaseJavaModule {
    private User mUser;

    public QrCodeReceiveMoneyModule(ReactApplicationContext reactContext, User user) {
        super(reactContext);
        this.mUser = user;
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

}
