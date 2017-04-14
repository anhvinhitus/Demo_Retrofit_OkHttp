package vn.com.zalopay.wallet.merchant;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observer;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.staticconfig.DConfigFromServer;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;
import vn.com.zalopay.wallet.merchant.listener.IDetectCardTypeListener;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;
import vn.com.zalopay.wallet.merchant.strategy.IMerchantTask;
import vn.com.zalopay.wallet.merchant.strategy.TaskDetectCardType;
import vn.com.zalopay.wallet.merchant.strategy.TaskGetCardSupportList;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

/***
 * class sharing data to app
 */
public class CShareData extends SingletonBase {

    protected static CShareData _object;
    protected static DConfigFromServer mConfigFromServer;
    protected IMerchantTask mMerchantTask;
    protected IGetWithDrawBankList mGetWithDrawBankList;
    /***
     * load resource static listener
     */
    protected PlatformInfoLoader.onCheckResourceStaticListener checkResourceStaticListener = new PlatformInfoLoader.onCheckResourceStaticListener() {
        @Override
        public void onCheckResourceStaticComplete(boolean isSuccess, String pError) {
            if (isSuccess && mMerchantTask != null) {
                mMerchantTask.onPrepareTaskComplete();
            } else {
                if (mMerchantTask != null)
                    mMerchantTask.onTaskError(null);
            }
            Log.d(this, "===onCheckResourceStaticComplete===" + "===success=" + isSuccess + "===pError=" + pError);
        }

        /*@Override
        public void onCheckResourceStaticInProgress() {
            if (mMerchantTask != null)
                mMerchantTask.onTaskInProcess();
        }*/

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            if (mMerchantTask != null && pForceUpdate) {
                mMerchantTask.onUpVersion(pForceUpdate, pVersion, pMessage);
            }
            Log.d(this, "===onUpVersion===pForceUpdate=" + pForceUpdate + "===pVersion=" + pVersion + "===pMessage=" + pMessage);
        }
    };
    protected ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
        }

        @Override
        public void onComplete() {
            List<BankConfig> bankConfigList = new ArrayList<>();

            if (BankLoader.mapBank != null) {
                Iterator it = BankLoader.mapBank.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    try {
                        BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(String.valueOf(pair.getValue())), BankConfig.class);

                        if (bankConfig != null && !bankConfigList.contains(bankConfig) && bankConfig.isAllowWithDraw()) {
                            bankConfigList.add(bankConfig);
                        }
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                }
            }

            if (mGetWithDrawBankList != null) {
                mGetWithDrawBankList.onComplete(bankConfigList);
            }

        }

        @Override
        public void onError(String pMessage) {
            Log.e(this, pMessage);
            if (mGetWithDrawBankList != null) {
                mGetWithDrawBankList.onError("Mạng không ổn định, không tải được danh sách ngân hàng.\n Vui lòng thử lại!");
            }
        }
    };

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
        Log.d("CShareData", "dispose merchant");
    }

    public static DConfigFromServer getConfigResource() {
        return CShareData.mConfigFromServer;
    }

    /***
     * load config from json file
     * @return
     */
    public static DConfigFromServer loadConfigBundle() {
        if (mConfigFromServer == null || mConfigFromServer.CCIdentifier == null) {
            try {
                String json = ResourceManager.loadResourceFile();
                mConfigFromServer = (new DConfigFromServer()).fromJsonString(json);
            } catch (Exception e) {
                Log.e("===loadConfigBundle===", e);
            }
        }
        return mConfigFromServer;
    }

    /***
     * push notify to SDK to finish flow vcb account link
     *
     * @param pObjects (ZPWNotication, IReloadMapInfoListener)
     */
    public void notifyLinkBankAccountFinish(Object... pObjects) {
        //user in sdk now.
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                Looper.getMainLooper().isCurrentThread() : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isUiThread) {
            Log.d(this, "notification coming from background, switching thread to main thread...");
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
     * 2. user in fail screen by networking or anhything -> reload to success screen
     * app can call this  in main thread or background thread so need to check for switch to main
     * thread
     * @param pObject
     */
    public void notifyTransactionFinish(Object... pObject) {
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                Looper.getMainLooper().isCurrentThread() : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isUiThread) {
            Log.d(this, "notification coming from background, switching thread to main thread...");
            new Handler(Looper.getMainLooper()).post(() -> {
                //this runs on the UI thread
                sendNotifyTransactionFinishToAdapter(pObject);
            });
        } else {
            sendNotifyTransactionFinishToAdapter(pObject);
        }
    }

    protected void sendNotifyBankAccountFinishToAdapter(Object... pObject) {
        Log.d(this, GsonUtils.toJsonString(pObject));
        if (BasePaymentActivity.getPaymentChannelActivity() instanceof PaymentChannelActivity &&
                ((PaymentChannelActivity) BasePaymentActivity.getPaymentChannelActivity()).getAdapter() instanceof AdapterLinkAcc) {
            ((PaymentChannelActivity) BasePaymentActivity.getPaymentChannelActivity()).getAdapter().onEvent(EEventType.ON_NOTIFY_BANKACCOUNT, pObject);
        } else {
            //user link/unlink on vcb website, then zalopay server notify to app -> sdk (use not in sdk)
            try {
                IReloadMapInfoListener reloadMapInfoListener = (IReloadMapInfoListener) pObject[1];
                BankAccountHelper.loadBankAccountList(true);
            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
    }

    protected void sendNotifyTransactionFinishToAdapter(Object... pObject) {
        //user in sdk now.
        Log.d(this, GsonUtils.toJsonString(pObject));
        if (BasePaymentActivity.getPaymentChannelActivity() instanceof PaymentChannelActivity &&
                ((PaymentChannelActivity) BasePaymentActivity.getPaymentChannelActivity()).getAdapter() != null) {
            ((PaymentChannelActivity) BasePaymentActivity.getPaymentChannelActivity()).getAdapter().onEvent(EEventType.ON_NOTIFY_TRANSACTION_FINISH, pObject);
        } else {
            //user quit sdk
            Log.d(this, "user is not in sdk, skip process now...");
        }
    }

    public CShareData setUserInfo(UserInfo pUserInfo) {
        GlobalData.setUserInfo(pUserInfo);
        return this;
    }

    protected void checkStaticResource() {
        //check static resource whether ready or not
        try {
            PlatformInfoLoader.getInstance().checkStaticResource();
        } catch (Exception e) {
            if (checkResourceStaticListener != null) {
                checkResourceStaticListener.onCheckResourceStaticComplete(false, e != null ? e.getMessage() : null);
            }
        }
    }

    /***
     * get card support list
     * app use this function to show bank list icon before
     * user go to the link card channel
     */
    public void getCardSupportList(IGetCardSupportListListener pListener) {
        mMerchantTask = new TaskGetCardSupportList();
        mMerchantTask.setTaskListener(pListener);
        mMerchantTask.onPrepareTaskComplete();
    }

    /***
     * 1 zalopay id map to 1 vietcombank's account only
     * check this user has 1 vietcombank account is linked
     *
     * @param pUserId
     * @return
     */
    public boolean hasVietcomBank(String pUserId) {
        try {
            return BankAccountHelper.hasBankAccountOnCache(pUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return false;
    }

    /***
     * get map card list of user
     *
     * @param pUserID
     * @return
     */
    public List<DMappedCard> getMappedCardList(String pUserID) {
        try {
            return SharedPreferencesManager.getInstance().getMapCardList(pUserID);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return null;
    }

    public List<DBankAccount> getMapBankAccountList(String pUserID) {
        try {
            return SharedPreferencesManager.getInstance().getBankAccountList(pUserID);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return null;
    }

    public long getMinTranferValue() {
        try {
            return SharedPreferencesManager.getInstance().getMinValueChannel(ETransactionType.WALLET_TRANSFER.toString());
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMaxTranferValue() {
        try {
            return SharedPreferencesManager.getInstance().getMaxValueChannel(ETransactionType.WALLET_TRANSFER.toString());
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMinDepositValue() {
        try {
            return SharedPreferencesManager.getInstance().getMinValueChannel(ETransactionType.TOPUP.toString());
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMaxDepositValue() {
        try {
            return SharedPreferencesManager.getInstance().getMaxValueChannel(ETransactionType.TOPUP.toString());
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMinWithDrawValue() {
        try {
            return SharedPreferencesManager.getInstance().getMinValueChannel(ETransactionType.WITHDRAW.toString());
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public long getMaxWithDrawValue() {
        try {
            return SharedPreferencesManager.getInstance().getMaxValueChannel(ETransactionType.WITHDRAW.toString());
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return 0;
    }

    public WDMaintenance getWithdrawMaintenance() {
        try {
            String maintenanceOb = SharedPreferencesManager.getInstance().getMaintenanceWithDraw();

            if (!TextUtils.isEmpty(maintenanceOb)) {
                return GsonUtils.fromJsonString(maintenanceOb, WDMaintenance.class);
            }

        } catch (Exception ex) {
            Log.e(this, ex);
        }

        return new WDMaintenance();
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

    public void getWithDrawBankList(IGetWithDrawBankList pListener) {
        this.mGetWithDrawBankList = pListener;
        BankLoader.loadBankList(mLoadBankListListener);
    }

    /***
     * return banner list for top menu on app
     * @return
     */
    public List<DBanner> getBannerList() {
        try {
            List<DBanner> bannerList = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBannerList(), new TypeToken<List<DBanner>>() {
            }.getType());
            return bannerList;
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    /***
     * app ids approved from server.
     * app use this to show apps
     *
     * @return
     */
    public List<Integer> getApproveInsideApps() {
        try {
            List<Integer> appListID = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getApproveInsideApps(), new TypeToken<List<Integer>>() {
            }.getType());

            return appListID;
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    /***
     * Platform info expire time,unix time to exprired time (in milisecond)
     * After this expire time, sdk or app need hit to server again
     * if api's response isupdateinfo=true,then sdk need to update cache
     *
     * @return
     */
    public long getPlatformInfoExpiredTime() {
        try {
            return SharedPreferencesManager.getInstance().getPlatformInfoExpriedTimeDuration();

        } catch (Exception ex) {
            Log.e(this, ex);
        }

        return 0;
    }

    /***
     * call api get card info again
     * app use this function in get notify remove map card
     *
     * @param pParams
     * @param pReloadMapCardInfoListener
     */
    public void reloadMapCardList(ZPWRemoveMapCardParams pParams, final IReloadMapInfoListener pReloadMapCardInfoListener) {
        try {
            //remove card on cache
            if (pParams != null && pParams.mapCard != null) {
                SharedPreferencesManager.getInstance().removeMappedCard(pParams.userID + Constants.COMMA + pParams.mapCard.getCardKey());
            }
            UserInfo userInfo = new UserInfo();
            userInfo.zaloPayUserId = pParams.userID;
            userInfo.accessToken = pParams.accessToken;
            MapCardHelper.loadMapCardList(true, userInfo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse response) {
                            if (response instanceof CardInfoListResponse && response.returncode == 1) {
                                pReloadMapCardInfoListener.onComplete(((CardInfoListResponse) response).cardinfos);
                            } else {
                                pReloadMapCardInfoListener.onError(response.getMessage());
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            pReloadMapCardInfoListener.onError(null);
                        }
                    });
            BankAccountHelper.loadBankAccountList(true)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<BaseResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(BaseResponse baseResponse) {

                        }
                    });
            Log.d(this, "reload map card and map bankaccount list from notification");
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    /***
     * support app detect type of visa card.
     * @param pCardNumber
     * @return type card
     */
    public ECardType detectCardType(String pCardNumber) {
        loadConfigBundle();

        if (mConfigFromServer != null) {
            CreditCardCheck cardCheck = new CreditCardCheck(mConfigFromServer.CCIdentifier);
            cardCheck.detectOnSync(pCardNumber);

            return ECardType.fromString(cardCheck.getCodeBankForVerify());
        } else {
            return ECardType.UNDEFINE;
        }
    }

    /***
     * detect type of visa card.
     * use this for sure that nessesary resource all always is downloaded before detecting
     * @param pCardNumber
     * @param pDetectCardTypeListener
     */
    public void detectCardType(String pCardNumber, IDetectCardTypeListener pDetectCardTypeListener) {
        loadConfigBundle();

        if (mConfigFromServer != null) {
            CreditCardCheck cardCheck = new CreditCardCheck(mConfigFromServer.CCIdentifier);
            cardCheck.detectOnSync(pCardNumber);

            ECardType eCardType = ECardType.fromString(cardCheck.getCodeBankForVerify());

            if (pDetectCardTypeListener != null) {
                pDetectCardTypeListener.onComplete(eCardType);
            }
        } else {
            mMerchantTask = new TaskDetectCardType(pCardNumber);
            mMerchantTask.setTaskListener(pDetectCardTypeListener);

            checkStaticResource();
        }
    }

    /***
     * path to resource folder
     *
     * @return
     * @throws Exception
     */
    public String getUnzipFolderPath() throws Exception {
        return SharedPreferencesManager.getInstance().getUnzipPath();
    }

}
