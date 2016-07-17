package vn.com.vng.zalopay.data.balance;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.util.ObservableHelper;
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
    public Observable<Long> getBalance() {
        return ObservableHelper.makeObservable(() -> {
            String balance = getDataManifest(Constants.MANIF_BALANCE);
            Long ret = 0l;
            try {
                ret = Long.parseLong(balance);
            } catch (Exception e) {
            }
            return ret;
        });
    }
}
