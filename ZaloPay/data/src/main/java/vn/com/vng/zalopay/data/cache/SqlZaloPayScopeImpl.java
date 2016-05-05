package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.DataManifest;
import vn.com.vng.zalopay.data.cache.model.DataManifestDao;
import vn.com.vng.zalopay.data.util.Lists;
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
        return makeObservable(() -> {
            String balance = getDataManifest(Constants.MANIF_BALANCE);
            Long ret = 0l;
            try {
                ret = Long.parseLong(balance);
            } catch (Exception e) {
                Timber.e(e, " parse error " + balance);
            }
            return ret;
        });
    }

    @Override
    public void writeBalance(long balance) {
        this.insertDataManifest(Constants.MANIF_BALANCE, String.valueOf(balance));
    }


    private void insertDataManifest(String key, String values) {
        daoSession.getDataManifestDao().insertOrReplace(new DataManifest(key, values));
    }

    private String getDataManifest(String key) {
        List<DataManifest> dataManifests = daoSession.getDataManifestDao().queryBuilder()
                .where(DataManifestDao.Properties.Key.eq(key))
                .limit(1)
                .list();
        if (Lists.isEmptyOrNull(dataManifests)) return null;
        return dataManifests.get(0).getValue();
    }

}
