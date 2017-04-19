package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.RefreshBankAccountEvent;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;

/**
 * Created by longlv on 2/8/17.
 * Wrapper CShareData of PaymentSDK.
 */

public class CShareDataWrapper {

    public static void getCardSupportList(UserInfo userInfo,
                                          IGetCardSupportListListener listener) {
        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(listener);
    }

    public static List<DMappedCard> getMappedCardList(String zaloPayId) {
        return CShareData.getInstance().getMappedCardList(zaloPayId);
    }

    public static List<DBankAccount> getMapBankAccountList(String zaloPayId) {
        return CShareData.getInstance().getMapBankAccountList(zaloPayId);
    }

    public static ECardType detectCardType(UserInfo userInfo, String first6CardNo) {
        return CShareData.getInstance().setUserInfo(userInfo).detectCardType(first6CardNo);
    }

    public static void getWithDrawBankList(IGetWithDrawBankList listener) {
        CShareData.getInstance().getWithDrawBankList(listener);
    }

    public static WDMaintenance getWithdrawMaintenance() {
        return CShareData.getInstance().getWithdrawMaintenance();
    }

    public static long getPlatformInfoExpiredTime() {
        return CShareData.getInstance().getPlatformInfoExpiredTime();
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
        DMappedCard card = new DMappedCard();
        card.last4cardno = last4cardno;
        card.first6cardno = first6cardno;
        params.mapCard = card;
        CShareData.getInstance().reloadMapCardList(params, listener);
    }


    public static List<DBanner> getBannerList() {
        return CShareData.getInstance().getBannerList();
    }

    public static void pushNotificationToSdk(User user, int notificationType, String message) {
        if (user == null
                || TextUtils.isEmpty(user.zaloPayId)
                || TextUtils.isEmpty(user.accesstoken)) {
            return;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = user.zaloPayId;
        userInfo.accessToken = user.accesstoken;

        CShareData.getInstance().setUserInfo(userInfo)
                .notifyLinkBankAccountFinish(new ZPWNotification(notificationType, message),
                        new IReloadMapInfoListener<DBankAccount>() {
                            @Override
                            public void onComplete(List<DBankAccount> pMapList) {
                                Timber.d("PushNotificationToSdk onComplete, type [%s]", notificationType);
                                EventBus.getDefault().post(new RefreshBankAccountEvent(pMapList));
                            }

                            @Override
                            public void onError(String pErrorMess) {
                                Timber.d("PushNotificationToSdk error, type [%s] message [%s]",
                                        notificationType, pErrorMess);
                                EventBus.getDefault().post(new RefreshBankAccountEvent(pErrorMess));
                            }
                        });
    }

    public static void notifyTransactionFinish(Object... pObject) {
        CShareData.getInstance().notifyTransactionFinish(pObject);
    }

    public static void dispose() {
        CShareData.dispose();
    }
}
