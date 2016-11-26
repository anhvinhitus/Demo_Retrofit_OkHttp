package vn.com.vng.zalopay.transfer.ui;

import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.JsonObject;

import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by huuhoa on 11/26/16.
 * Helper for sending notification while transfer money in QR mode
 */

class TransferNotificationHelper {

    private final NotificationStore.Repository mNotificationRepository;
    private final User mUser;

    TransferNotificationHelper(NotificationStore.Repository notificationRepository, User user) {

        mNotificationRepository = notificationRepository;
        mUser = user;
    }

    Subscription sendNotificationMessage(String toZaloPayId, int stage, long amount, String transId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", Constants.QRCode.RECEIVE_MONEY);
        jsonObject.addProperty("displayname", mUser.displayName);
        jsonObject.addProperty("avatar", mUser.avatar);
        jsonObject.addProperty("mt_progress", stage);
        if (!TextUtils.isEmpty(transId)) {
            jsonObject.addProperty("transid", transId);
        }

        if (amount > 0) {
            jsonObject.addProperty("amount", amount);
        }

        String embeddata = jsonObject.toString();
        Timber.d("Send notification: %s", embeddata);
        embeddata = Base64.encodeToString(embeddata.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
        return mNotificationRepository.sendNotification(toZaloPayId, embeddata)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<BaseResponse>());
    }
}
