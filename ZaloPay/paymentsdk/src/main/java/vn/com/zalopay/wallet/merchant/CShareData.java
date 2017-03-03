package vn.com.zalopay.wallet.merchant;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.GatewayLoader;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
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
import vn.com.zalopay.wallet.utils.Log;

/***
 * class sharing data to app
 */
public class CShareData extends SingletonBase {

    private static CShareData _object;

    private static DConfigFromServer mConfigFromServer;

    private IMerchantTask mMerchantTask;

    private IGetWithDrawBankList mGetWithDrawBankList;
    /***
     * load resource static listener
     */
    private GatewayLoader.onCheckResourceStaticListener checkResourceStaticListener = new GatewayLoader.onCheckResourceStaticListener() {
        @Override
        public void onCheckResourceStaticComplete(boolean isSuccess, String pError) {
            if (isSuccess && mMerchantTask != null) {
                mMerchantTask.onPrepareTaskComplete();
            } else {
                if (mMerchantTask != null)
                    mMerchantTask.onTaskError(null);
            }

            Log.d(this, "===onCheckResourceStaticComplete===" + "===isSuccess=" + isSuccess + "===pError=" + pError);

        }

        @Override
        public void onCheckResourceStaticInProgress() {
            if (mMerchantTask != null)
                mMerchantTask.onTaskInProcess();
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            if (mMerchantTask != null && pForceUpdate) {
                mMerchantTask.onUpVersion(pForceUpdate, pVersion, pMessage);
            }

            Log.d(this, "===onUpVersion===pForceUpdate=" + pForceUpdate + "===pVersion=" + pVersion + "===pMessage=" + pMessage);
        }
    };
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
        }

        @Override
        public void onComplete() {
            List<BankConfig> bankConfigList = new ArrayList<>();

            if (BankCardCheck.mBankMap != null) {
                Iterator it = BankCardCheck.mBankMap.entrySet().iterator();

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
        Log.d("CShareData", "prepare to dispose merchant");
        SingletonLifeCircleManager.disposeMerchant();
    }

    public static DConfigFromServer getConfigResource() {
        return CShareData.mConfigFromServer;
    }

    /***
     * load config from json file
     *
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
     * push notify to SDK
     *
     * @param pNotification
     */
    public void pushNotificationToSdk(ZPWNotification pNotification) {
        GlobalData.setNotification(pNotification);
    }

    public CShareData setUserInfo(UserInfo pUserInfo) {
        GlobalData.setUserInfo(pUserInfo);

        return this;
    }

    protected void checkStaticResource() {
        GatewayLoader.getInstance().setOnCheckResourceStaticListener(checkResourceStaticListener).checkStaticResource();
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
        //check resource statis first
        //checkStaticResource();
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

    /****
     * amount money for link card channel
     *
     * @return
     */
    public long getLinkCardValue() {
        try {
            return Long.parseLong(GlobalData.getStringResource(RS.string.zpw_conf_wallet_amount));

        } catch (Exception ex) {
            Log.e(this, ex);
        }

        return 20000;
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
     *
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
     *
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
            setUserInfo(userInfo);
            MapCardHelper.loadMapCardList(true, pReloadMapCardInfoListener);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public int getZaloChannelId() {
        int channelId = 0;

        try {
            channelId = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay));
        } catch (Exception e) {
            Log.e(this, e);
        }

        return channelId;
    }

    public int getATMChannelId() {
        int channelId = 0;

        try {
            channelId = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm));
        } catch (Exception e) {
            Log.e(this, e);
        }

        return channelId;
    }

    public int getCreditCardChannelId() {
        int channelId = 0;

        try {
            channelId = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card));
        } catch (Exception e) {
            Log.e(this, e);
        }

        return channelId;
    }

    /***
     * support app detect type of visa card.
     *
     * @param pCardNumber
     * @return type card
     */
    public ECardType detectCardType(String pCardNumber) {
        loadConfigBundle();

        if (mConfigFromServer != null) {
            CreditCardCheck cardCheck = new CreditCardCheck(mConfigFromServer.CCIdentifier);
            cardCheck.detectCard(pCardNumber);

            return ECardType.fromString(cardCheck.getCodeBankForVerify());
        } else {
            return ECardType.UNDEFINE;
        }
    }

    /***
     * detect type of visa card.
     * use this for sure that nessesary resource all always is downloaded before detecting
     *
     * @param pCardNumber
     * @param pDetectCardTypeListener
     */
    public void detectCardType(String pCardNumber, IDetectCardTypeListener pDetectCardTypeListener) {
        loadConfigBundle();

        if (mConfigFromServer != null) {
            CreditCardCheck cardCheck = new CreditCardCheck(mConfigFromServer.CCIdentifier);
            cardCheck.detectCard(pCardNumber);

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
