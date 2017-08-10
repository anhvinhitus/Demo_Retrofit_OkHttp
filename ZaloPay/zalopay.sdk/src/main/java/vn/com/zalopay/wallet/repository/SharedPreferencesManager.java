package vn.com.zalopay.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.SharePrefConstants;
import vn.com.zalopay.wallet.constants.TransactionType;

public class SharedPreferencesManager {
    private static final String SHARE_PREFERENCES_NAME = "ZALO_PAY_CACHED";
    private SharedPreferences sharedPreferences = null;

    public SharedPreferencesManager(Context pContext) {
        super();
        sharedPreferences = pContext.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String pKey) {
        return sharedPreferences.getString(pKey, null);
    }

    public boolean setString(String pKey, String pValue) {
        return sharedPreferences != null && sharedPreferences.edit().putString(pKey, pValue).commit();
    }

    private long getLong(String pKey) {
        return sharedPreferences.getLong(pKey, 0);
    }

    private boolean setLong(String pKey, long pValue) {
        return sharedPreferences != null && sharedPreferences.edit().putLong(pKey, pValue).commit();
    }

    private int getInt(String pKey) {
        return sharedPreferences.getInt(pKey, Integer.MIN_VALUE);
    }

    private boolean setInt(String pKey, int pValue) {
        return sharedPreferences != null && sharedPreferences.edit().putInt(pKey, pValue).commit();
    }

    private boolean getBoolean(String pKey, boolean defaultValue) {
        return sharedPreferences.getBoolean(pKey, defaultValue);
    }

    private boolean setBoolean(String pKey, boolean pValue) {
        return sharedPreferences != null && sharedPreferences.edit().putBoolean(pKey, pValue).commit();
    }

    public boolean setMaintenanceWithDraw(String pMessage) {
        return setString(SharePrefConstants.sdk_conf_maintenance_withdraw, pMessage);
    }

    public String getMaintenanceWithDraw() {
        return getString(SharePrefConstants.sdk_conf_maintenance_withdraw);
    }

    public boolean setEnableDeposite(boolean pEnableDeposite) {
        return setBoolean(SharePrefConstants.sdk_conf_enable_deposite, pEnableDeposite);
    }

    public boolean getEnableDeposite() {
        return getBoolean(SharePrefConstants.sdk_conf_enable_deposite, true);
    }

    public long getPlatformInfoExpriedTime() {
        return getLong(SharePrefConstants.sdk_conf_platform_expired_time);
    }

    public boolean setPlatformInfoExpriedTime(long pValue) {
        return setLong(SharePrefConstants.sdk_conf_platform_expired_time, pValue);
    }

    public long getPlatformInfoExpriedTimeDuration() {
        return getLong(SharePrefConstants.sdk_conf_platform_expired_time_duration);
    }

    public boolean setPlatformInfoExpriedTimeDuration(long pValue) {
        return setLong(SharePrefConstants.sdk_conf_platform_expired_time_duration, pValue);
    }

    public boolean setExpiredTimeAppChannel(String pAppID, long pExpiredTime) {
        return setLong(SharePrefConstants.sdk_conf_app_info_expired_time + pAppID, pExpiredTime);
    }

    public long getExpiredTimeAppChannel(String pAppID) {
        return getLong(SharePrefConstants.sdk_conf_app_info_expired_time + pAppID);
    }

    public boolean setExpiredBankList(long pExpiredTime) {
        return setLong(SharePrefConstants.sdk_conf_banklist_expire_time, pExpiredTime);
    }

    public long getExpiredBankList() {
        return getLong(SharePrefConstants.sdk_conf_banklist_expire_time);
    }

    public boolean setCheckSumBankList(String pCheckSum) {
        return setString(SharePrefConstants.sdk_conf_banklist_checksum, pCheckSum);
    }

    public String getCheckSumBankList() {
        return getString(SharePrefConstants.sdk_conf_banklist_checksum);
    }

    public boolean setBankConfig(String pKey, String pBankConfig) {
        return setString(SharePrefConstants.sdk_conf_bankconfig + pKey, pBankConfig);
    }

    public String getBankConfig(String pKey) {
        return getString(SharePrefConstants.sdk_conf_bankconfig + pKey);
    }

    public boolean setBankPrefix(String pBankMap) {
        return setString(SharePrefConstants.sdk_conf_bank_prefix, pBankMap);
    }

    public String getBankPrefix() {
        return getString(SharePrefConstants.sdk_conf_bank_prefix);
    }

    public boolean setBankCodeList(String pBankCodeList) {
        return setString(SharePrefConstants.sdk_conf_sorted_bankcode_list, pBankCodeList);
    }

    public String getBankCodeList() {
        return getString(SharePrefConstants.sdk_conf_sorted_bankcode_list);
    }

    public boolean setCheckSumAppChannel(String pAppID, String pCheckSum) {
        return setString(SharePrefConstants.sdk_conf_app_info_checksum + pAppID, pCheckSum);
    }

    public String getCheckSumAppChannel(String pAppID) {
        return getString(SharePrefConstants.sdk_conf_app_info_checksum + pAppID);
    }

    //cache url download resource
    public String getResourceDownloadUrl() {
        return getString(SharePrefConstants.sdk_conf_resource_download_url);
    }

    public boolean setResourceDownloadUrl(String pValue) {
        return setString(SharePrefConstants.sdk_conf_resource_download_url, pValue);
    }

    //current user id.
    public String getCurrentUserID() {
        return getString(SharePrefConstants.sdk_conf_current_user_id);
    }

    public boolean setCurrentUserID(String pValue) {
        return setString(SharePrefConstants.sdk_conf_current_user_id, pValue);
    }

    public String getAppVersion() {
        return getString(SharePrefConstants.sdk_conf_app_ver);
    }

    public boolean setAppVersion(String pValue) {
        return setString(SharePrefConstants.sdk_conf_app_ver, pValue);
    }

    public String getPlatformInfoCheckSum() {
        return getString(SharePrefConstants.sdk_conf_platform_checksum);
    }

    public boolean setPlatformInfoCheckSum(String pValue) {
        return setString(SharePrefConstants.sdk_conf_platform_checksum, pValue);
    }

    public String getBankAccountCheckSum() {
        return getString(SharePrefConstants.sdk_conf_bankaccount_checksum);
    }

    public boolean setBankAccountCheckSum(String pValue) {
        return setString(SharePrefConstants.sdk_conf_bankaccount_checksum, pValue);
    }

    public String getCardInfoCheckSum() {
        return getString(SharePrefConstants.sdk_conf_cardinfo_checksum);
    }

    public boolean setCardInfoCheckSum(String pValue) {
        return setString(SharePrefConstants.sdk_conf_cardinfo_checksum, pValue);
    }

    public String getUnzipPath() throws Exception {
        return getString(SharePrefConstants.sdk_conf_resource_path);
    }

    public boolean setUnzipPath(String pValue) {
        return setString(SharePrefConstants.sdk_conf_resource_path, pValue);
    }

    public String getResourceVersion() {
        return getString(SharePrefConstants.sdk_conf_resource_ver);
    }

    public boolean setResourceVersion(String pValue) {
        return setString(SharePrefConstants.sdk_conf_resource_ver, pValue);
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

    public boolean setBankMinAmountSupport(String pKey, long pValue) {
        return setLong(pKey + "__MIN__BANK", pValue);
    }

    public long getBankMinAmountSupport(String pKey) {
        return getLong(pKey + "__MIN__BANK");
    }

    public boolean setMap(String pUserId, String pKey, String pConfig) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(pUserId).append(Constants.COMMA).append(pKey);
            return setString(stringBuilder.toString(), pConfig);
        } catch (Exception ignored) {
        }
        return false;
    }

    public String getMap(String pUserID, String pKey) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pUserID).append(Constants.COMMA).append(pKey);
        return getString(stringBuilder.toString());
    }

    public boolean setBankAccountKeyList(String pKey, String pBankAccountKeyList) {
        return setString(pKey + SharePrefConstants.sdk_conf_bankaccount_list, pBankAccountKeyList);
    }

    public String getBankAccountKeyList(String pKey) {
        try {
            return getString(pKey + SharePrefConstants.sdk_conf_bankaccount_list);
        } catch (Exception e) {
            Timber.w(e, "Exception getBankAccountKeyList");
        }
        return null;
    }

    /***
     * set map card list
     */
    public boolean setMapCardList(String pKey, String pMappedCardKeyList) {
        return setString(pKey + SharePrefConstants.sdk_conf_mapcard_list, pMappedCardKeyList);
    }

    public String getMapCardKeyList(String pKey) {
        return getString(pKey + SharePrefConstants.sdk_conf_mapcard_list);
    }

    /***
     * get map card list by user id
     * @param pUserID
     * @return
     */
    public List<MapCard> getMapCardList(String pUserID) {
        List<MapCard> mappedCardList = new ArrayList<>();
        String keyList = getMapCardKeyList(pUserID);
        if (TextUtils.isEmpty(keyList)) {
            return mappedCardList;
        }
        for (String key : keyList.split(Constants.COMMA)) {
            String strMappedCard = getMap(pUserID, key);
            if (TextUtils.isEmpty(strMappedCard)) {
                continue;
            }
            MapCard mappedCard = GsonUtils.fromJsonString(strMappedCard, MapCard.class);
            if (mappedCard != null) {
                BankConfig bankConfig = GsonUtils.fromJsonString(getBankConfig(mappedCard.bankcode), BankConfig.class);
                if (bankConfig != null) {
                    mappedCard.displayorder = bankConfig.displayorder;
                }
                mappedCardList.add(mappedCard);
            }
        }
        return mappedCardList;
    }

    public List<BankAccount> getBankAccountList(String pUserID) {
        List<BankAccount> bankAccountList = new ArrayList<>();
        String keyList = getBankAccountKeyList(pUserID);
        if (TextUtils.isEmpty(keyList)) {
            return bankAccountList;
        }
        for (String key : keyList.split(Constants.COMMA)) {
            String sMap = getMap(pUserID, key);
            if (TextUtils.isEmpty(sMap)) {
                continue;
            }
            BankAccount bankAccount = GsonUtils.fromJsonString(sMap, BankAccount.class);
            if (bankAccount != null) {
                BankConfig bankConfig = GsonUtils.fromJsonString(getBankConfig(CardType.PVCB), BankConfig.class);
                if (bankConfig != null) {
                    bankAccount.displayorder = bankConfig.displayorder;
                }
                bankAccountList.add(bankAccount);
            }
        }
        return bankAccountList;
    }

    public boolean resetMapCardListCache(String pUserId) {
        String keyList = getMapCardKeyList(pUserId);
        resetMap(pUserId, keyList);
        setMapCardList(pUserId, null);
        return true;
    }

    public boolean resetBankAccountListCache(String pUserId) {
        String keyList = getBankAccountKeyList(pUserId);
        resetMap(pUserId, keyList);
        setBankAccountKeyList(pUserId, null);
        return true;
    }

    private void resetMap(String pUserId, String pKeyList) {
        if (!TextUtils.isEmpty(pKeyList)) {
            for (String key : pKeyList.split(Constants.COMMA)) {
                setMap(pUserId, key, null);
            }
        }
    }

    public boolean setApp(String pId, String pConfig) {
        return setString(SharePrefConstants.sdk_conf_appinfo + pId, pConfig);
    }

    public String getAppById(String pID) {
        return getString(SharePrefConstants.sdk_conf_appinfo + pID);
    }

    public boolean setPmcConfig(String keyChannelID, String pConfig) {
        return setString(SharePrefConstants.sdk_conf_channel_prefix + keyChannelID, pConfig);
    }

    public String getPmcConfigByPmcID(long pAppId, @TransactionType int pTranstype, int pPmcID, String pBankCode) {
        StringBuilder channelIDkey = new StringBuilder();
        channelIDkey.append(MiniPmcTransType.getPmcKey(pAppId, pTranstype, pPmcID));
        if (!TextUtils.isEmpty(pBankCode)) {
            channelIDkey.append(Constants.UNDERLINE).append(pBankCode);
        }
        return getString(SharePrefConstants.sdk_conf_channel_prefix + channelIDkey.toString());
    }


    public String getPmcConfigByPmcKey(String pPmcKey) {
        return getString(SharePrefConstants.sdk_conf_channel_prefix + pPmcKey);
    }

    public boolean setPmcTranstypeKeyList(String pKey, List<String> pPmcIdList) {
        StringBuilder pmcIdList = new StringBuilder();
        for (int i = 0; i < pPmcIdList.size(); i++) {
            pmcIdList.append(pPmcIdList.get(i));
            if ((i + 1) < pPmcIdList.size()) {
                pmcIdList.append(Constants.COMMA);
            }
        }
        return setString(SharePrefConstants.sdk_conf_channel_list + pKey, pmcIdList.toString());
    }

    public List<String> getPmcTranstypeKeyList(String pKey) {
        List<String> result = new ArrayList<>();
        String raw = getString(SharePrefConstants.sdk_conf_channel_list + pKey);
        if (TextUtils.isEmpty(raw)) {
            return result;
        }
        try {
            for (String pmcId : raw.split(Constants.COMMA)) {
                result.add(String.valueOf(pmcId));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public boolean setTranstypePmcCheckSum(String pKey, String pCheckSum) {
        return setString(SharePrefConstants.sdk_conf_transtype_list_checksum + pKey, pCheckSum);
    }

    public String getTransypePmcCheckSum(String pKey) {
        return getString(SharePrefConstants.sdk_conf_transtype_list_checksum + pKey);
    }

    public String getBankAccountChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        Timber.d("get cache bank account channel config, bankcode " + pBankCode);
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_bankaccount, pBankCode);
    }

    public String getATMChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        Timber.d("get cache atm channel config, bankcode " + pBankCode);
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_atm, pBankCode);
    }

    public String getCreditCardChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        Timber.d("get cache credit card channel config, bankcode " + pBankCode);
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_credit_card, pBankCode);
    }

    public String getZaloPayChannelConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        return getPmcConfigByPmcID(pAppId, pTranstype, BuildConfig.channel_zalopay, pBankCode);
    }

    public boolean setRevertVoucher(String pUserId, String mapKey, String pVoucherInfo) throws Exception {
        String mapKeys = getVoucherMapList(pUserId);
        boolean existed = false;
        if (!TextUtils.isEmpty(mapKeys)) {
            String[] keys = mapKeys.split(Constants.COMMA);
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (!TextUtils.isEmpty(key) && key.equals(mapKey)) {
                    existed = true;
                    break;
                }
            }
        }
        if (!existed) {
            StringBuffer mapKeysBuilder = new StringBuffer();
            if (!TextUtils.isEmpty(mapKeys)) {
                mapKeysBuilder.append(mapKeys);
                mapKeysBuilder.append(Constants.COMMA);
            }
            mapKeysBuilder.append(mapKey);
            setVoucherMapList(pUserId, mapKeysBuilder.toString());
        }
        return setString(mapKey, pVoucherInfo);
    }

    public void clearVouchers(String pUserId, String mapKey) throws Exception {
        String mapKeys = getVoucherMapList(pUserId);
        if (TextUtils.isEmpty(mapKeys)) {
            return;
        }
        StringBuffer mapKeyBuffer = new StringBuffer();
        String[] keys = mapKeys.split(Constants.COMMA);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            if (key.equals(mapKey)) {
                continue;
            }
            mapKeyBuffer.append(key);
            if ((i + 1) < keys.length) {
                mapKeyBuffer.append(Constants.COMMA);
            }
        }
        setVoucherMapList(pUserId, mapKeyBuffer.toString());
        setString(mapKey, null);
    }

    public String[] getVouchers(String pUserId) throws Exception {
        String mapKeys = getVoucherMapList(pUserId);
        if (TextUtils.isEmpty(mapKeys)) {
            return new String[0];
        }
        String[] keys = mapKeys.split(Constants.COMMA);
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            try {
                String value = getString(keys[i]);
                if (!TextUtils.isEmpty(value)) {
                    values[i] = value;
                }
            } catch (Exception e) {
                Timber.d(e);
            }
        }
        return values;
    }

    public String getVoucherMapList(String pUserId) {
        return getString(getUserVoucherKey(pUserId));
    }

    public boolean setVoucherMapList(String pUser, String pMapKeys) {
        return setString(getUserVoucherKey(pUser), pMapKeys);
    }

    private String getUserVoucherKey(String pUserId) {
        StringBuffer keyBuiler = new StringBuffer();
        keyBuiler
                .append(SharePrefConstants.sdk_conf_channel_list)
                .append(Constants.UNDERLINE)
                .append(pUserId);
        return keyBuiler.toString();
    }
}
