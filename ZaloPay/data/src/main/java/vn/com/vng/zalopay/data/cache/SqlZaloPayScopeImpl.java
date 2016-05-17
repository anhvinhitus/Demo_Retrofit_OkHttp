package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.mapper.ZaloPayDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class SqlZaloPayScopeImpl extends SqlBaseScopeImpl implements SqlZaloPayScope {

    private final User user;
    private ZaloPayDaoMapper zaloCacheMapper;
    private final int LENGTH_TRANSITION = 30;

    public SqlZaloPayScopeImpl(User user, DaoSession daoSession, ZaloPayDaoMapper zaloCacheMapper) {
        super(daoSession);
        this.user = user;
        this.zaloCacheMapper = zaloCacheMapper;
    }

    @Override
    public void write(List<TransHistoryEntity> val) {
        getDaoSession().getTransactionLogDao().insertOrReplaceInTx(zaloCacheMapper.transform(val));
    }

    @Override
    public void write(TransHistoryEntity val) {
        getDaoSession().getTransactionLogDao().insertOrReplace(zaloCacheMapper.transform(val));
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistorys() {
        return makeObservable(() -> zaloCacheMapper.transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                       // .limit(LENGTH_TRANSITION)
                        .list()));
    }

    @Override
    public Observable<TransHistoryEntity> transactionHistory() {
        //return makeObservable(() -> zaloCacheMapper.transform(getDaoSession().getTransactionLogDao().queryBuilder().w));
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
            }
            return ret;
        });
    }

    @Override
    public void writeBalance(long balance) {
        Timber.tag("SqlZaloPayScopeImpl").d("writeBalance, balance:" + balance);
        this.insertDataManifest(Constants.MANIF_BALANCE, String.valueOf(balance));
    }

    @Override
    public boolean isHaveTransactionInDb() {
        return getDaoSession().getTransactionLogDao().queryBuilder().count() > 0;
    }
}
