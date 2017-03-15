package vn.com.vng.zalopay.data.balance;

import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by huuhoa on 6/14/16.
 * Implementation for balance local storage
 */
public class BalanceLocalStorage extends SqlBaseScopeImpl implements BalanceStore.LocalStorage {
    public BalanceLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void putBalance(long value) {
        Timber.d("writeBalance, balance: %d", value);
        this.insertDataManifest(Constants.MANIF_BALANCE, String.valueOf(value));
    }

    @Override
    public long getBalance() {
        String balance = getDataManifest(Constants.MANIF_BALANCE);
        long ret;
        try {
            ret = Long.parseLong(balance);
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }
}