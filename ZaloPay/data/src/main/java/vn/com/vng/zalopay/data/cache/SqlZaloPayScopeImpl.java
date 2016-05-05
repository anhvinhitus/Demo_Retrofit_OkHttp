package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.DataManifest;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class SqlZaloPayScopeImpl extends SqlBaseScope implements SqlZaloPayScope {

    private final User user;

    public SqlZaloPayScopeImpl(User user, DaoSession daoSession) {
        super(daoSession);
        this.user = user;
    }

    @Override
    public void write(List<TransHistoryEntity> val) {
    }

    @Override
    public void write(TransHistoryEntity val) {
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistorys() {
        return null;
    }

    @Override
    public Observable<TransHistoryEntity> transactionHistory() {
        return null;
    }

    @Override
    public Observable<Long> balance() {
        return null;
    }

    @Override
    public void writeBalance(long balance) {
        this.insertDataManifest(Constants.MANIF_BALANCE, String.valueOf(balance));
    }


    private void insertDataManifest(String values, String key) {
        daoSession.getDataManifestDao().insertOrReplace(new DataManifest(values, key));
    }

}
