package vn.com.zalopay.wallet.repository.bank;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankResponse;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;
import vn.com.zalopay.wallet.repository.SharedPreferencesManager;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankLocalStorage extends AbstractLocalStorage implements BankStore.LocalStorage {
    public BankLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        super(sharedPreferencesManager);
    }

    @Override
    public SharedPreferencesManager sharePref() {
        return mSharedPreferences;
    }

    private boolean isCheckSumChanged(String pNewCheckSum) {
        String checkSumOnCache = null;
        try {
            checkSumOnCache = mSharedPreferences.getCheckSumBankList();
        } catch (Exception ignored) {
        }
        return TextUtils.isEmpty(checkSumOnCache) || (!TextUtils.isEmpty(pNewCheckSum) && !checkSumOnCache.equalsIgnoreCase(pNewCheckSum));
    }

    @Override
    public String getCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getCheckSumBankList();
        } catch (Exception ignored) {
        }
        return !TextUtils.isEmpty(checksum) ? checksum : "";
    }

    @Override
    public Map<String, String> getBankPrefix() {
        Map<String, String> bankPrefix = null;
        try {
            java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            bankPrefix = GsonUtils.fromJsonString(mSharedPreferences.getBankPrefix(), type);
        } catch (Exception ignored) {
        }
        return bankPrefix;
    }

    @Override
    public long getExpireTime() {
        long expireTime = 0;
        try {
            expireTime = mSharedPreferences.getExpiredBankList();
        } catch (Exception e) {
            Timber.d(e, "Exception getExpireTime");
        }
        return expireTime;
    }

    @Override
    public void setExpireTime(long expireTime) {
        mSharedPreferences.setExpiredBankList(expireTime);
    }

    @Override
    public void put(BankResponse pResponse) {
        if (pResponse == null || pResponse.returncode != 1) {
            Timber.d("request not success, stopping saving bank list to cache");
            return;
        }
        long time_to_live = System.currentTimeMillis() + pResponse.expiredtime;
        setExpireTime(time_to_live);
        if (!isCheckSumChanged(pResponse.checksum)) {
            Timber.d("bank list on cache is valid - skip update");
            return;
        }
        Timber.d("start update bank list to cache");
        //save check sum
        mSharedPreferences.setCheckSumBankList(pResponse.checksum);
        //sort by order
        List<BankConfig> bankConfigList = pResponse.banklist;
        Collections.sort(bankConfigList, (item1, item2) -> item1.displayorder > item2.displayorder ? 1 : -1);
        StringBuilder bankCodeList = new StringBuilder();
        for (BankConfig bankConfig : bankConfigList) {
            //for testing
                /*if (bankConfig.code.equals(CardType.PVTB)) {
                   *//* bankConfig.status = BankStatus.MAINTENANCE;
                    bankConfig.maintenanceto = Long.parseLong("1480063794000");
                    bankConfig.maintenancemsg = "NH VietinBank bảo trì tới %s, vui lòng chọn ngân hàng khác hoặc quay lại sau";*//*
                    bankConfig.functions.get(0).status = BankStatus.MAINTENANCE;
                }*/
            //save bank config
            mSharedPreferences.setBankConfig(bankConfig.code, GsonUtils.toJsonString(bankConfig));
            bankCodeList.append(bankConfig.code).append(Constants.COMMA);
        }
        //save bank code list in order sort
        mSharedPreferences.setBankCodeList(bankCodeList.toString());
        //save bank prefix number (use to detect card type)
        String hashMapBank = GsonUtils.toJsonString(pResponse.bankcardprefixmap);
        mSharedPreferences.setBankPrefix(hashMapBank);
    }

    @Override
    public Observable<BankResponse> get() {
        return Observable.defer(() -> {
            try {
                BankResponse bankConfigResponse = new BankResponse();
                bankConfigResponse.bankcardprefixmap = getBankPrefix();
                bankConfigResponse.expiredtime = getExpireTime();
                Timber.d("load bank list from cache %s", GsonUtils.toJsonString(bankConfigResponse));
                return Observable.just(bankConfigResponse);
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    public String getBankCodeList() {
        String bankCodeList = null;
        try {
            bankCodeList = mSharedPreferences.getBankCodeList();
        } catch (Exception e) {
            Timber.d(e, "Exception getBankCodeList");
        }
        return bankCodeList;
    }

    @Override
    public BankConfig getBankConfig(String bankCode) {
        try {
            String bankConfig = "";
            try {
                bankConfig = mSharedPreferences.getBankConfig(bankCode);
            } catch (Exception e) {
                Timber.d(e, "Exception getBankConfig");
            }
            if (!TextUtils.isEmpty(bankConfig)) {
                return GsonUtils.fromJsonString(bankConfig, BankConfig.class);
            }
        } catch (Exception e) {
            Timber.d(e, "Exception getBankConfig");
        }
        return null;
    }

    @Override
    public void clearConfig() {
        try {
            mSharedPreferences.setExpiredBankList(0);
            mSharedPreferences.setCheckSumBankList(null);
            mSharedPreferences.setBankPrefix(null);
            String bankCodeList = getBankCodeList();
            if (TextUtils.isEmpty(bankCodeList)) {
                return;
            }
            String[] arrayBankCode = bankCodeList.split(Constants.COMMA);
            for (String bankCode : arrayBankCode) {
                mSharedPreferences.setBankConfig(bankCode, null);
            }
            mSharedPreferences.setBankCodeList(null);
        } catch (Exception e) {
            Timber.w(e, "clear bank config error");
        }
    }
}