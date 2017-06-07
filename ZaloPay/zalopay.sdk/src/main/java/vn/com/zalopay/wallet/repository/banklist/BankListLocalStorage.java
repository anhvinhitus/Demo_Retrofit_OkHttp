package vn.com.zalopay.wallet.repository.banklist;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import rx.Observable;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
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

    protected void saveBankListToCache(BankConfigResponse pResponse) {
        mSharedPreferences.setCheckSumBankList(pResponse.checksum);
        for (BankConfig bankConfig : pResponse.banklist) {
            mSharedPreferences.setBankConfig(bankConfig.code, GsonUtils.toJsonString(bankConfig));
        }
        String hashMapBank = GsonUtils.toJsonString(pResponse.bankcardprefixmap);
        mSharedPreferences.setBankConfigMap(hashMapBank);
        Log.d(this, "save bank list to cache", pResponse);
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
        return mSharedPreferences.getCheckSumBankList();
    }

    @Override
    public String getMap() {
        return mSharedPreferences.getBankMap();
    }

    @Override
    public long getExpireTime() {
        return mSharedPreferences.getExpiredBankList();
    }

    @Override
    public void putBankList(BankConfigResponse bankConfigResponse) {
        if (bankConfigResponse == null || bankConfigResponse.returncode != 1) {
            Log.d(this, "request not success, stopping saving bank list to cache");
            return;
        }
        Log.d(this, "start update expire time bank list");
        long time_to_live = System.currentTimeMillis() + bankConfigResponse.expiredtime;
        mSharedPreferences.setExpiredBankList(time_to_live);
        if (isCheckSumChanged(bankConfigResponse.checksum)) {
            saveBankListToCache(bankConfigResponse);
        }
    }

    @Override
    public Observable<BankConfigResponse> getBankList() {
        return Observable.defer(() -> {
            try {
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(mSharedPreferences.getBankMap(), type);
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
}