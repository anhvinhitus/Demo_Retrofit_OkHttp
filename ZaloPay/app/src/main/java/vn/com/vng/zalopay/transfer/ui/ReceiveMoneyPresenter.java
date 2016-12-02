package vn.com.vng.zalopay.transfer.ui;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.PersonTransfer;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.notification.NotificationType;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.GlobalData;

/**
 * Created by huuhoa on 8/28/16.
 * Controller for receiving money
 */

final class ReceiveMoneyPresenter extends BaseUserPresenter implements IPresenter<IReceiveMoneyView>, GenerateQrCodeTask.ImageListener {

    private IReceiveMoneyView mView;

    private String mPreviousContent;

    private EventBus mEventBus;

    private final List<PersonTransfer> mListTransfer;
    private Set<Long> mMessageToUserId;

    private User mUser;

    private Map<String, Subscription> mapSubscription = new HashMap<>();

    @Inject
    ReceiveMoneyPresenter(User user, EventBus eventBus) {
        mListTransfer = new ArrayList<>();
        mUser = user;
        mEventBus = eventBus;
        mMessageToUserId = new HashSet<>();
    }

    @Override
    public void setView(IReceiveMoneyView view) {
        mView = view;
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void destroyView() {
        mEventBus.unregister(this);
        mView = null;
        mListTransfer.clear();
        mMessageToUserId.clear();
        cancelAllTimer();
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
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

    public void onViewCreated() {
        String content = generateQrContent();
        Timber.d("QR Content: %s", content);
        mPreviousContent = content;
        if (!TextUtils.isEmpty(content)) {
            new GenerateQrCodeTask(this, content).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        /*if (notify.appid == Constants.ZALOPAY_APP_ID &&
                isEqualCurrentUser(notify.destuserid) &&
                notify.notificationtype == NotificationType.MONEY_TRANSFER) {
            // extract sender, amount
            // extract transid
            // mView.displayReceivedMoney();
        }*/

        // {"transid":0,"appid":1,"timestamp":1472488434621,
        // "notificationtype":109,"userid":"160526000000502",
        // "receiverid":"160526000000502",
        // "embeddata":"eyJ0eXBlIjoxLCJkaXNwbGF5bmFtZSI6Ik5ndXnhu4VuIEjhu691IEhvw6AiLCJhdmF0YXIiOiJodHRwOi8vczI0MC5hdmF0YXIudGFsay56ZG4udm4vZS9kL2UvMi80LzI0MC9mMTg5OGEwYTBhM2YwNWJiYjExMDg4Y2IyMDJkMWMwMi5qcGciLCJtdF9wcm9ncmVzcyI6MX0"}
        if (notify.appid == 1 &&
                notify.notificationtype == NotificationType.APP_P2P_NOTIFICATION &&
                isEqualCurrentUser(notify.destuserid) &&
                !mMessageToUserId.contains(notify.mtuid)) {

            JsonObject embedData = notify.getEmbeddata();
            if (embedData == null) {
                return;
            }

            mMessageToUserId.add(notify.mtuid);

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
                try {
                    handleNotifications(notify, embedData);
                } catch (Exception e) {
                    Timber.e(e, "handle Notifications");
                }
            }
        }
    }

    private void handleNotifications(NotificationData notify, JsonObject embedData) {
        final String senderDisplayName = embedData.get("displayname").getAsString();
        final String senderAvatar = embedData.get("avatar").getAsString();
        final int progress = embedData.get("mt_progress").getAsInt();
        String transId = null;

        if (embedData.has("transid")) {
            transId = embedData.get("transid").getAsString();
        }

        final String zaloPayId = notify.getUserid();

        final long amount;
        if (embedData.has("amount")) {
            amount = embedData.get("amount").getAsLong();
        } else {
            amount = 0;
        }

        Timber.d("Receiver profile: %s - %s", senderDisplayName, senderAvatar);

        cancelTimer(zaloPayId);

        switch (progress) {
            case Constants.MoneyTransfer.STAGE_PRETRANSFER:
                Timber.d("Stage: Pre transfer");
                mView.displayWaitForMoney();
                startTimer(zaloPayId, senderDisplayName, senderAvatar);
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED:
                Timber.d("Stage: Transfer succeeded with amount %s", amount);
                displayReceivedMoney(senderDisplayName, senderAvatar, amount, transId);
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_FAILED:
                Timber.d("Stage: Transfer failed");
                break;
            case Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL:
                Timber.d("Stage: Transfer canceled");
                break;
        }

        addPersonTransfer(zaloPayId, senderDisplayName, senderAvatar, progress, amount, transId);

    }

    private void startTimer(final String zaloPayId, final String senderDisplayName, final String senderAvatar) {
        Subscription subscription = Observable.timer(5, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        Timber.d("call:  %s", aLong);
                        try {
                            addPersonTransfer(zaloPayId, senderDisplayName, senderAvatar, Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL, 0, null);
                        } catch (Exception e) {
                            Timber.d(e, "add person with state cancel");
                        }
                    }
                });
        mapSubscription.put(zaloPayId, subscription);
    }

    private void cancelTimer(String zaloPayId) {
        if (mapSubscription.containsKey(zaloPayId)) {
            Subscription subscription = mapSubscription.get(zaloPayId);
            unsubscribeIfNotNull(subscription);
            mapSubscription.remove(zaloPayId);
        }
    }

    private void cancelAllTimer() {
        Collection<Subscription> subscriptions = mapSubscription.values();
        for (Subscription subscription : subscriptions) {
            unsubscribeIfNotNull(subscription);
        }
        mapSubscription.clear();
    }

    private int mCountReceiveSuccess;

    private void displayReceivedMoney(String senderDisplayName, String senderAvatar, long amount, String transId) {

        if (TextUtils.isEmpty(transId)) {
            return;
        }

        if (existTransaction(transId)) {
            return;
        }
        
        trackEvent(mCountReceiveSuccess++);
        mView.displayReceivedMoney(senderDisplayName, senderAvatar, amount, transId);
    }

    private boolean existTransaction(String transId) {
        synchronized (mListTransfer) {
            for (PersonTransfer personTransfer : mListTransfer) {
                if (transId.equals(personTransfer.transId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEqualCurrentUser(String zalopayId) {
        return !TextUtils.isEmpty(zalopayId) && mUser.zaloPayId.equals(zalopayId);
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
        if (mView == null || mView.getContext() == null) {
            return;
        }

        mView.showError(mView.getContext().getString(R.string.Generate_qrcode_error));
    }

    private void addPersonTransfer(String uid, String displayName, String avatar, int state, long amount, String transId) {
        PersonTransfer item = transform(uid, displayName, avatar, state, amount, transId);
        synchronized (mListTransfer) {
            if (mListTransfer.isEmpty()) {
                mListTransfer.add(item);
                mView.addPersonTransfer(item);
                return;
            }

            if (mListTransfer.indexOf(item) < 0) {
                mListTransfer.add(0, item);
                mView.insertPersonTransfer(0, item);
                return;
            }

            for (int i = 0; i < mListTransfer.size(); i++) {
                PersonTransfer person = mListTransfer.get(i);
                if (person.equals(item)) {
                    if (person.state == Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED) {
                        mListTransfer.add(0, item);
                        mView.insertPersonTransfer(0, item);
                    } else {
                        mListTransfer.set(i, item);
                        mView.replacePersonTransfer(i, item);
                    }
                    return;
                }

            }
        }
    }


    private PersonTransfer transform(String uid, String displayName, String avatar, int state, long amount, String transId) {
        PersonTransfer item = new PersonTransfer();
        item.avatar = avatar;
        item.zaloPayId = uid;
        item.state = state;
        item.displayName = displayName;
        item.amount = amount;
        item.transId = transId;
        return item;
    }

    private void trackEvent(int count) {
        int eventId;
        switch (count) {
            case 1:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_1;
                break;
            case 2:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_2;
                break;
            case 3:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_3;
                break;
            case 4:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_4;
                break;
            case 5:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_5;
                break;
            case 6:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_6;
                break;
            case 7:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_7;
                break;
            case 8:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_8;
                break;
            case 9:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_9;
                break;
            default:
                eventId = ZPEvents.RECEIVEMONEY_RECEIVED_MORE;
                break;

        }

        ZPAnalytics.trackEvent(eventId);
    }
}
