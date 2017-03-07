package vn.com.vng.zalopay.data.transfer;

import java.util.List;

import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.cache.model.TransferRecentDao;

/**
 * Created by huuhoa on 7/6/16.
 * Implementation for TransferStore.LocalStorage
 */
public class TransferLocalStorage extends SqlBaseScopeImpl implements TransferStore.LocalStorage {

    TransferRecentDao mDao;

    public TransferLocalStorage(DaoSession session) {
        super(session);
        mDao = session.getTransferRecentDao();
    }

    @Override
    public List<TransferRecent> get() {
        return mDao.queryBuilder().orderDesc(TransferRecentDao.Properties.TimeCreate).limit(20).list();
    }

    @Override
    public void append(TransferRecent recentTransfer) {
        mDao.insertOrReplaceInTx(recentTransfer);
    }
}
