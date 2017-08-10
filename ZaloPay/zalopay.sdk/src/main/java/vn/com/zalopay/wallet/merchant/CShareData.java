package vn.com.zalopay.wallet.merchant;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.text.TextUtils;

import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.staticconfig.DConfigFromServer;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.CardTypeUtils;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channellist.ChannelListActivity;
import vn.com.zalopay.wallet.ui.channellist.ResultPaymentFragment;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.AccountLinkWorkFlow;
import vn.zalopay.promotion.IPromotionResult;

/***
 * class sharing data to app
 */
public class CShareData extends SingletonBase {
    protected static CShareData _object;
    protected static DConfigFromServer mConfigFromServer;

    public CShareData() {
        super();
    }

    public static CShareData getInstance() {
        if (CShareData._object == null)
            CShareData._object = new CShareData();

        return CShareData._object;
    }

    /***
     * app need to call this to release all resource after not use anymore
     */
    public static void dispose() {
        SingletonLifeCircleManager.disposeMerchant();
        Timber.d("dispose merchant");
    }

    /***
     * load config from json file
     */
    public static DConfigFromServer loadConfigBundle() {
        if (mConfigFromServer == null || mConfigFromServer.CCIdentifier == null) {
            try {
                String json = ResourceManager.loadJsonConfig();
                mConfigFromServer = (new DConfigFromServer()).fromJsonString(json);
            } catch (Exception e) {
                Timber.d(e, "Exception loadConfigBundle");
            }
        }
        return mConfigFromServer;
    }

    public void notifyPromotionEvent(Object... pObjects) {
        //this runs on the UI thread
        new Handler(Looper.getMainLooper()).post(() -> sendNotifyPromotionEventToSDK(pObjects));
    }

    /***
     * push notify to SDK to finish flow vcb account link
     * @param pObjects (ZPWNotication, IReloadMapInfoListener)
     */
    public void notifyLinkBankAccountFinish(Object... pObjects) {
        new Handler(Looper.getMainLooper()).post(() -> sendNotifyBankAccountFinishToSDK(pObjects));
    }

    /***
     * app push notify about finish transaction to workout for issue when
     * 1. user waiting for processing loading -> stop loading and show success screen
     * 2. user in fail screen by networking -> reload to success screen
     * app can call this  in main thread or background thread so need to check for switch to main
     * thread
     */
    public void notifyTransactionFinish(Object... pObject) {
        new Handler(Looper.getMainLooper()).post(() -> sendNotifyTransactionFinishIntoSDK(pObject));
    }

    @UiThread
    private void sendNotifyBankAccountFinishToSDK(Object... pObject) {
        ChannelActivity activity = BaseActivity.getChannelActivity();
        if (activity != null && !activity.isFinishing() && activity.getWorkFlow() instanceof AccountLinkWorkFlow) {
            ((AccountLinkWorkFlow) activity.getWorkFlow()).onEvent(EEventType.ON_NOTIFY_BANKACCOUNT, pObject);
        } else {
            //user link/unlink on vcb website, then zalopay server notify to app -> sdk (use not in sdk)
            try {
                if (pObject.length >= 3) {
                    UserInfo userInfo = (UserInfo) pObject[2];
                    IReloadMapInfoListener mIReloadMapInfoListener = (IReloadMapInfoListener) pObject[1];
                    String appVersion = SdkUtils.getAppVersion(SDKApplication.getContext());
                    SDKApplication.getApplicationComponent()
                            .linkInteractor()
                            .getBankAccounts(userInfo.zalopay_userid, userInfo.accesstoken, true, appVersion)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new LoadBankListSubscriber(mIReloadMapInfoListener));
                }
            } catch (Exception ex) {
                Timber.w(ex);
            }
        }
    }

    @UiThread
    private void sendNotifyTransactionFinishIntoSDK(Object... pObject) {
        try {
            SdkSuccessTransEvent successTransEvent = getSuccessTransEvent(pObject);
            if (successTransEvent != null) {
                SDKApplication
                        .getApplicationComponent()
                        .eventBus()
                        .post(successTransEvent);
                Timber.d("send event notification into event bus");
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private SdkSuccessTransEvent getSuccessTransEvent(Object... pObject) throws Exception {
        if (pObject == null) {
            return null;
        }
        SdkSuccessTransEvent successTransEvent = new SdkSuccessTransEvent();
        if (pObject.length >= 1) {
            successTransEvent.notification_type = (long) pObject[0];
        }
        if (pObject.length >= 2) {
            successTransEvent.transid = (long) pObject[1];
        }
        if (pObject.length >= 3) {
            successTransEvent.trans_time = (long) pObject[2];
        }
        return successTransEvent;
    }

    @UiThread
    private void sendNotifyPromotionEventToSDK(Object... pObject) {
        try {
            Activity sdkCurrentActivity = BaseActivity.getCurrentActivity();
            String transactionID = null;
            boolean successPayment = false;
            //flow 2 activity channellist -> channel
            if ((sdkCurrentActivity instanceof ChannelActivity)
                    && !sdkCurrentActivity.isFinishing()
                    && ((ChannelActivity) sdkCurrentActivity).getWorkFlow() != null) {
                AbstractWorkFlow workFlow = ((ChannelActivity) sdkCurrentActivity).getWorkFlow();
                transactionID = workFlow.getTransactionID();
                successPayment = workFlow.isTransactionSuccess();
            }
            //flow 1 activity channellist
            if ((sdkCurrentActivity instanceof ChannelListActivity)
                    && !sdkCurrentActivity.isFinishing()) {
                android.support.v4.app.Fragment currentFragment = ((ChannelListActivity) sdkCurrentActivity).getActiveFragment();
                if (currentFragment instanceof ResultPaymentFragment) {
                    transactionID = ((ResultPaymentFragment) currentFragment).getSuccessTransId();
                    successPayment = !TextUtils.isEmpty(transactionID);
                }
            }

            if (successPayment && !TextUtils.isEmpty(transactionID)) {
                SdkPromotion promotion = SdkPromotion.shared();
                if (promotion.showing()) {
                    Timber.d("There are a showing promotion popup - skip render again");
                    return;
                }
                promotion
                        .plant(sdkCurrentActivity)
                        .setTransId(transactionID);
                promotion.handlePromotion(pObject);
                return;
            }

            if (pObject != null && pObject.length >= 2 && pObject[1] instanceof IPromotionResult) {
                IPromotionResult promotionResult = (IPromotionResult) pObject[1];
                promotionResult.onReceiverNotAvailable();//callback again to notify that sdk not available
                return;
            }
            Timber.d("skip post notification promotion event because user quit sdk and listener is null");
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    /***
     * support app detect type of visa card.
     */
    public String detectCardType(String pCardNumber) {
        loadConfigBundle();
        if (mConfigFromServer != null) {
            CreditCardDetector cardCheck = new CreditCardDetector(mConfigFromServer.CCIdentifier);
            cardCheck.detectOnSync(pCardNumber);
            return CardTypeUtils.fromBankCode(cardCheck.getCodeBankForVerifyCC());
        } else {
            return CardType.UNDEFINE;
        }
    }

    private class LoadBankListSubscriber extends DefaultSubscriber<Boolean> {
        IReloadMapInfoListener mIReloadMapInfoListener;

        public LoadBankListSubscriber(IReloadMapInfoListener iReloadMapInfoListener) {
            mIReloadMapInfoListener = iReloadMapInfoListener;
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onNext(Boolean aBoolean) {
            Timber.d("reload bank account finish");
            if (mIReloadMapInfoListener == null) {
                return;
            }
            mIReloadMapInfoListener.onComplete(null);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("reload bank account error %s", e);
            if (mIReloadMapInfoListener == null) {
                return;
            }
            mIReloadMapInfoListener.onError(e.getMessage());
        }
    }
}
