package vn.com.zalopay.wallet.repository.bankaccount;

import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.helper.ListUtils;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankAccountLocalStorage extends AbstractLocalStorage implements BankAccountStore.LocalStorage {
    public BankAccountLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        super(sharedPreferencesManager);
    }

    @Override
    public void resetBankAccountCacheList(String userId) {
        mSharedPreferences.resetBankAccountListCache(userId);
    }

    @Override
    public void resetBankAccountCache(String userId, String first6cardno, String last4cardno) {
        String accountKey = first6cardno + last4cardno;
        mSharedPreferences.setMap(userId, accountKey, null);
        String keyList = mSharedPreferences.getBankAccountKeyList(userId);
        if (TextUtils.isEmpty(keyList)) {
            return;
        }
        String accountKeyList = ListUtils.filterMapKey(keyList, accountKey);
        setBankAccountKeyList(userId, accountKeyList);
    }

    @Override
    public void put(String pUserId, String checkSum, List<BankAccount> bankAccountList) {
        if (!needUpdate(checkSum)) {
            Timber.d("bank account list in cache is valid - skip update");
            return;
        }
        Timber.d("start update bank account list on cache");
        mSharedPreferences.setBankAccountCheckSum(checkSum);
        if(bankAccountList == null || bankAccountList.size() <= 0){
            resetBankAccountCacheList(pUserId);
            return;
        }
        StringBuilder keyList = new StringBuilder();
        int count = 0;
        for (BaseMap bankAccount : bankAccountList) {
            count++;
            setBankAccount(pUserId, bankAccount);
            keyList.append(bankAccount.getKey());
            if (count < bankAccountList.size()) {
                keyList.append(Constants.COMMA);
            }
        }
        //cache map list
        setBankAccountKeyList(pUserId, keyList.toString());
    }

    @Override
    public void saveResponse(String pUserId, BankAccountListResponse pResponse) {
        Log.d(this, "start save bank account list", pResponse);
        if (pResponse == null || pResponse.returncode != 1) {
            Timber.d("stop save bank account cache because result fail");
            return;
        }
        put(pUserId, pResponse.bankaccountchecksum, pResponse.bankaccounts);
    }

    @Override
    public String getCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getBankAccountCheckSum();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return !TextUtils.isEmpty(checksum) ? checksum : "";
    }

    @Override
    public List<BankAccount> getBankAccountList(String userid) {
        List<BankAccount> bankAccounts = null;
        try {
            bankAccounts = mSharedPreferences.getBankAccountList(userid);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return bankAccounts;
    }

    @Override
    public void setBankAccount(String userid, BaseMap bankAccount) {
        if (bankAccount == null) {
            return;
        }
        String key = bankAccount.getKey();
        mSharedPreferences.setMap(userid, key, GsonUtils.toJsonString(bankAccount));
    }

    @Override
    public BankAccount getBankAccount(String userid, String key) {
        String map = mSharedPreferences.getMap(userid, key);
        BankAccount bankAccount = null;
        if (!TextUtils.isEmpty(map)) {
            bankAccount = GsonUtils.fromJsonString(map, BankAccount.class);
        }
        return bankAccount;
    }

    @Override
    public void setBankAccountKeyList(String userid, String keyList) {
        mSharedPreferences.setBankAccountKeyList(userid, keyList);
    }

    private boolean needUpdate(String newCheckSum) {
        try {
            String checkSumOnCache = getCheckSum();
            if (TextUtils.isEmpty(checkSumOnCache) || (!TextUtils.isEmpty(checkSumOnCache) && !checkSumOnCache.equals(newCheckSum))) {
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    @Override
    public void clearCheckSum() {
        mSharedPreferences.setBankAccountCheckSum(null);
    }
}