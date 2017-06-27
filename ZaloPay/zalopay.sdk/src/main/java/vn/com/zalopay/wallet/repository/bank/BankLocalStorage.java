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
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankLocalStorage extends AbstractLocalStorage implements BankStore.LocalStorage {
    public BankLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        super(sharedPreferencesManager);
    }

    protected boolean isCheckSumChanged(String pNewCheckSum) {
        String checkSumOnCache = null;
        try {
            checkSumOnCache = mSharedPreferences.getCheckSumBankList();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return TextUtils.isEmpty(checkSumOnCache) || (!TextUtils.isEmpty(pNewCheckSum) && !checkSumOnCache.equalsIgnoreCase(pNewCheckSum));
    }

    @Override
    public String getCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getCheckSumBankList();
        } catch (Exception e) {
            Log.e(this, e);
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
        } catch (Exception e) {
            Log.e(this, e);
        }
        return bankPrefix;
    }

    @Override
    public long getExpireTime() {
        long expiretime = 0;
        try {
            expiretime = mSharedPreferences.getExpiredBankList();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return expiretime;
    }

    @Override
    public void put(BankConfigResponse pResponse) {
        if (pResponse == null || pResponse.returncode != 1) {
            Timber.d("request not success, stopping saving bank list to cache");
            return;
        }
        long time_to_live = System.currentTimeMillis() + pResponse.expiredtime;
        mSharedPreferences.setExpiredBankList(time_to_live);
        if (!isCheckSumChanged(pResponse.checksum)) {
            Timber.d("bank list on cache is valid - skip udpate");
            return;
        }
        Timber.d("start update bank list to cache");
        //save check sum
        mSharedPreferences.setCheckSumBankList(pResponse.checksum);
        //sort by order
        List<BankConfig> bankConfigList = pResponse.banklist;
        Collections.sort(bankConfigList, (item1, item2) -> Integer.valueOf(item1.displayorder).compareTo(item2.displayorder));
        StringBuilder stringBuilder = new StringBuilder();
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
            stringBuilder.append(bankConfig.code).append(Constants.COMMA);
        }
        //save bank code list in order sort
        mSharedPreferences.setBankCodeList(stringBuilder.toString());
        //save bank prefix number (use to detect card type)
        String hashMapBank = GsonUtils.toJsonString(pResponse.bankcardprefixmap);
        mSharedPreferences.setBankConfigMap(hashMapBank);
    }

    @Override
    public Observable<BankConfigResponse> get() {
        return Observable.defer(() -> {
            try {
                BankConfigResponse bankConfigResponse = new BankConfigResponse();
                bankConfigResponse.bankcardprefixmap = getBankPrefix();
                bankConfigResponse.expiredtime = getExpireTime();
                Log.d(this, "load bank list from cache", bankConfigResponse);
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
            Log.e(this, e);
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
                Log.e(this, e);
            }
            if (!TextUtils.isEmpty(bankConfig)) {
                return GsonUtils.fromJsonString(bankConfig, BankConfig.class);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    @Override
    public void clearCheckSum() {
        mSharedPreferences.setCheckSumBankList(null);
    }

    @Override
    public void clearConfig() {
        mSharedPreferences.setBankConfigMap(null);
    }
}