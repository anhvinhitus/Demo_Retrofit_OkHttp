package vn.com.zalopay.wallet.business.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.TransactionType;

public class SharedPreferencesManager extends SingletonBase {
    private static final String SHARE_PREFERENCES_NAME = "ZALO_PAY_CACHED";
    private static SharedPreferencesManager mSharePreferencesManager = null;
    private SharedPreferences mCommonSharedPreferences = null;
    private WeakReference<Context> mContext;

    public SharedPreferencesManager() throws Exception {
        super();
        if (GlobalData.getAppContext() == null)
            throw new Exception("Truy cập không còn hợp lệ");
        mContext = new WeakReference<>(GlobalData.getAppContext());
    }

    public SharedPreferencesManager(Context pContext) {
        super();
        mContext = new WeakReference<>(pContext);
    }

    public static synchronized SharedPreferencesManager getInstance() throws Exception {
        if (mSharePreferencesManager == null) {
            mSharePreferencesManager = new SharedPreferencesManager();
        }
        return mSharePreferencesManager;
    }

    public static synchronized SharedPreferencesManager shared(Context pContext) {
        if (mSharePreferencesManager == null) {
            mSharePreferencesManager = new SharedPreferencesManager(pContext);
        }
        return mSharePreferencesManager;
    }

    public synchronized SharedPreferences getSharedPreferences() {
        if (mContext == null || mContext.get() == null) {
            Log.d(this, "mContext is null");
            return null;
        }
        if (mCommonSharedPreferences != null) {
            return mCommonSharedPreferences;
        }
        mCommonSharedPreferences = mContext.get().getSharedPreferences(SHARE_PREFERENCES_NAME, 0);
        return mCommonSharedPreferences;
    }

    private String getString(String pKey) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null)
            return sharedPreferences.getString(pKey, null);
        return null;
    }

    public boolean setString(String pKey, String pValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences != null && sharedPreferences.edit().putString(pKey, pValue).commit();
    }

    private long getLong(String pKey) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null)
            return sharedPreferences.getLong(pKey, 0);
        return 0;
    }

    public boolean setLong(String pKey, long pValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences != null && sharedPreferences.edit().putLong(pKey, pValue).commit();
    }

    private int getInt(String pKey) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(pKey, Integer.MIN_VALUE);
        }
        return Integer.MIN_VALUE;
    }

    public boolean setInt(String pKey, int pValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();

        return sharedPreferences != null && sharedPreferences.edit().putInt(pKey, pValue).commit();
    }

    private boolean getBoolean(String pKey, boolean defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(pKey, defaultValue);
        }
        return defaultValue;
    }

    public boolean setBoolean(String pKey, boolean pValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();

        return sharedPreferences != null && sharedPreferences.edit().putBoolean(pKey, pValue).commit();

    }

    public String pickCachedCardNumber() {
        String cardNumber = getString(mContext.get().getResources().getString(R.string.zpw_cache_card_for_show_inlinkcard));
        if (!TextUtils.isEmpty(cardNumber)) {
            setCachedCardNumber(null);
        }
        return cardNumber;
    }

    public boolean setCachedCardNumber(String pCardNumber) {
        return setString(mContext.get().getResources().getString(R.string.zpw_cache_card_for_show_inlinkcard), pCardNumber);
    }

    /***
     * approve inside app
     * @return
     */
    public String getApproveInsideApps() {

        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_approve_inside_app));
    }

    public boolean setApproveInsideApps(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_approve_inside_app), pValue);
    }

    /***
     * banner list,used by merchant
     * @return
     */
    public String getBannerList() {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_banner_list));
    }

    public boolean setBannerList(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_banner_list), pValue);
    }

    /***
     * is maintenance withdraw
     */
    public boolean setMaintenanceWithDraw(String pMessage) {
        return setString(mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_maintenance_withdraw), pMessage);
    }

    public String getMaintenanceWithDraw() {
        return getString(mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_maintenance_withdraw));
    }

    /***
     * on/off deposite
     * @param pEnableDeposite
     * @return
     */
    public boolean setEnableDeposite(boolean pEnableDeposite) {
        return setBoolean(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_enable_deposite), pEnableDeposite);
    }

    public boolean getEnableDeposite() {
        return getBoolean(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_enable_deposite), true);
    }

    /****
     * platform info api in previous request time.
     * @return
     */
    public long getPlatformInfoExpriedTime() {
        return getLong(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_expired_time));
    }

    public boolean setPlatformInfoExpriedTime(long pValue) {
        return setLong(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_expired_time), pValue);
    }

    /****
     * platform info api in previous request time duration(ms from server)
     * @return
     */
    public long getPlatformInfoExpriedTimeDuration() {
        return getLong(mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_expired_time_duration));
    }

    public boolean setPlatformInfoExpriedTimeDuration(long pValue) {
        return setLong(mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_expired_time_duration), pValue);
    }

    public boolean setExpiredTimeAppChannel(String pAppID, long pExpiredTime) {
        return setLong(mContext.get().getResources().getString(R.string.zpw_app_info_map_channel_expired_time) + pAppID, pExpiredTime);
    }

    public long getExpiredTimeAppChannel(String pAppID) {
        return getLong(mContext.get().getResources().getString(R.string.zpw_app_info_map_channel_expired_time) + pAppID);
    }

    public boolean setExpiredBankList(long pExpiredTime) {
        return setLong(mContext.get().getResources().getString(R.string.zpw_banklist_expired_time), pExpiredTime);
    }

    public boolean setCheckSumBankList(String pCheckSum) {
        return setString(mContext.get().getResources().getString(R.string.zpw_banklist_checksum), pCheckSum);
    }

    public long getExpiredBankList() {
        return getLong(mContext.get().getResources().getString(R.string.zpw_banklist_expired_time));
    }

    public String getCheckSumBankList() {
        return getString(mContext.get().getResources().getString(R.string.zpw_banklist_checksum));
    }

    public boolean setBankConfig(String pKey, String pBankConfig) {
        return setString(mContext.get().getResources().getString(R.string.zpw_bankconfig) + pKey, pBankConfig);
    }

    public String getBankConfig(String pKey) {
        return getString(mContext.get().getResources().getString(R.string.zpw_bankconfig) + pKey);
    }

    public boolean setBankConfigMap(String pBankMap) {
        return setString(mContext.get().getResources().getString(R.string.zpw_bankmap), pBankMap);
    }

    public String getBankMap() {
        return getString(mContext.get().getResources().getString(R.string.zpw_bankmap));
    }

    public boolean setCheckSumAppChannel(String pAppID, String pCheckSum) {
        return setString(mContext.get().getResources().getString(R.string.zpw_app_info_map_channel_checksum) + pAppID, pCheckSum);
    }

    public String getCheckSumAppChannel(String pAppID) {
        return getString(mContext.get().getResources().getString(R.string.zpw_app_info_map_channel_checksum) + pAppID);
    }

    //cache url download resource
    public String getResourceDownloadUrl() {
        return getString(mContext.get().getResources().getString(R.string.zpw_resource_url));
    }

    public boolean setResourceDownloadUrl(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zpw_resource_url), pValue);
    }

    //current user id.
    public String getCurrentUserID() {
        return getString(mContext.get().getResources().getString(R.string.zpw_current_user_id));
    }

    public boolean setCurrentUserID(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zpw_current_user_id), pValue);
    }

    public String getChecksumSDKversion() {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_sdk_ver));
    }

    public boolean setChecksumSDKversion(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_sdk_ver), pValue);
    }

    public String getChecksumSDK() {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_sdk_checksum));
    }

    public boolean setChecksumSDK(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_sdk_checksum), pValue);
    }

    public String getBankAccountCheckSum() {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_bankaccount_checksum));
    }

    public boolean setBankAccountCheckSum(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_bankaccount_checksum), pValue);
    }

    public String getCardInfoCheckSum() {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_cardinfo_checksum));
    }

    public boolean setCardInfoCheckSum(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_cardinfo_checksum), pValue);
    }

    public String getUnzipPath() throws Exception {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_unzip_path));
    }

    public boolean setUnzipPath(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_unzip_path), pValue);
    }

    public String getResourceVersion() {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_res_ver));
    }

    public boolean setResourceVersion(String pValue) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_res_ver), pValue);
    }

    public boolean setMinValueChannel(String pKey, long pValue) {
        return setLong(pKey + "__MIN", pValue);
    }

    public long getMinValueChannel(String pKey) {
        return getLong(pKey + "__MIN");
    }

    public boolean setMaxValueChannel(String pKey, long pValue) {
        return setLong(pKey + "__MAX", pValue);
    }

    public long getMaxValueChannel(String pKey) {
        return getLong(pKey + "__MAX");
    }

    /***
     * save map card
     */
    public boolean setMapCard(String pUserId, String pKey, String pConfig) {
        try {
            return setString(pUserId + Constants.COMMA + pKey, pConfig);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public String getMapCardByKey(String pUserID, String pKey) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pUserID).append(Constants.COMMA).append(pKey);
        return getString(stringBuilder.toString());
    }

    public boolean setBankAccountKeyList(String pKey, String pBankAccountKeyList) {
        return setString(pKey + mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_bank_account_list), pBankAccountKeyList);
    }

    public String getBankAccountKeyList(String pKey) {
        try {
            return getString(pKey + mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_bank_account_list));
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    /***
     * set map card list
     */
    public boolean setMapCardList(String pKey, String pMappedCardKeyList) {
        return setString(pKey + mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_mapped_card_list), pMappedCardKeyList);
    }

    public String getMapCardKeyList(String pKey) {
        return getString(pKey + mContext.get().getResources().getString(R.string.zpw_conf_gwinfo_mapped_card_list));
    }

    public boolean removeMappedCard(String pKey) {
        return setString(pKey, "");
    }

    /***
     * get map card list by user id
     * @param pUserID
     * @return
     */
    public List<DMappedCard> getMapCardList(String pUserID) {
        List<DMappedCard> mappedCardList = new ArrayList<>();

        String keyList = getMapCardKeyList(pUserID);

        if (!TextUtils.isEmpty(keyList)) {
            for (String key : keyList.split(Constants.COMMA)) {
                String strMappedCard = getMapCardByKey(pUserID, key);

                if (!TextUtils.isEmpty(strMappedCard)) {
                    DMappedCard mappedCard = GsonUtils.fromJsonString(strMappedCard, DMappedCard.class);

                    if (mappedCard != null)
                        mappedCardList.add(mappedCard);
                }
            }
        }

        return mappedCardList;
    }

    public List<DBankAccount> getBankAccountList(String pUserID) {
        List<DBankAccount> bankAccountList = new ArrayList<>();

        String keyList = getBankAccountKeyList(pUserID);

        if (!TextUtils.isEmpty(keyList)) {
            for (String key : keyList.split(Constants.COMMA)) {
                String strMappedCard = getMapCardByKey(pUserID, key);

                if (!TextUtils.isEmpty(strMappedCard)) {
                    DBankAccount bankAccount = GsonUtils.fromJsonString(strMappedCard, DBankAccount.class);

                    if (bankAccount != null) {
                        bankAccountList.add(bankAccount);
                    }
                }
            }
        }

        return bankAccountList;
    }

    /***
     * clear map card list on cache
     * @param pUserId
     * @return
     */
    public boolean resetMapCardListOnCache(String pUserId) {
        //remove all card info
        String keyList = getMapCardKeyList(pUserId);

        if (!TextUtils.isEmpty(keyList)) {
            for (String key : keyList.split(Constants.COMMA)) {
                setMapCard(pUserId, key, null);
            }
        }

        //remove card id list
        setMapCardList(pUserId, null);

        return true;
    }

    /***
     * clear bank list map on cache
     * @param pUserId
     * @return
     */
    public boolean resetBankListOnCache(String pUserId) {
        String keyList = getBankAccountKeyList(pUserId);
        if (!TextUtils.isEmpty(keyList)) {
            for (String key : keyList.split(Constants.COMMA)) {
                setMapCard(pUserId, key, null);
            }
        }
        //remove bank id list
        setBankAccountKeyList(pUserId, null);
        return true;
    }

    public boolean setApp(String pId, String pConfig) {
        return setString(mContext.get().getResources().getString(R.string.zpw_config_platform_info_app) + pId, pConfig);
    }

    public String getAppById(String pID) {
        return getString(mContext.get().getResources().getString(R.string.zpw_config_platform_info_app) + pID);
    }

    public boolean setPmcConfig(String keyChannelID, String pConfig) {
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_channel_prefix) + keyChannelID, pConfig);
    }

    public String getPmcConfigByPmcID(long pAppId, @TransactionType int pTranstype, int pPmcID, String pBankCode) {
        StringBuilder channelIDkey = new StringBuilder();
        channelIDkey.append(MiniPmcTransType.getPmcKey(pAppId, pTranstype, pPmcID));
        if (!TextUtils.isEmpty(pBankCode)) {
            channelIDkey.append(Constants.UNDERLINE).append(pBankCode);
        }
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_channel_prefix) + channelIDkey.toString());
    }


    public String getPmcConfigByPmcKey(String pPmcKey) {
        return getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_channel_prefix) + pPmcKey);
    }

    public boolean setPmcConfigList(String pKey, ArrayList<String> pPmcIdList) {
        StringBuilder pmcIdList = new StringBuilder();
        for (int i = 0; i < pPmcIdList.size(); i++) {
            pmcIdList.append(pPmcIdList.get(i));
            if ((i + 1) < pPmcIdList.size()) {
                pmcIdList.append(Constants.COMMA);
            }
        }
        return setString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_channel_list) + pKey, pmcIdList.toString());
    }

    public ArrayList<String> getPmcConfigList(String pKey) {
        ArrayList<String> result = new ArrayList<>();
        String raw = getString(mContext.get().getResources().getString(R.string.zingpaysdk_conf_gwinfo_channel_list) + pKey);
        if (TextUtils.isEmpty(raw)) {
            return result;
        }
        try {
            for (String pmcId : raw.split(Constants.COMMA)) {
                result.add(String.valueOf(pmcId));
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return result;
    }

    public boolean setTranstypePmcCheckSum(String pKey, String pCheckSum) {
        return setString(mContext.get().getResources().getString(R.string.sdk_conf_gwinfo_transtype_list_checksum) + pKey, pCheckSum);
    }

    public String getTransypePmcCheckSum(String pKey) {
        return getString(mContext.get().getResources().getString(R.string.sdk_conf_gwinfo_transtype_list_checksum) + pKey);
    }

    public String getBankAccountChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        Log.d(this, "get cache bank account channel config, bankcode " + pBankCode);
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_bankaccount, pBankCode);
    }

    public String getATMChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        Log.d(this, "get cache atm channel config, bankcode " + pBankCode);
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_atm, pBankCode);
    }

    public String getCreditCardChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        Log.d(this, "get cache credit card channel config, bankcode " + pBankCode);
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_credit_card, pBankCode);
    }

    public String getZaloPayChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_zalopay, pBankCode);
    }
}
