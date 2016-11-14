package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by hieuvm on 11/14/16.
 */

public class TransactionLocalStorageTest extends ApplicationTestCase {

    private final String sResponseTransHistorySuccess = "{\"data\":[{\"userid\":\"160525000004005\",\"transid\":161114000000353,\"appid\":1,\"appuser\":\"160713000001505\",\"platform\":\"android\",\"description\":\"hdhdhdhd\",\"pmcid\":38,\"reqdate\":1479110258485,\"userchargeamt\":13111,\"amount\":11111,\"userfeeamt\":2000,\"type\":4,\"sign\":-1,\"username\":\"Mạnh Hiếu\",\"appusername\":\"Anh\",\"transstatus\":1,\"isretry\":false,\"isrefundsucc\":0}],\"returncode\":1,\"returnmessage\":\"\"}";

    private TransactionStore.LocalStorage mLocalStorage;

    private Gson mGson;

    private long transid = 1000;

    private final int TRANSACTION_STATUS_SUCCESS = 1;
    private final int TRANSACTION_STATUS_FAIL = 2;

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mGson = new Gson();
        mLocalStorage = new TransactionLocalStorage(daoSession);
    }


    @Test
    public void putTest() {
        TransactionHistoryResponse response = mGson.fromJson(sResponseTransHistorySuccess, TransactionHistoryResponse.class);
        response.data.get(0).transid = transid;

        mLocalStorage.put(response.data);
        List<TransHistoryEntity> result = mLocalStorage.get(0, response.data.size(), TRANSACTION_STATUS_SUCCESS);
        assertTrue(response.data.equals(result));
    }

/*    @Test
    public void isHaveTransactionInDb() {
        boolean result = mLocalStorage.isHaveTransactionInDb();
        assertEquals(result, true);
    }*/

    @Test
    public void updateStatusType() {
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
