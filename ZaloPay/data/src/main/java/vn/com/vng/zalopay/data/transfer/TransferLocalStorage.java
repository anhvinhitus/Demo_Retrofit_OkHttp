package vn.com.vng.zalopay.data.transfer;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.cache.model.TransferRecentDao;
import vn.com.vng.zalopay.data.util.ObservableHelper;

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
        return mDao.queryBuilder().orderDesc(TransferRecentDao.Properties.TimeCreate).limit(3).list();
    }

    @Override
    public void append(TransferRecent recentTransfer) {
        mDao.insertOrReplaceInTx(recentTransfer);
    }
}
