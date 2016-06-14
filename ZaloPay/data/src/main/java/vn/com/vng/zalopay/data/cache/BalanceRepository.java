package vn.com.vng.zalopay.data.cache;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by huuhoa on 6/14/16.
 * Implementation for balance repository
 */
public class BalanceRepository extends SqlBaseScopeImpl implements BalanceContract.Repository {
    public BalanceRepository(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void putBalance(long value) {
        Timber.d("writeBalance, balance: %d", value);
        this.insertDataManifest(Constants.MANIF_BALANCE, String.valueOf(value));
    }

    @Override
    public Observable<Long> getBalance() {
        return RepositoryHelper.makeObservable(() -> {
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
