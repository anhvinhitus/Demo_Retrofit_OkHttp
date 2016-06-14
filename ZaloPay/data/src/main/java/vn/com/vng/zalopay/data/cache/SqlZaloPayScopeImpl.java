package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.mapper.ZaloPayDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.cache.model.TransactionLogDao;
import vn.com.vng.zalopay.data.cache.model.ZaloFriend;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendDao;
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

        Timber.d("write list transaction %s", val.size(), listTransHistories(10));
    }

    @Override
    public void write(TransHistoryEntity val) {
        getDaoSession().getTransactionLogDao().insertOrReplace(zaloCacheMapper.transform(val));
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistories() {
        return makeObservable(() -> listTransHistories(Integer.MAX_VALUE))
                .doOnNext(transHistoryEntities -> Timber.d("transactionHistories %s", transHistoryEntities.size()))
                ;
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistories(int limit) {
        return makeObservable(() -> listTransHistories(limit));
    }

    @Override
    public List<TransHistoryEntity> listTransHistories(int limit) {
        return zaloCacheMapper.transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                        .limit(limit)
                        .orderDesc(TransactionLogDao.Properties.Reqdate)
                        .list());
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
        Timber.d("writeBalance, balance: %d", balance);
        this.insertDataManifest(Constants.MANIF_BALANCE, String.valueOf(balance));
    }

    @Override
    public void writeZaloFriends(List<ZaloFriend> val) {
        getDaoSession().getZaloFriendDao().insertOrReplaceInTx(val);
    }

    @Override
    public void writeZaloFriend(ZaloFriend val) {
        getDaoSession().getZaloFriendDao().insertOrReplaceInTx(val);
    }

    @Override
    public List<ZaloFriend> listZaloFriend() {
        return getDaoSession().getZaloFriendDao().queryBuilder().where(ZaloFriendDao.Properties.UsingApp.eq("true")).list();
    }

    @Override
    public List<ZaloFriend> listZaloFriend(int limit) {
        return getDaoSession().getZaloFriendDao().queryBuilder().where(ZaloFriendDao.Properties.UsingApp.eq("true")).limit(limit).list();
    }

    @Override
    public boolean isHaveZaloFriendDb() {
        return getDaoSession().getZaloFriendDao().queryBuilder().count() > 0;
    }

    @Override
    public void writeTransferRecent(TransferRecent val) {
        getDaoSession().getTransferRecentDao().insertOrReplaceInTx(val);
    }

    @Override
    public List<TransferRecent> listTransferRecent() {
        return getDaoSession().getTransferRecentDao().queryBuilder().list();
    }

    @Override
    public List<TransferRecent> listTransferRecent(int limit) {
        return getDaoSession().getTransferRecentDao().queryBuilder().limit(limit).list();
    }

    @Override
    public boolean isHaveTransactionInDb() {
        return getDaoSession().getTransactionLogDao().queryBuilder().count() > 0;
    }

}
