package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransactionLog;
import vn.com.vng.zalopay.data.util.Lists;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
            "      \"transstatus\": 0,\n" +
            "      \"isretry\": false,\n" +
            "      \"isrefundsucc\": 0\n" +
            "    }";

    private TransactionStore.LocalStorage mLocalStorage;

    private Gson mGson;

    private final int TRANSACTION_STATUS_SUCCESS = 1;
    private final int TRANSACTION_STATUS_FAIL = 2;

    private final int TRANSACTION_SIZE = 20;

    private List<TransHistoryEntity> entities = new ArrayList<>();
    private TransHistoryEntity entity;

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionLocalStorage(daoSession);
    }

    private void initData() {
        mGson = new Gson();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            entity = mGson.fromJson(JSON_TRANSACTION, TransHistoryEntity.class);
            int j = i + 1;
            entity.transid = j;
            entity.appid = j;
            entity.userid = "user" + j;
            entity.reqdate += j;
            entity.statustype = TRANSACTION_STATUS_SUCCESS;
            entities.add(entity);
        }
    }

    private void insertTransaction() {
        mLocalStorage.put(entities);
    }

    @Test
    public void putNullParam() {
        mLocalStorage.put(null);
        List<TransHistoryEntity> result = mLocalStorage.get(0, TRANSACTION_SIZE, TRANSACTION_STATUS_SUCCESS);
        assertEquals("putNullParam", 0, result.size());
    }

    @Test
    public void putEmptyList() {
        insertTransaction();
        List<TransHistoryEntity> result = mLocalStorage.get(0, TRANSACTION_SIZE, TRANSACTION_STATUS_SUCCESS);
        assertEquals("putEmptyList", 0, result.size());
    }

    @Test
    public void put() {
        initData();
        insertTransaction();
        List<TransHistoryEntity> result = mLocalStorage.get(0, TRANSACTION_SIZE, TRANSACTION_STATUS_SUCCESS);
        assertTrue("put", result.containsAll(entities));
    }

    @Test
    public void getTransactionsWithEmptyDB() {
        initData();
        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 0;
        limit = TRANSACTION_SIZE;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_SUCCESS);
        assertEquals("get transactions with empty DB", 0, result.size());
    }

    @Test
    public void getAllTransactions() {
        initData();
        insertTransaction();

        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 0;
        limit = TRANSACTION_SIZE;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_FAIL);
        for(int i = 0; i < result.size(); i++) {
            compare2Elements(result.get(i), entities.get(i));
        }
    }

    @Test
    public void get0Transaction() {
        initData();
        insertTransaction();

        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 0;
        limit = 0;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_SUCCESS);
        assertEquals("get 0 transaction", 0, result.size());
    }

    @Test
    public void get() {
        initData();
        insertTransaction();

        initData();
        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 0;
        limit = 5;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_SUCCESS);
        assertEquals("get", true, result.containsAll(entities.subList(
                TRANSACTION_SIZE - (pageIndex + 1) * limit, TRANSACTION_SIZE - (pageIndex + 1) * limit + limit)
        ));
    }

    @Test
    public void getOversizedList() {
        initData();
        insertTransaction();

        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 5;
        limit = 5;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_SUCCESS);
        assertEquals("get oversized list", 0, result.size());
    }

    @Test
    public void getWithWrongFormatOfStatusType() {
        initData();
        insertTransaction();

        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 0;
        limit = TRANSACTION_SIZE;
        result = mLocalStorage.get(pageIndex, limit, -3);
        assertEquals("get with wrong format of status type", 0, result.size());
    }

    @Test
    public void getTransactionWhenLimitIsANegativeNumber() {
        initData();
        insertTransaction();

        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = 0;
        limit = -1;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_SUCCESS);
        System.out.print(result.size() + " - " + result);
        assertEquals("get transaction with limit is a negative number", 0, result.size());
    }

    @Test
    public void getTransactionWhenPageIndexIsANegativeNumber() {
        initData();
        insertTransaction();

        List<TransHistoryEntity> result;
        int pageIndex, limit;

        pageIndex = -1;
        limit = 4;
        result = mLocalStorage.get(pageIndex, limit, TRANSACTION_STATUS_SUCCESS);
        System.out.print(result.size() + " - " + result);
        assertEquals("get transaction with pageIndex is a negative number", 0, result.size());
    }

    @Test
    public void isHaveTransactionInDbWhenDBHasntInitialized() {
        boolean result;

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionLocalStorage(daoSession);
        result = mLocalStorage.isHaveTransactionInDb();
        assertEquals("isHaveTransactionInDb when DB hasn't initialized", false, result);
    }

    @Test
    public void isHaveTransactionInDbWhenDBInitialized() {
        boolean result;

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionLocalStorage(daoSession);
        result = mLocalStorage.isHaveTransactionInDb();
        assertEquals("isHaveTransactionInDb when DB initialized", false, result);

    }

    @Test
    public void isHaveTransactionInDb() {
        initData();
        insertTransaction();

        boolean result;

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        daoSession.getTransactionLogDao().insertOrReplaceInTx(transform(entities));
        mLocalStorage = new TransactionLocalStorage(daoSession);
        result = mLocalStorage.isHaveTransactionInDb();
        assertEquals("isHaveTransactionInDb when DB had datas", true, result);
    }

    @Test
    public void isHaveTransactionInDbWithEmptyDB() {
        initData();
        insertTransaction();

        boolean result;

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        daoSession.getTransactionLogDao().insertOrReplaceInTx(transform(entities));
        daoSession.getTransactionLogDao().deleteAll();
        mLocalStorage = new TransactionLocalStorage(daoSession);
        result = mLocalStorage.isHaveTransactionInDb();
        assertEquals("isHaveTransactionInDb when DB has deleted all datas", false, result);
    }

    @Test
    public void getTransactionWithEmptyDB() {
        assertEquals("getTransaction with empty DB",
                null, mLocalStorage.getTransaction(0));
    }

    @Test
    public void getTransaction() {
        initData();
        insertTransaction();

        compare2Elements(entities.get(1), mLocalStorage.getTransaction(2));
    }

    @Test
    public void getTransactionWithWrongFormatTransId() {
        initData();
        insertTransaction();
        assertEquals("getTransaction with wrong format transId",
                null, mLocalStorage.getTransaction(-1));
    }

    @Test
    public void getTransactionWithOversizedTransId() {
        initData();
        insertTransaction();
        assertEquals("getTransaction with oversized transId",
                null, mLocalStorage.getTransaction(21));
    }

    @Test
    public void updateStatusTypeWithSuccessType() {
        initData();
        insertTransaction();

        long transid = 1;
        TransHistoryEntity result;

        mLocalStorage.updateStatusType(transid, TRANSACTION_STATUS_SUCCESS);
        result = mLocalStorage.getTransaction(transid);
        assertEquals("updateStatusType: test with success type", TRANSACTION_STATUS_SUCCESS, result.statustype);
    }

    @Test
    public void updateStatusTypeWithFailType() {
        initData();
        insertTransaction();

        long transid = 1;
        TransHistoryEntity result;

        mLocalStorage.updateStatusType(transid, TRANSACTION_STATUS_FAIL);
        result = mLocalStorage.getTransaction(transid);
        assertEquals("getTransaction: test with fail type", TRANSACTION_STATUS_FAIL, result.statustype);
    }

    @Test
    public void setLoadedTransactionSuccessWithTrueParam() {
        initData();
        insertTransaction();

        boolean ret;

        mLocalStorage.setLoadedTransactionSuccess(true);
        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("setLoadedTransactionSuccess: test with param is true", true, ret);
    }

    @Test
    public void setLoadedTransactionSuccessWithFailParam() {
        initData();
        insertTransaction();

        boolean ret;

        mLocalStorage.setLoadedTransactionSuccess(false);
        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("setLoadedTransactionSuccess: test with param is false", false, ret);
    }

    @Test
    public void setLoadedTransactionFailWithTrueParam() {
        initData();
        insertTransaction();

        boolean ret;

        mLocalStorage.setLoadedTransactionFail(true);
        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("setLoadedTransactionFail with param is true", true, ret);
    }

    @Test
    public void setLoadedTransactionFailWithFailParam() {
        initData();
        insertTransaction();

        boolean ret;

        mLocalStorage.setLoadedTransactionFail(false);
        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("setLoadedTransactionFail with param is false", false, ret);
    }

    @Test
    public void isLoadedTransactionSuccessWithEmptyDB() {
        boolean ret;

        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("isLoadedTransactionSuccess when DB doesn't have datas", false, ret);
    }

    @Test
    public void isLoadedTransactionSuccessWhenSetLoadedTransactionSuccessIsTrue() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionSuccess(true);
        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionSuccess is true", true, ret);
    }

    @Test
    public void isLoadedTransactionSuccessWhenSetLoadedTransactionSuccessIsFalse() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionSuccess(false);
        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionSuccess is false", false, ret);
    }

    @Test
    public void isLoadedTransactionSuccessWhenSetLoadedTransactionFailIsFalse() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionFail(false);
        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionFail is false", false, ret);
    }

    @Test
    public void isLoadedTransactionSuccessWhenSetLoadedTransactionFailIsTrue() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionFail(true);
        ret = mLocalStorage.isLoadedTransactionSuccess();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionFail is true", false, ret);
    }

    @Test
    public void isLoadedTransactionFailWithEmptyDB() {
        boolean ret;

        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("isLoadedTransactionSuccess: test when DB doesn't have datas", false, ret);
    }

    @Test
    public void isLoadedTransactionFailWhenSetLoadedTransactionSuccessIsTrue() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionSuccess(true);
        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionSuccess is true", false, ret);
    }

    @Test
    public void isLoadedTransactionFailWhenSetLoadedTransactionSuccessIsFalse() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionSuccess(false);
        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionSuccess is false", false, ret);
    }

    @Test
    public void isLoadedTransactionFailWhenSetLoadedTransactionFailIsFalse() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionFail(false);
        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionFail is false", false, ret);
    }

    @Test
    public void isLoadedTransactionSFailWhenSetLoadedTransactionFailIsTrue() {
        initData();
        insertTransaction();
        boolean ret;

        mLocalStorage.setLoadedTransactionFail(true);
        ret = mLocalStorage.isLoadedTransactionFail();
        assertEquals("isLoadedTransactionSuccess when setLoadedTransactionFail is true", true, ret);
    }

    @Test
    public void getLatestTimeTransactionWithEmptyDBAndSuccessType() {
        long latestTime;

        latestTime = mLocalStorage.getLatestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        assertEquals("getLatestTimeFailTransaction get success time when DB doesn't have datas", 0, latestTime);
    }

    @Test
    public void getLatestTimeTransactionWithEmptyDBAndFailType() {
        long latestTime;

        latestTime = mLocalStorage.getLatestTimeTransaction(TRANSACTION_STATUS_FAIL);
        assertEquals("getLatestTimeFailTransaction get fail time when DB doesn't have datas", 0, latestTime);
    }

    @Test
    public void getLatestTimeTransactionWithSuccessType() {
        initData();
        insertTransaction();
        long latestTime;

        latestTime = mLocalStorage.getLatestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        assertEquals("getLatestTimeSuccessTransaction", true, latestTime == entities.get(entities.size() - 1).reqdate);
    }

    @Test
    public void getLatestTimeTransactionWithFailType() {
        initData();
        insertTransaction();
        long latestTime;

        latestTime = mLocalStorage.getLatestTimeTransaction(TRANSACTION_STATUS_FAIL);
        assertEquals("getLatestTimeFailTransaction", true, latestTime == 0);
    }

    @Test
    public void getOldestTimeTransactionWithEmptyDBAndSuccessType() {
        long oldestTime;

        oldestTime = mLocalStorage.getOldestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        assertEquals("getOldestTimeTransaction: test get success time when DB doesn't have datas", 0, oldestTime);
    }

    @Test
    public void getOldestTimeTransactionWithEmptyDBAndFailType() {
        long oldestTime;

        oldestTime = mLocalStorage.getOldestTimeTransaction(TRANSACTION_STATUS_FAIL);
        assertEquals("getOldestTimeTransaction: test get fail time when DB doesn't have datas", 0, oldestTime);
    }

    @Test
    public void getOldestTimeTransactionWithSuccessType() {
        initData();
        insertTransaction();
        long oldestTime;

        oldestTime = mLocalStorage.getOldestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        assertEquals("getOldestTimeSuccessTransaction", entities.get(0).reqdate, oldestTime);
    }

    @Test
    public void getOldestTimeTransactionWithFailType() {
        initData();
        insertTransaction();
        long oldestTime;

        oldestTime = mLocalStorage.getOldestTimeTransaction(TRANSACTION_STATUS_FAIL);
        assertEquals("getOldestTimeFailTransaction", 0, oldestTime);
    }

    private List<TransactionLog> transform(Collection<TransHistoryEntity> transHistoryEntities) {
        if (Lists.isEmptyOrNull(transHistoryEntities)) {
            return emptyList();
        }

        List<TransactionLog> transactionLogs = new ArrayList<>(transHistoryEntities.size());
        for (TransHistoryEntity transHistoryEntity : transHistoryEntities) {
            TransactionLog transactionLog = transform(transHistoryEntity);
            if (transactionLog == null) {
                continue;
            }

            transactionLogs.add(transactionLog);
        }

        return transactionLogs;
    }

    private TransactionLog transform(TransHistoryEntity transEntity) {
        if (transEntity == null) {
            return null;
        }

        TransactionLog transDao = new TransactionLog();
        transDao.transid = transEntity.transid;
        transDao.appuser = (transEntity.appuser);
        transDao.appid = (transEntity.appid);
        transDao.description = (transEntity.description);
        transDao.userchargeamt = (transEntity.userchargeamt);
        transDao.userfeeamt = (transEntity.userfeeamt);
        transDao.amount = (transEntity.amount);
        transDao.platform = (transEntity.platform);
        transDao.pmcid = (transEntity.pmcid);
        transDao.type = (transEntity.type);
        transDao.reqdate = (transEntity.reqdate);
        transDao.userid = (transEntity.userid);
        transDao.sign = (transEntity.sign);
        transDao.username = (transEntity.username);
        transDao.appusername = (transEntity.appusername);
        transDao.statustype = (transEntity.statustype);
        return transDao;
    }

    private void compare2Elements(TransHistoryEntity b1, TransHistoryEntity b2) {
        if (b1 == null && b2 != null) {
            fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            fail("Compare null and non-null object");
            return;
        }

        assertEquals("userid", b1.userid, b2.userid);
        assertEquals("transid", b1.transid, b2.transid);
        assertEquals("appid", b1.appid, b2.appid);
        assertEquals("appuser", b1.appuser, b2.appuser);
        assertEquals("platform", b1.platform, b2.platform);
        assertEquals("description", b1.description, b2.description);
        assertEquals("pmcid", b1.pmcid, b2.pmcid);
        assertEquals("reqdate", b1.reqdate, b2.reqdate);
        assertEquals("userchargeamt", b1.userchargeamt, b2.userchargeamt);
        assertEquals("amount", b1.amount, b2.amount);
        assertEquals("userfeeamt", b1.userfeeamt, b2.userfeeamt);
        assertEquals("type", b1.type, b2.type);
        assertEquals("sign", b1.sign, b2.sign);
        assertEquals("username", b1.username, b2.username);
        assertEquals("appusername", b1.appusername, b2.appusername);
        assertEquals("transstatus", b1.transstatus, b2.transstatus);
        assertEquals("isretry", b1.isretry, b2.isretry);
    }
}
