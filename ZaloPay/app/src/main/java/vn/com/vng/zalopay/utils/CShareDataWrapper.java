package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.RefreshBankAccountEvent;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;

/**
 * Created by longlv on 2/8/17.
 * Wrapper CShareData of PaymentSDK.
 */

public class CShareDataWrapper {

    private static List<MapCard> detectCCCard(List<MapCard> dMappedCards, User user) {
        if (Lists.isEmptyOrNull(dMappedCards)) {
            return Collections.emptyList();
        }
        for (MapCard bankCard : dMappedCards) {
            if (BuildConfig.CC_CODE.equalsIgnoreCase(bankCard.bankcode)) {
                bankCard.bankcode = BankUtils.detectCCCard(bankCard.getFirstNumber(), user);
            }
        }
        return dMappedCards;
    }

    public static List<MapCard> getMappedCardList(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return detectCCCard(CShareData.getInstance().getMappedCardList(user.zaloPayId), user);
    }

    public static List<BankAccount> getMapBankAccountList(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return CShareData.getInstance().getMapBankAccountList(user.zaloPayId);
    }

    public static String detectCardType(UserInfo userInfo, String first6CardNo) {
        return CShareData.getInstance().detectCardType(first6CardNo);
    }

    public static void getWithDrawBankList(IGetWithDrawBankList listener) {
        CShareData.getInstance().getWithDrawBankList(listener);
    }

    public static Maintenance getWithdrawMaintenance() {
        return CShareData.getInstance().getWithdrawMaintenance();
    }

    public static long getPlatformInfoExpiredTime() {
        return SDKApplication.getApplicationComponent().platformInfoInteractor().getPlatformInfoDurationExpire();
    }

    public static boolean isEnableDeposite() {
        return CShareData.getInstance().isEnableDeposite();
    }

    public static long getMinTranferValue() {
        return CShareData.getInstance().getMinTranferValue();
    }

    public static long getMaxTranferValue() {
        return CShareData.getInstance().getMaxTranferValue();
    }

    public static long getMinDepositValue() {
        return CShareData.getInstance().getMinDepositValue();
    }

    public static long getMaxDepositValue() {
        return CShareData.getInstance().getMaxDepositValue();
    }

    public static long getMinWithDrawValue() {
        return CShareData.getInstance().getMinWithDrawValue();
    }

    public static long getMaxWithDrawValue() {
        return CShareData.getInstance().getMaxWithDrawValue();
    }

    public static void reloadMapCardList(String last4cardno, String first6cardno, User user, IReloadMapInfoListener listener) {
        if (user == null) {
            return;
        }

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        params.userID = user.zaloPayId;
        params.accessToken = user.accesstoken;
        MapCard card = new MapCard();
        card.last4cardno = last4cardno;
        card.first6cardno = first6cardno;
        params.mapCard = card;
        CShareData.getInstance().reloadMapCardList(params, listener);
    }

    public static void pushNotificationToSdk(User user, int notificationType, String message) {
        if (user == null
                || TextUtils.isEmpty(user.zaloPayId)
                || TextUtils.isEmpty(user.accesstoken)) {
            return;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.zalopay_userid = user.zaloPayId;
        userInfo.accesstoken = user.accesstoken;

        CShareData.getInstance().notifyLinkBankAccountFinish(new ZPWNotification(notificationType, message),
                        new IReloadMapInfoListener<BankAccount>() {
                            @Override
                            public void onComplete(List<BankAccount> pMapList) {
                                Timber.d("PushNotificationToSdk onComplete, type [%s]", notificationType);
                                EventBus.getDefault().post(new RefreshBankAccountEvent(pMapList));
                            }

                            @Override
                            public void onError(String pErrorMess) {
                                Timber.d("PushNotificationToSdk error, type [%s] message [%s]",
                                        notificationType, pErrorMess);
                                EventBus.getDefault().post(new RefreshBankAccountEvent(pErrorMess));
                            }
                        }, userInfo);
    }

    public static void notifyTransactionFinish(Object... pObject) {
        CShareData.getInstance().notifyTransactionFinish(pObject);
    }

    public static void notifyPromotionEventToSdk(Object... pObject) {
        CShareData.getInstance().notifyPromotionEvent(pObject);
    }

    public static void dispose() {
        CShareData.dispose();
    }
}
