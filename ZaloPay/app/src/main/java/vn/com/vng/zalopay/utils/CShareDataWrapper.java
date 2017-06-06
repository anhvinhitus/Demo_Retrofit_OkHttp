package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Subscription;
import rx.subjects.PublishSubject;
import timber.log.Timber;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.RefreshBankAccountEvent;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;

/**
 * Created by longlv on 2/8/17.
 * Wrapper CShareData of PaymentSDK.
 */

public class CShareDataWrapper {

    private static PublishSubject<List<ZPCard>> mCardSupportSubject = PublishSubject.create();

    public static Subscription getCardSupportList(UserInfo userInfo, DefaultSubscriber<List<ZPCard>> subscriber) {
        Timber.d("Call get support banks from PaymentSDK [%s]", subscriber);
        Subscription subscription = mCardSupportSubject.subscribe(subscriber);

        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(new IGetCardSupportListListener() {
            @Override
            public void onComplete(ArrayList<ZPCard> cardSupportArrayList) {
                Timber.d("Get support banks from PaymentSDK completed [%s]", cardSupportArrayList);
                mCardSupportSubject.onNext(cardSupportArrayList);
                subscriber.onNext(cardSupportArrayList);
            }

            @Override
            public void onProcess() {

            }

            @Override
            public void onError(String pErrorMess) {
                Timber.d("Get support banks from PaymentSDK error [%s]", pErrorMess);
                mCardSupportSubject.onError(new Throwable(pErrorMess));
                subscriber.onError(new Throwable(pErrorMess));
            }

            @Override
            public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
                // TODO: 4/27/17 - longlv: hiện tại đã ko còn dùng, chờ PaymentSDK remove
            }
        });
        return subscription;
    }

    private static List<DMappedCard> detectCCCard(List<DMappedCard> dMappedCards, User user) {
        if (Lists.isEmptyOrNull(dMappedCards)) {
            return Collections.emptyList();
        }
        for (DMappedCard bankCard : dMappedCards) {
            if (Constants.CCCode.equalsIgnoreCase(bankCard.bankcode)) {
                bankCard.bankcode = BankUtils.detectCCCard(bankCard.getFirstNumber(), user);
            }
        }
        return dMappedCards;
    }

    public static List<DMappedCard> getMappedCardList(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return detectCCCard(CShareData.getInstance().getMappedCardList(user.zaloPayId), user);
    }

    public static List<DBankAccount> getMapBankAccountList(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return CShareData.getInstance().getMapBankAccountList(user.zaloPayId);
    }

    public static String detectCardType(UserInfo userInfo, String first6CardNo) {
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

    public static void notifyPromotionEventToSdk(Object... pObject) {
        CShareData.getInstance().notifyPromotionEvent(pObject);
    }

    public static void dispose() {
        CShareData.dispose();
    }
}
