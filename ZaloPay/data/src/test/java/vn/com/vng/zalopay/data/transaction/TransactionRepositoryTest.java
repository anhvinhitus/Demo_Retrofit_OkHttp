package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 11/14/16.
 */

public class TransactionRepositoryTest extends ApplicationTestCase {

    private TransactionStore.Repository mRepository;

    @Before
    public void setUp() throws Exception {
        ZaloPayEntityDataMapper mapper = new ZaloPayEntityDataMapper();
        User mUser = new User();
        mUser.accesstoken = "";

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        TransactionStore.LocalStorage mLocalStorage = new TransactionLocalStorage(daoSession);

        mRepository = new TransactionRepository(mapper, mUser, mLocalStorage, null, EventBus.getDefault(), new RxBus());
    }


    @Test
    public void getTransactions() {

    }

    @Test
    public void getTransactionsFail() {

    }

    @Test
    public void getTransactionById() {

    }

    @Test
    public void updateTransactionStatusSuccess() {

    }

    @Test
    public void fetchTransactionHistorySuccessLatest() {

    }

    @Test
    public void fetchTransactionHistoryFailLatest() {

    }

}