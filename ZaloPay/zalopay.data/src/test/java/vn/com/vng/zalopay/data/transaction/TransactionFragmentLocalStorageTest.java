package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.TransactionFragmentEntity;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

import static junit.framework.Assert.assertEquals;

public class TransactionFragmentLocalStorageTest extends ApplicationTestCase {
    private TransactionFragmentStore.LocalStorage mLocalStorage;

    private List<TransactionFragmentEntity> entities = new ArrayList<>();

    private long maxreqdate = 1480000500000L;
    private long maxreqdate2 = 1480000400000L;
    private long maxreqdate3 = 1480000200000L;

    private long minreqdate = 1480000380000L;
    private long minreqdate2 = 1480000280000L;
    private long minreqdate3 = 1480000050000L;

    private int TRANSACTION_SUCCESS = 1;

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionFragmentLocalStorage(daoSession);
    }

    private void initData() {
        TransactionFragmentEntity entity = new TransactionFragmentEntity();
        entity.statustype = TRANSACTION_SUCCESS;
        entity.maxreqdate = maxreqdate;
        entity.minreqdate = minreqdate;
        entities.add(entity);

        TransactionFragmentEntity entity2 = new TransactionFragmentEntity();
        entity2.statustype = TRANSACTION_SUCCESS;
        entity2.maxreqdate = maxreqdate2;
        entity2.minreqdate = minreqdate2;
        entities.add(entity2);

        TransactionFragmentEntity entity3 = new TransactionFragmentEntity();
        entity3.statustype = TRANSACTION_SUCCESS;
        entity3.maxreqdate = maxreqdate3;
        entity3.minreqdate = minreqdate3;
        entities.add(entity3);
    }

    private void insertTransaction() {
        for (int i = 0; i < 3; i++) {
            mLocalStorage.put(entities.get(i));
        }
    }

    @Test
    public void putNullParam() {
        mLocalStorage.put(null);
        TransactionFragmentEntity result = mLocalStorage.getLatestFragment(TRANSACTION_SUCCESS);
        assertEquals("put null param", null, result);
    }

    @Test
    public void put() {
        initData();
        insertTransaction();

        List<TransactionFragmentEntity> result = mLocalStorage.get(maxreqdate, TRANSACTION_SUCCESS);
        assertEquals("put", maxreqdate, result.get(0).maxreqdate);
        result = mLocalStorage.get(maxreqdate2, TRANSACTION_SUCCESS);
        assertEquals("put", 2, result.size());
        result = mLocalStorage.get(maxreqdate3, TRANSACTION_SUCCESS);
        assertEquals("put", maxreqdate3, result.get(0).maxreqdate);
    }

    @Test
    public void updateOutOfDataUnknowTimestamp() {
        mLocalStorage.updateOutOfData(maxreqdate, TRANSACTION_SUCCESS, true);

        List<TransactionFragmentEntity> result = mLocalStorage.get(maxreqdate, TRANSACTION_SUCCESS);
        assertEquals("updateOutOfData with unknow timestamp", 0, result.size());
    }

    @Test
    public void updateOutOfData() {
        initData();
        insertTransaction();

        mLocalStorage.updateOutOfData(maxreqdate2, TRANSACTION_SUCCESS, true);

        List<TransactionFragmentEntity> result = mLocalStorage.get(maxreqdate2, TRANSACTION_SUCCESS);
        assertEquals("updateOutOfData", true, result.get(0).outofdata);
    }

    @Test
    public void getUnknownTimestamp() {
        initData();
        insertTransaction();

        List<TransactionFragmentEntity> result = mLocalStorage.get(0, TRANSACTION_SUCCESS);
        assertEquals("get unknown timestamp", 0, result.size());
    }

    @Test
    public void get() {
        initData();
        insertTransaction();

        List<TransactionFragmentEntity> result = mLocalStorage.get(1480000390000L, TRANSACTION_SUCCESS);
        assertEquals("get", 2, result.size());
    }

    @Test
    public void getLatestFragmentWhenNotHavingDB() {
        TransactionFragmentEntity result = mLocalStorage.getLatestFragment(TRANSACTION_SUCCESS);
        assertEquals("get latest fragment when not having db", null, result);
    }

    @Test
    public void getLatestFragment() {
        initData();
        insertTransaction();

        TransactionFragmentEntity result = mLocalStorage.getLatestFragment(TRANSACTION_SUCCESS);
        assertEquals("get latest fragment", maxreqdate, result.maxreqdate);
    }

    @Test
    public void remove() {
        initData();
        insertTransaction();

        mLocalStorage.remove(minreqdate);
        List<TransactionFragmentEntity> result = mLocalStorage.get(maxreqdate, TRANSACTION_SUCCESS);
        assertEquals("remove", true, Lists.isEmptyOrNull(result));
    }

    @Test
    public void removeUnknowTimestamp() {
        mLocalStorage.remove(minreqdate);
        List<TransactionFragmentEntity> result = mLocalStorage.get(maxreqdate, TRANSACTION_SUCCESS);
        assertEquals("remove unknow timestamp", 0, result.size());
    }
}