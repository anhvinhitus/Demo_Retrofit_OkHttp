package vn.com.zalopay.wallet.merchant;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.staticconfig.DConfigFromServer;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.CardTypeUtils;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
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

    public static synchronized CShareData getInstance() {
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
     * @return
     */
    public static DConfigFromServer loadConfigBundle() {
        if (mConfigFromServer == null || mConfigFromServer.CCIdentifier == null) {
            try {
                String json = ResourceManager.loadJsonConfig();
                mConfigFromServer = (new DConfigFromServer()).fromJsonString(json);
            } catch (Exception e) {
                Log.e("===loadConfigBundle===", e);
            }
        }
        return mConfigFromServer;
    }

    public void notifyPromotionEvent(Object... pObjects) {
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                Looper.getMainLooper().isCurrentThread() : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isUiThread) {
            Timber.d("notification promotion event coming from background, switching thread to main thread...");
            new Handler(Looper.getMainLooper()).post(() -> {
                //this runs on the UI thread
                sendNotifyPromotionEventToAdapter(pObjects);
            });
        } else {
            sendNotifyPromotionEventToAdapter(pObjects);
        }
    }

    /***
     * push notify to SDK to finish flow vcb account link
     * @param pObjects (ZPWNotication, IReloadMapInfoListener)
     */
    public void notifyLinkBankAccountFinish(Object... pObjects) {
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                Looper.getMainLooper().isCurrentThread() : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isUiThread) {
            Timber.d("notification coming from background, switching thread to main thread...");
            new Handler(Looper.getMainLooper()).post(() -> {
                //this runs on the UI thread
                sendNotifyBankAccountFinishToAdapter(pObjects);
            });
        } else {
            sendNotifyBankAccountFinishToAdapter(pObjects);
        }
    }

    /***
     * app push notify about finish transaction to workout for issue when
     * 1. user waiting for processing loading -> stop loading and show success screen
     * 2. user in fail screen by networking -> reload to success screen
     * app can call this  in main thread or background thread so need to check for switch to main
     * thread
     * @param pObject
     */
    public void notifyTransactionFinish(Object... pObject) {
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                Looper.getMainLooper().isCurrentThread() : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isUiThread) {
            Timber.d("notification coming from background, switching thread to main thread...");
            new Handler(Looper.getMainLooper()).post(() -> {
                //this runs on the UI thread
                sendNotifyTransactionFinishIntoSDK(pObject);
            });
        } else {
            sendNotifyTransactionFinishIntoSDK(pObject);
        }
    }

    private void sendNotifyBankAccountFinishToAdapter(Object... pObject) {
        Log.d(this, "start send notify finish link/unlink bank account into sdk", pObject);
        ChannelActivity activity = BaseActivity.getChannelActivity();
        if (activity != null && !activity.isFinishing() && activity.getAdapter() instanceof AdapterLinkAcc) {
            activity.getAdapter().onEvent(EEventType.ON_NOTIFY_BANKACCOUNT, pObject);
        } else {
            //user link/unlink on vcb website, then zalopay server notify to app -> sdk (use not in sdk)
            try {
                if (pObject.length >= 2) {
                    UserInfo userInfo = (UserInfo) pObject[1];
                    String appVersion = SdkUtils.getAppVersion(SDKApplication.getContext());
                    SDKApplication.getApplicationComponent()
                            .linkInteractor()
                            .getBankAccounts(userInfo.zalopay_userid, userInfo.accesstoken, true, appVersion)
                            .subscribe(aBoolean -> Timber.d("reload bank account finish"), throwable -> {
                                Timber.d("reload bank account error %s", throwable);
                            });
                }
            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
    }

    private void sendNotifyTransactionFinishIntoSDK(Object... pObject) {
        //user in sdk now.
        Log.d(this, "start send notify finish transaction into sdk", pObject);
        ChannelActivity activity = BaseActivity.getChannelActivity();
        if (activity != null && !activity.isFinishing() && activity.getAdapter() != null) {
            activity.getAdapter().onEvent(EEventType.ON_NOTIFY_TRANSACTION_FINISH, pObject);
        } else {
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
                Log.e(this, e);
            }
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

    private void sendNotifyPromotionEventToAdapter(Object... pObject) {
        ChannelActivity activity = BaseActivity.getChannelActivity();
        if (activity != null && !activity.isFinishing() && activity.getAdapter() != null) {
            activity.getAdapter().onEvent(EEventType.ON_PROMOTION, pObject);
        } else if (pObject[1] instanceof IPromotionResult) {
            IPromotionResult promotionResult = (IPromotionResult) pObject[1];
            promotionResult.onReceiverNotAvailable();//callback again to notify that sdk not available
        } else {
            Timber.d("skip post notification promotion event because user quit sdk");
        }
    }

    /***
     * get map card list of user
     *
     * @param pUserID
     * @return
     */
    public List<MapCard> getMappedCardList(String pUserID) {
        try {
            return SharedPreferencesManager.getInstance().getMapCardList(pUserID);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return null;
    }

    public List<BankAccount> getMapBankAccountList(String pUserID) {
        try {
            return SharedPreferencesManager.getInstance().getBankAccountList(pUserID);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return null;
    }

    public long getMinTranferValue() {
        try {
            return SharedPreferencesManager.getInstance().getMinValueChannel(String.valueOf(TransactionType.MONEY_TRANSFER));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMaxTranferValue() {
        try {
            return SharedPreferencesManager.getInstance().getMaxValueChannel(String.valueOf(TransactionType.MONEY_TRANSFER));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMinDepositValue() {
        try {
            return SharedPreferencesManager.getInstance().getMinValueChannel(String.valueOf(TransactionType.TOPUP));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMaxDepositValue() {
        try {
            return SharedPreferencesManager.getInstance().getMaxValueChannel(String.valueOf(TransactionType.TOPUP));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMinWithDrawValue() {
        try {
            return SharedPreferencesManager.getInstance().getMinValueChannel(String.valueOf(TransactionType.WITHDRAW));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMaxWithDrawValue() {
        try {
            return SharedPreferencesManager.getInstance().getMaxValueChannel(String.valueOf(TransactionType.WITHDRAW));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public Maintenance getWithdrawMaintenance() {
        try {
            String maintenanceOb = SharedPreferencesManager.getInstance().getMaintenanceWithDraw();
            if (!TextUtils.isEmpty(maintenanceOb)) {
                return GsonUtils.fromJsonString(maintenanceOb, Maintenance.class);
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return new Maintenance();
    }

    /****
     * show/hide deposite.
     * this get config from server.
     * @return true/false
     */
    public boolean isEnableDeposite() {
        try {
            return SharedPreferencesManager.getInstance().getEnableDeposite();
        } catch (Exception ex) {
            Log.e(this, ex);
        }

        return true;
    }

    /***
     * support app detect type of visa card.
     * @param pCardNumber
     * @return type card
     */
    public String detectCardType(String pCardNumber) {
        loadConfigBundle();
        if (mConfigFromServer != null) {
            CreditCardCheck cardCheck = new CreditCardCheck(mConfigFromServer.CCIdentifier);
            cardCheck.detectOnSync(pCardNumber);
            return CardTypeUtils.fromBankCode(cardCheck.getCodeBankForVerify());
        } else {
            return CardType.UNDEFINE;
        }
    }
}
