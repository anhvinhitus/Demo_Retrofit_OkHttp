package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionLocalStorageTest extends ApplicationTestCase {
    private static final String JSON_TRANSACTION = "{\n" +
            "      \"userid\": \"160525000004005\",\n" +
            "      \"transid\": 161111000000030,\n" +
            "      \"appid\": 1,\n" +
            "      \"appuser\": \"160525000004003\",\n" +
            "      \"platform\": \"android\",\n" +
            "      \"description\": \"sadf asd a\",\n" +
            "      \"pmcid\": 38,\n" +
            "      \"reqdate\": 1478834599840,\n" +
            "      \"userchargeamt\": 12000,\n" +
            "      \"amount\": 10000,\n" +
            "      \"userfeeamt\": 2000,\n" +
            "      \"type\": 4,\n" +
            "      \"sign\": 1,\n" +
            "      \"username\": \"Mạnh Hiếu\",\n" +
            "      \"appusername\": \"Long Lê Văn\",\n" +
            "      \"transstatus\": 1,\n" +
            "      \"isretry\": false,\n" +
            "      \"isrefundsucc\": 0\n" +
            "    }";

    private TransactionStore.LocalStorage mLocalStorage;

    private Gson mGson;

    private final int TRANSACTION_STATUS_SUCCESS = 1;
    private final int TRANSACTION_STATUS_FAIL = 2;

    private final int TRANSACTION_SIZE = 20; //row count jsonObject

    private List<TransHistoryEntity> entities = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionLocalStorage(daoSession);

        initData();
    }

    private void initData() {
        mGson = new Gson();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            TransHistoryEntity entity = mGson.fromJson(JSON_TRANSACTION, TransHistoryEntity.class);
            int j = i + 1;
            entity.transid = j;
            entity.appid = j;
            entity.userid = "user" + j;
            entity.statustype = TRANSACTION_STATUS_SUCCESS;
            entities.add(entity);
        }
    }

    private void insertTransaction() {
        mLocalStorage.put(entities);
    }


    @Test
    public void putTest() {
        insertTransaction();
        List<TransHistoryEntity> result = mLocalStorage.get(0, TRANSACTION_SIZE, TRANSACTION_STATUS_SUCCESS);
        assertTrue(result.containsAll(result));
    }

    @Test
    public void isHaveTransactionInDb() {

        insertTransaction();

        boolean result = mLocalStorage.isHaveTransactionInDb();
        assertEquals(result, true);
    }


    @Test
    public void updateStatusType() {
        insertTransaction();
        long transid = 1;

        mLocalStorage.updateStatusType(transid, TRANSACTION_STATUS_SUCCESS);

        TransHistoryEntity result = mLocalStorage.getTransaction(transid);
        assertEquals(result.statustype, TRANSACTION_STATUS_SUCCESS);

        mLocalStorage.updateStatusType(transid, TRANSACTION_STATUS_FAIL);

        TransHistoryEntity resultFail = mLocalStorage.getTransaction(transid);

        assertEquals(resultFail.statustype, TRANSACTION_STATUS_FAIL);
    }

    @Test
    public void setLoadedTransactionSuccess() {
        mLocalStorage.setLoadedTransactionSuccess(true);
        boolean ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals(ret, true);

    }

    @Test
    public void setLoadedTransactionFail() {
        mLocalStorage.setLoadedTransactionFail(true);
        boolean ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals(ret, true);
    }

    public void getLatestTimeTransaction() {
        long latestTime = mLocalStorage.getLatestTimeTransaction(TRANSACTION_STATUS_FAIL);
        long oldestTime = mLocalStorage.getOldestTimeTransaction(TRANSACTION_STATUS_FAIL);
    }

    public void getOldestTimeTransaction() {
        long latestTime = mLocalStorage.getLatestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        long oldestTime = mLocalStorage.getOldestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
    }
}
