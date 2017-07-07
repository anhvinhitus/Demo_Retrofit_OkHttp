package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Query;
import rx.Observable;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.CustomObserver;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.entity.TransactionFragmentEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.model.User;

public class TransactionRepositoryTest extends ApplicationTestCase {
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

    private TransactionStore.Repository mRepository;

    private static final int TRANSACTION_STATUS_FAIL = 2;

    private final int TRANSACTION_SIZE = 20;

    private List<TransHistoryEntity> entities = new ArrayList<>();
    private List<TransHistoryEntity> serverEntities = new ArrayList<>();
    private List<TransactionFragmentEntity> fragmentEntities = new ArrayList<>();

    private long maxreqdate = 1478834599840L;
    private long maxreqdate2 = 1478834598840L;
    private long timestamp = 1478834599800L;
    private long timestamp2 = 1478834598800L;
    private int sign = 0;
    private List<Integer> types = new ArrayList<>();

    private TransactionHistoryResponse transactionHistoryResponse;
    private TransactionStore.LocalStorage mLocalStorage;
    private TransactionFragmentStore.LocalStorage mFragmentLocalStorage;
    private TransactionStore.RequestService mRequestService = null;
    private ZaloPayEntityDataMapper mMapper;
    private User mUser;

    public class RequestServiceImpl implements TransactionStore.RequestService {
        @Override
        public Observable<TransactionHistoryResponse> getTransactionHistories(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("statustype") int statustype) {
            return Observable.just(transactionHistoryResponse);
        }
    }

    @Before
    public void setUp() throws Exception {
        initData();

        mMapper = new ZaloPayEntityDataMapper();
        mUser = new User("");
        mUser.accesstoken = "";

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionLocalStorage(daoSession);
        mFragmentLocalStorage = new TransactionFragmentLocalStorage(daoSession);
    }

    private void initData() {
        Gson mGson = new Gson();

        types.add(4);

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            TransHistoryEntity entity = mGson.fromJson(JSON_TRANSACTION, TransHistoryEntity.class);
            int j = i + 1;
            entity.transid = j;
            entity.appid = j;
            entity.userid = "user" + j;
            entity.reqdate -= 10 * i;
            entity.statustype = TRANSACTION_STATUS_FAIL;
            entities.add(entity);
        }

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            TransHistoryEntity entity = mGson.fromJson(JSON_TRANSACTION, TransHistoryEntity.class);
            int j = i + 1;
            entity.transid = j;
            entity.appid = j;
            entity.userid = "user" + (20 + j);
            entity.reqdate -= 1000 * j;
            entity.statustype = TRANSACTION_STATUS_FAIL;
            serverEntities.add(entity);
        }

        TransactionFragmentEntity entity = new TransactionFragmentEntity();
        entity.statustype = TRANSACTION_STATUS_FAIL;
        entity.maxreqdate = entities.get(0).reqdate;
        entity.minreqdate = entities.get(entities.size() - 1).reqdate;
        fragmentEntities.add(entity);

        transactionHistoryResponse = new TransactionHistoryResponse();
        transactionHistoryResponse.data = new ArrayList<>();
    }

    private void putFragmentDB() {
        for (int i = 0; i < fragmentEntities.size(); i++) {
            mFragmentLocalStorage.put(fragmentEntities.get(i));
        }
    }

    @Test
    public void getTransactionsWithEmptyData() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage, new RequestServiceImpl(), EventBus.getDefault());

        pageIndex = 2;
        count = 5;
        mRepository.getTransactionsSuccess(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess when not having data (both from local and cloud)", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsWithCountIsANegativeNumber() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        putFragmentDB();
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = -1;
        mRepository.getTransactionsSuccess(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess with data from clound and count = -1", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsWithPageIndexIsANegativeNumber() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = -1;
        count = 10;
        mRepository.getTransactionsSuccess(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess with datas from cloud and pageIndex = -1", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsWithDataFromCloud() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 10;
        count = 5;
        mRepository.getTransactionsSuccess(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));
        int startIdx = (int)(maxreqdate - timestamp) / 10 + offset;
        assertEquals(transactionHistoryResponse.data.subList(startIdx, startIdx + count), result.get(0).second);
    }

    @Test
    public void getNoneOfTransactionsWithDataFromCloud() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 0;
        count = 0;
        mRepository.getTransactionsSuccess(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess: get 0 data from clound", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsWithDataFromLocal() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 10;
        count = 5;
        mRepository.getTransactionsSuccess(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess from local when not having any datas with success type", 0, result.get(0).second.size());
    }

    @Test
    public void getNoneOfTransactionsWithDataFromLocal() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 0;
        count = 0;
        mRepository.getTransactionsSuccess(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess get 0 datas from local", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsWithDataFromCloudAndStorage() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 10;
        count = 5;
        mRepository.getTransactionsSuccess(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));

        int startIdx = (int)(maxreqdate - timestamp) / 10 + offset;
        List<TransHistoryEntity> getDataFromCloudList = transactionHistoryResponse.data.subList(startIdx, startIdx + count);
        List<TransHistoryEntity> getDataFromLocalList = entities.subList(startIdx, startIdx + count);
        getDataFromLocalList.addAll(getDataFromCloudList);
        assertEquals(getDataFromLocalList, result.get(0).second);
    }

    @Test
    public void getTransactionsWithNonDataInDB() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = serverEntities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = TRANSACTION_SIZE;
        mRepository.getTransactionsSuccess(maxreqdate2, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));

        assertEquals(transactionHistoryResponse.data, result.get(0).second);
    }

    @Test
    public void getNoneOfTransactionsWithDataFromCloudAndStorage() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = 0;
        mRepository.getTransactionsSuccess(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsSuccess: get 0 data from clound and local", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsFailWithEmptyData() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 2;
        count = 5;
        mRepository.getTransactionsFail(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsFail when not having data (both from local and cloud)", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsFailWithCountIsANegativeNumber() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = -1;
        mRepository.getTransactionsFail(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsFail with data from clound and count = -1", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsFailWithPageIndexIsANegativeNumber() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = -1;
        count = 10;
        mRepository.getTransactionsFail(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsFail with datas from clound and pageIndex = -1", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsFailWithDataFromCloud() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 2;
        count = 5;
        mRepository.getTransactionsFail(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));
        int startIdx = (int)(maxreqdate - timestamp) / 10 + offset;
        assertEquals(transactionHistoryResponse.data.subList(startIdx, startIdx + count), result.get(0).second);
    }

    @Test
    public void getNoneOfTransactionsFailWithDataFromCloud() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = 0;
        mRepository.getTransactionsFail(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsFail: get 0 data from clound", 0, result.get(0).second.size());
    }

    @Test
    public void getNoneOfTransactionsFailWithDataFromLocal() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = 0;
        mRepository.getTransactionsFail(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsFail get 0 datas from local", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionsFailWithDataFromCloudAndStorage() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int offset, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        offset = 10;
        count = 5;
        mRepository.getTransactionsFail(timestamp, types, offset, count, sign).subscribe(new CustomObserver<>(result));

        int startIdx = (int)(maxreqdate - timestamp) / 10 + offset;
        List<TransHistoryEntity> getDataFromCloudList = transactionHistoryResponse.data.subList(startIdx, startIdx + count);
        List<TransHistoryEntity> getDataFromLocalList = entities.subList(startIdx, startIdx + count);
        getDataFromLocalList.addAll(getDataFromCloudList);
        assertEquals(getDataFromLocalList, result.get(0).second);
    }

    @Test
    public void getTransactionsFailWithNonDataInDB() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = serverEntities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = TRANSACTION_SIZE;
        mRepository.getTransactionsFail(maxreqdate2, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("getTransactionsFail with non data in db",
                maxreqdate2, mFragmentLocalStorage.get(timestamp2, TRANSACTION_STATUS_FAIL).get(0).maxreqdate);
        assertEquals(transactionHistoryResponse.data, result.get(0).second);
    }

    @Test
    public void getNoneOfTransactionsFailWithDataFromCloudAndStorage() {
        List<Pair<Integer, List<TransHistory>>> result = new ArrayList<>();
        int pageIndex, count;

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        pageIndex = 0;
        count = 0;
        mRepository.getTransactionsFail(timestamp, types, pageIndex, count, sign).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionsFail: get 0 data from clound and local", 0, result.get(0).second.size());
    }

    @Test
    public void getTransactionWithEmptyDB() {
        final List<TransHistory> result = new ArrayList<>();
        long id = 1;

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.getTransaction(id).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionByID: when not having data", null, result.get(0));
    }

    @Test
    public void getTransactionFromLocal() {
        final List<TransHistory> result = new ArrayList<>();
        long id = 1;

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.getTransaction(id).subscribe(new CustomObserver<>(result));
        assertElementEquals(result.get(0), entities.get((int) id - 1));
    }

    @Test
    public void getTransactionFromLocalWithWrongFormatTransId() {
        final List<TransHistory> result = new ArrayList<>();
        long id = -1;

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.getTransaction(id).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionByID from local with id = -1", null, result.get(0));
    }

    @Test
    public void getTransactionFromLocalWithOversizedTransId() {
        final List<TransHistory> result = new ArrayList<>();
        long id = 21;

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.getTransaction(id).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionByID from local id = 21", null, result.get(0));
    }

    @Test
    public void getTransactionFromCloud() {
        final List<TransHistory> result = new ArrayList<>();
        long id = 1;

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.getTransaction(id).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("getTransactionByID from cloud", null, result.get(0));
    }

    @Test
    public void getTransactionFromLocalCloud() {
        final List<TransHistory> result = new ArrayList<>();
        long id = 1;

        mLocalStorage.put(entities);
        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.getTransaction(id).subscribe(new CustomObserver<>(result));
        assertElementEquals(result.get(0), entities.get((int) id - 1));
    }

    @Test
    public void isLoadedTransactionSuccess() {
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionSuccess();
        Assert.assertEquals("isLoadedTransactionSuccess", false, result);
    }

    @Test
    public void isLoadedTransactionSuccessWithSetLoadedSuccessIsTrue() {
        mLocalStorage.setLoadedTransactionSuccess(true);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionSuccess();
        Assert.assertEquals("isLoadedTransactionSuccess when set loaded success local is true", true, result);
    }

    @Test
    public void isLoadedTransactionSuccessWithSetLoadedSuccessIsFalse() {
        mLocalStorage.setLoadedTransactionSuccess(false);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionSuccess();
        Assert.assertEquals("isLoadedTransactionSuccess when set loaded success local is false", false, result);
    }

    @Test
    public void isLoadedTransactionSuccessWithSetLoadedFalseIsTrue() {
        mLocalStorage.setLoadedTransactionFail(true);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionSuccess();
        Assert.assertEquals("isLoadedTransactionSuccess when set loaded fail local is true", false, result);
    }

    @Test
    public void isLoadedTransactionSuccessWithSetLoadedFalseIsFalse() {
        mLocalStorage.setLoadedTransactionFail(false);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionSuccess();
        Assert.assertEquals("isLoadedTransactionSuccess when set loaded fail local is false", false, result);
    }

    @Test
    public void isLoadedTransactionFail() {
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionFail();
        Assert.assertEquals("isLoadedTransactionFail", false, result);
    }

    @Test
    public void isLoadedTransactionFailWithSetLoadedSuccessIsTrue() {
        mLocalStorage.setLoadedTransactionSuccess(true);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionFail();
        Assert.assertEquals("isLoadedTransactionFail when set loaded success local is true", false, result);
    }

    @Test
    public void isLoadedTransactionFailWithSetLoadedSuccessIsFalse() {
        mLocalStorage.setLoadedTransactionSuccess(false);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionFail();
        Assert.assertEquals("isLoadedTransactionFail when set loaded success local is false", false, result);
    }

    @Test
    public void isLoadedTransactionFailWithSetLoadedFalseIsTrue() {
        mLocalStorage.setLoadedTransactionFail(true);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionFail();
        Assert.assertEquals("isLoadedTransactionFail when set loaded fail local is true", true, result);
    }

    @Test
    public void isLoadedTransactionFailWithSetLoadedFalseIsFalse() {
        mLocalStorage.setLoadedTransactionFail(false);
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        boolean result = mRepository.isLoadedTransactionFail();
        Assert.assertEquals("isLoadedTransactionFail when set loaded fail local is false", false, result);
    }

    @Test
    public void fetchTransactionHistorySuccessLatestWithEmptyDB() {
        final List<Boolean> result = new ArrayList<>();

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistorySuccessLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistorySuccessLatest with empty DB", 0, result.size());
    }

    @Test
    public void fetchTransactionHistorySuccessLatestWithNullCloud() {
        final List<Boolean> result = new ArrayList<>();

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistorySuccessLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistorySuccessLatest with only data from local", 0, result.size());
    }

    @Test
    public void fetchTransactionHistorySuccessLatestWithNullLocal() {
        final List<Boolean> result = new ArrayList<>();

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistorySuccessLatest().subscribe(new CustomObserver<>(result));
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("fetchTransactionHistorySuccessLatest with only data from cloud", true, result.get(0));
        }
    }

    @Test
    public void fetchTransactionHistorySuccessLatest() {
        final List<Boolean> result = new ArrayList<>();

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistorySuccessLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistorySuccessLatest", true, result.get(0));
    }

    @Test
    public void fetchTransactionHistoryFailLatestWithEmptyDB() {
        final List<Boolean> result = new ArrayList<>();

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryFailLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryFailLatest with empty DB", 0, result.size());
    }

    @Test
    public void fetchTransactionHistoryFailLatestWithNullCloud() {
        final List<Boolean> result = new ArrayList<>();

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryFailLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryFailLatest with only data from local", 0, result.size());
    }

    @Test
    public void fetchTransactionHistoryFailLatestWithNullLocal() {
        final List<Boolean> result = new ArrayList<>();

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryFailLatest().subscribe(new CustomObserver<>(result));
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("fetchTransactionHistoryFailLatest with only data from cloud", true, result.get(0));
        }
    }

    @Test
    public void fetchTransactionHistoryFailLatest() {
        final List<Boolean> result = new ArrayList<>();

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryFailLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryFailLatest", true, result.get(0));
    }

    @Test
    public void fetchTransactionHistoryLatestWithEmptyDB() {
        final List<Boolean> result = new ArrayList<>();

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryLatest with empty DB", 0, result.size());
    }

    @Test
    public void fetchTransactionHistoryLatestWithNullCloud() {
        final List<Boolean> result = new ArrayList<>();

        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryLatest with only data from local", 0, result.size());
    }

    @Test
    public void fetchTransactionHistoryLatestWithNullLocal() {
        final List<Boolean> result = new ArrayList<>();

        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryLatest().subscribe(new CustomObserver<>(result));
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("fetchTransactionHistoryLatest with only data from cloud", true, result.get(0));
        }
    }

    @Test
    public void fetchTransactionHistoryLatest() {
        final List<Boolean> result = new ArrayList<>();

        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        mRepository.fetchTransactionHistoryLatest().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryLatest", true, result.get(0));
    }

    @Test
    public void fetchTransactionHistoryOldestWithThresholdTime() {
        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        final List<Boolean> result = new ArrayList<>();
        long time = transactionHistoryResponse.data.get(3).reqdate;

        mRepository.fetchTransactionHistoryOldest(time).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryOldestWithThresholdTime", true, result.get(0));
    }

    @Test
    public void fetchTransactionHistoryOldestWithTimeEqual0() {
        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        final List<Boolean> result = new ArrayList<>();
        long time = 0;

        mRepository.fetchTransactionHistoryOldest(time).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryOldest with time equal 0", 0, result.size());
    }

    @Test
    public void fetchTransactionHistoryLatestWithThresholdTime() {
        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        final List<Boolean> result = new ArrayList<>();
        long time = transactionHistoryResponse.data.get(3).reqdate;

        mRepository.fetchTransactionHistoryLatest(time).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryLatestWithThresholdTime", true, result.get(0));
    }

    @Test
    public void fetchTransactionHistoryLatestWithTimeEqual0() {
        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());

        final List<Boolean> result = new ArrayList<>();
        long time = 0;

        mRepository.fetchTransactionHistoryLatest(time).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("fetchTransactionHistoryLatestWithThresholdTime", 0, result.size());
    }

    @Test
    public void reloadTransactionHistoryWithTime() {
        transactionHistoryResponse.data = entities;
        mLocalStorage.put(entities);
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mFragmentLocalStorage,
                mRequestService, EventBus.getDefault());


        TransHistoryEntity entity = entities.get(19);
        long time = entity.reqdate;

        final List<TransHistory> result = new ArrayList<>();
        mRepository.reloadTransactionHistory(entity.transid, time).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("reloadTransactionHistoryWithTime", true, !result.isEmpty());
    }

    private void assertElementEquals(TransHistory b1, TransHistoryEntity b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("userid", b1.userid, b2.userid);
        Assert.assertEquals("transid", b1.transid, b2.transid);
        Assert.assertEquals("appid", b1.appid, b2.appid);
        Assert.assertEquals("appuser", b1.appuser, b2.appuser);
        Assert.assertEquals("platform", b1.platform, b2.platform);
        Assert.assertEquals("description", b1.description, b2.description);
        Assert.assertEquals("pmcid", b1.pmcid, b2.pmcid);
        Assert.assertEquals("reqdate", b1.reqdate, b2.reqdate);
        Assert.assertEquals("userchargeamt", b1.userchargeamt, b2.userchargeamt);
        Assert.assertEquals("amount", b1.amount, b2.amount);
        Assert.assertEquals("userfeeamt", b1.userfeeamt, b2.userfeeamt);
        Assert.assertEquals("type", b1.type, b2.type);
        Assert.assertEquals("sign", b1.sign, b2.sign);
        Assert.assertEquals("username", b1.username, b2.username);
        Assert.assertEquals("appusername", b1.appusername, b2.appusername);
    }

    private void assertEquals(List<TransHistoryEntity> list2, List<TransHistory> list1) {
        if (list1.size() == 0) {
            Assert.fail("object is null");
            return;
        }

        for (int i = 0; i < list1.size(); i++) {
            System.out.print(i);
            assertElementEquals(list1.get(i), list2.get(i));
        }
    }
}