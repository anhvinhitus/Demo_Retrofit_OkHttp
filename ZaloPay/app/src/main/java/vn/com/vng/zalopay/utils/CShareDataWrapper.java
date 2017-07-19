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
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;

/**
 * Created by longlv on 2/8/17.
 * Wrapper CShareData of PaymentSDK.
 */

public class CShareDataWrapper {

    private static List<MapCard> detectCCCard(List<MapCard> dMappedCards) {
        if (Lists.isEmptyOrNull(dMappedCards)) {
            return Collections.emptyList();
        }
        for (MapCard bankCard : dMappedCards) {
            if (BuildConfig.CC_CODE.equalsIgnoreCase(bankCard.bankcode)) {
                bankCard.bankcode = detectCCCard(bankCard.getFirstNumber());
            }
        }
        return dMappedCards;
    }

    public static String detectCCCard(String first6CardNo) {
        try {
            return CShareDataWrapper.detectCardType(first6CardNo);
        } catch (Exception e) {
            Timber.w(e, "detectCardType exception [%s]", e.getMessage());
        }
        return CardType.UNDEFINE;
    }

    public static List<MapCard> getMappedCardList(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        List<MapCard> mapCards = SDKApplication.getApplicationComponent()
                .linkInteractor().getMapCardList(user.zaloPayId);
        return detectCCCard(mapCards);
    }

    public static List<BankAccount> getMapBankAccountList(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return SDKApplication.getApplicationComponent()
                .linkInteractor()
                .getBankAccountList(user.zaloPayId);
    }

    public static String detectCardType(String first6CardNo) {
        return CShareData.getInstance().detectCardType(first6CardNo);
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
