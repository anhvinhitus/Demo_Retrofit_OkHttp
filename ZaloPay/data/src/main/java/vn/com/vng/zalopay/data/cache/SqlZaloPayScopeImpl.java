package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class SqlZaloPayScopeImpl extends SqlBaseScopeImpl implements SqlZaloPayScope {

    private final User user;
    private final int LENGTH_TRANSITION = 30;

    public SqlZaloPayScopeImpl(User user, DaoSession daoSession) {
        super(daoSession);
        this.user = user;
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
}
