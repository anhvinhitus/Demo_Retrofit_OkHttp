package vn.com.vng.zalopay.transfer.ui.presenter;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.notification.NotificationType;
import vn.com.vng.zalopay.transfer.ui.view.IReceiveMoneyView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.data.GlobalData;

/**
 * Created by huuhoa on 8/28/16.
 * Controller for receiving money
 */

public class ReceiveMoneyPresenter implements IPresenter<IReceiveMoneyView>, GenerateQrCodeTask.ImageListener {

    private IReceiveMoneyView mView;

    private String mPreviousContent;

    @Inject
    EventBus eventBus;

    @Inject
    UserConfig userConfig;

    @Inject
    ReceiveMoneyPresenter() {

    }

    @Override
    public void setView(IReceiveMoneyView view) {
        mView = view;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {
        eventBus.register(this);
    }

    @Override
    public void pause() {
        eventBus.unregister(this);
    }

    @Override
    public void destroy() {
        GlobalData.initApplication(null);
    }


    private String generateQrContent() {
        return generateQrContent(0, "");
    }

    public String generateQrContent(long amount, String message) {
        try {
            User user = userConfig.getCurrentUser();
            if (user == null) {
                return "";
            }

            List<String> fields = new ArrayList<>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Constants.QRCode.RECEIVE_MONEY);
            fields.add(String.valueOf(Constants.QRCode.RECEIVE_MONEY));

            jsonObject.put("uid", Long.parseLong(user.zaloPayId));
            fields.add(user.zaloPayId);

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

    public void onViewCreated() {
        String content = generateQrContent();
        Timber.d("QR Content: %s", content);
        mPreviousContent = content;
        if (!TextUtils.isEmpty(content)) {
            new GenerateQrCodeTask(this, content).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //  mView.setUserInfo(userConfig.getCurrentUser().displayName, userConfig.getCurrentUser().avatar);
        // mView.displayWaitForMoney();
    }


    public void setUserInfo() {
        if (mView == null) {
            return;
        }

        if (userConfig.hasCurrentUser()) {
            mView.setUserInfo(userConfig.getCurrentUser().displayName, userConfig.getCurrentUser().avatar);
        }
    }

    public void updateQRWithAmount(long amount, String message) {
        String content = generateQrContent(amount, message);
        if (content.equals(mPreviousContent)) {
            return;
        }
        mPreviousContent = content;

        Timber.d("pre-encode content %s", content);

        if (!TextUtils.isEmpty(content)) {
            new GenerateQrCodeTask(this, content).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverMoney(NotificationData notify) {
        if (mView == null || notify == null) {
            return;
        }

        // {"transid":160828000000011,"appid":1,"timestamp":1472352416687,
        // "message":"Nguyễn Hữu Hoà đã chuyển cho bạn 15.000 VND",
        // "notificationtype":4,
        // "userid":"160526000000502",
        // "destuserid":"160601000000002"}
        if (notify.appid == Constants.ZALOPAY_APP_ID &&
                isEqualCurrentUser(notify.destuserid) &&
                notify.notificationtype == NotificationType.MONEY_TRANSFER) {
            // extract sender, amount
            // extract transid
            // mView.displayReceivedMoney();
        }

        // {"transid":0,"appid":1,"timestamp":1472488434621,
        // "notificationtype":109,"userid":"160526000000502",
        // "receiverid":"160526000000502",
        // "embeddata":"eyJ0eXBlIjoxLCJkaXNwbGF5bmFtZSI6Ik5ndXnhu4VuIEjhu691IEhvw6AiLCJhdmF0YXIiOiJodHRwOi8vczI0MC5hdmF0YXIudGFsay56ZG4udm4vZS9kL2UvMi80LzI0MC9mMTg5OGEwYTBhM2YwNWJiYjExMDg4Y2IyMDJkMWMwMi5qcGciLCJtdF9wcm9ncmVzcyI6MX0"}
        if (notify.appid == 1 &&
                notify.notificationtype == NotificationType.APP_P2P_NOTIFICATION) {
            JsonObject embedData = notify.getEmbeddata();
            if (embedData == null) {
                return;
            }

            Timber.d("Embed data: %s", embedData);
//            jsonObject.addProperty("type", Constants.QRCode.RECEIVE_MONEY);
//            jsonObject.addProperty("displayname", user.displayName);
//            jsonObject.addProperty("avatar", user.avatar);
//            jsonObject.addProperty("mt_progress", stage);
//            if (amount > 0) {
//                jsonObject.addProperty("amount", mTransaction.amount);

            int type = 0;
            if (embedData.has("type")) {
                type = embedData.get("type").getAsInt();
            }

            if (type == Constants.QRCode.RECEIVE_MONEY) {
                handleNotifications(notify, embedData);
            }
        }
    }

    private void handleNotifications(NotificationData notify, JsonObject embedData) {
        String senderDisplayName = embedData.get("displayname").getAsString();
        String senderAvatar = embedData.get("avatar").getAsString();
        int progress = embedData.get("mt_progress").getAsInt();
        String transId = null;
        try {
            if (embedData.has("transid")) {
                transId = embedData.get("transid").getAsString();
            }
        } catch (Exception e) {
            Timber.d(e, "exception");
        }

        String zaloPayId = notify.getUserid();

        long amount = 0;
        if (embedData.has("amount")) {
            amount = embedData.get("amount").getAsLong();
        }

        Timber.d("Receiver profile: %s - %s", senderDisplayName, senderAvatar);
        switch (progress) {
            case Constants.MoneyTransfer.STAGE_PRETRANSFER:
                Timber.d("Stage: Pre transfer");
                mView.displayWaitForMoney();
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED:
                Timber.d("Stage: Transfer succeeded with amount %s", amount);
                mView.displayReceivedMoney(amount, transId);
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_FAILED:
                Timber.d("Stage: Transfer failed");
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL:
                Timber.d("Stage: Transfer canceled");
                break;
        }

        mView.setReceiverInfo(zaloPayId, senderDisplayName, senderAvatar, progress, amount, transId);
    }

    private boolean isEqualCurrentUser(String zalopayId) {
        if (userConfig.getCurrentUser() == null) {
            return false;
        }

        if (TextUtils.isEmpty(zalopayId)) {
            return false;
        }

        if (TextUtils.isEmpty(userConfig.getCurrentUser().zaloPayId)) {
            return false;
        }

        return userConfig.getCurrentUser().zaloPayId.equals(zalopayId);
    }

    @Override
    public void onImageGenerated(Bitmap bitmap) {
        if (mView == null) {
            return;
        }

        mView.setQrImage(bitmap);
    }

    @Override
    public void onImageGeneratedError() {
        if (mView == null) {
            return;
        }

        mView.showError("Sinh mã QR thất bại!");
    }
}
