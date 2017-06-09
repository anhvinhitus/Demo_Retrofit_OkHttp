package vn.com.zalopay.wallet.repository.banklist;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankListLocalStorage extends AbstractLocalStorage implements BankListStore.LocalStorage {
    public BankListLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
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
    public String getBankPrefix() {
        String mapBankConfig = null;
        try {
            mapBankConfig = mSharedPreferences.getBankPrefix();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return mapBankConfig;
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
        Log.d(this, "start save bank list to cache", pResponse);
        if (pResponse == null || pResponse.returncode != 1) {
            Log.d(this, "request not success, stopping saving bank list to cache");
            return;
        }
        long time_to_live = System.currentTimeMillis() + pResponse.expiredtime;
        mSharedPreferences.setExpiredBankList(time_to_live);
        if (isCheckSumChanged(pResponse.checksum)) {
            //save check sum
            mSharedPreferences.setCheckSumBankList(pResponse.checksum);
            //sort by order
            List<BankConfig> bankConfigList = pResponse.banklist;
            Collections.sort(bankConfigList, (item1, item2) -> Integer.valueOf(item1.displayorder).compareTo(item2.displayorder));
            StringBuilder stringBuilder = new StringBuilder();
            for (BankConfig bankConfig : bankConfigList) {
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
    }

    @Override
    public Observable<BankConfigResponse> get() {
        return Observable.defer(() -> {
            try {
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(getBankPrefix(), type);
                BankConfigResponse bankConfigResponse = new BankConfigResponse();
                bankConfigResponse.bankcardprefixmap = bankMap;
                bankConfigResponse.expiredtime = getExpireTime();
                Log.d(this, "loaded bank list from cache", bankConfigResponse);
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
        String bankConfig = "";
        try {
            bankConfig = mSharedPreferences.getBankConfig(bankCode);
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (TextUtils.isEmpty(bankConfig)) {
            return null;
        }
        return GsonUtils.fromJsonString(bankConfig, BankConfig.class);
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