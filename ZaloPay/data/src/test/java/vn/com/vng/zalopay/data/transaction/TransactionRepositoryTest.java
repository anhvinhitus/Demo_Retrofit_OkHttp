package vn.com.vng.zalopay.data.transaction;

import android.database.sqlite.SQLiteDatabase;

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
import rx.Observer;
import rx.Subscription;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
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

    private Gson mGson;

    private static final int TRANSACTION_STATUS_FAIL = 2;

    private final int TRANSACTION_SIZE = 20; //row count jsonObject

    private List<TransHistoryEntity> entities = new ArrayList<>();
    TransactionHistoryResponse transactionHistoryResponse;

    public class RequestServiceImpl implements TransactionStore.RequestService {
        @Override
        public Observable<TransactionHistoryResponse> getTransactionHistories(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("statustype") int statustype) {
            return Observable.just(transactionHistoryResponse);
        }
    }

    @Before
    public void setUp() throws Exception {
        initData();

        TransactionStore.RequestService mRequestService = new RequestServiceImpl();
        ZaloPayEntityDataMapper mapper = new ZaloPayEntityDataMapper();
        User mUser = new User();
        mUser.accesstoken = "";

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        TransactionStore.LocalStorage mLocalStorage = new TransactionLocalStorage(daoSession);
        mLocalStorage.put(entities);
        mLocalStorage.setLoadedTransactionFail(true);

        mRepository = new TransactionRepository(mapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
    }

    private void initData() {
        mGson = new Gson();

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            TransHistoryEntity entity = mGson.fromJson(JSON_TRANSACTION, TransHistoryEntity.class);
            int j = i + 1;
            entity.transid = j;
            entity.appid = j;
            entity.userid = "user" + j;
            entity.reqdate += j;
            entity.statustype = TRANSACTION_STATUS_FAIL;
            entities.add(entity);
        }

        transactionHistoryResponse = new TransactionHistoryResponse();
        transactionHistoryResponse.data = new ArrayList<>();
        transactionHistoryResponse.data = entities;
    }

    @Test
    public void getTransactions() {
        final int[] size = {-1};

        Subscription subscription = mRepository.getTransactions(0, TRANSACTION_SIZE).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print(e);
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        size[0] = transHistories.size();
                    }
                }
        );

        Assert.assertTrue(size[0] != 0);
    }


    @Test
    public void getTransactionsFail() {
        final boolean[] result = new boolean[1];

        Subscription subscription = mRepository.getTransactionsFail(0, TRANSACTION_SIZE).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print(e);
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result[0] = assertEquals(transHistories, transactionHistoryResponse.data);
                    }
                }
        );

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void getTransaction() {
        final boolean[] result = new boolean[1];
        long id = 1;

        Subscription subscription = mRepository.getTransaction(id).subscribe(new Observer<TransHistory>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(TransHistory transHistory) {
                result[0] = assertElementEquals(transHistory, transactionHistoryResponse.data.get((int)id - 1));
            }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void updateTransactionStatusSuccess() {
        final boolean[] result = new boolean[1];
        long id = 1;

        Subscription subscription = mRepository.updateTransactionStatusSuccess(id).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
            }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void isLoadedTransaction() {
        boolean result;

        result = mRepository.isLoadedTransactionSuccess();
        Assert.assertEquals(false, result);

        result = mRepository.isLoadedTransactionFail();
        Assert.assertEquals(true, result);
    }

    @Test
    public void fetchTransactionHistorySuccessLatest() {
        final boolean[] result = new boolean[1];

        Subscription subscription = mRepository.fetchTransactionHistorySuccessLatest().subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
            }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void fetchTransactionHistoryFailLatest() {
        final boolean[] result = new boolean[1];

        Subscription subscription = mRepository.fetchTransactionHistoryFailLatest().subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
            }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void fetchTransactionHistoryLatest() {
        final boolean[] result = new boolean[1];

        Subscription subscription = mRepository.fetchTransactionHistoryLatest().subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
            }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void fetchTransactionHistoryOldestWithThresholdTime() {
        final boolean[] result = new boolean[1];
        long time = transactionHistoryResponse.data.get(3).reqdate;

        Subscription subscription = mRepository.fetchTransactionHistoryOldest(time).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
           }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void fetchTransactionHistoryLatestWithThresholdTime() {
        final boolean[] result = new boolean[1];
        long time = transactionHistoryResponse.data.get(3).reqdate;

        Subscription subscription = mRepository.fetchTransactionHistoryLatest(time).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
            }
        });

        Assert.assertEquals(true, result[0]);
    }

    @Test
    public void reloadTransactionHistoryWithTime() {
        final boolean[] result = new boolean[1];
        long time = transactionHistoryResponse.data.get(3).reqdate;

        Subscription subscription = mRepository.reloadTransactionHistory(time).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.print(e);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                result[0] = aBoolean;
            }
        });

        Assert.assertEquals(true, result[0]);
    }


    private boolean assertElementEquals(TransHistory b1, TransHistoryEntity b2) {
        if (b1 == null && b2 != null) { return false; }
        if (b1 != null && b2 == null) { return false; }

        if(b1.userid != b2.userid) { return false; }
        if(b1.transid != b2.transid) { return false; }
        if(b1.appid != b2.appid) { return false; }
        if(b1.appuser != b2.appuser) { return false; }
        if(b1.platform != b2.platform) { return false; }
        if(b1.description != b2.description) { return false; }
        if(b1.pmcid != b2.pmcid) { return false; }
        if(b1.reqdate != b2.reqdate) { return false; }
        if(b1.userchargeamt != b2.userchargeamt) { return false; }
        if(b1.amount != b2.amount) { return false; }
        if(b1.userfeeamt != b2.userfeeamt) { return false; }
        if(b1.type != b2.type) { return false; }
        if(b1.sign != b2.sign) { return false; }
        if(b1.username != b2.username) { return false; }
        if(b1.appusername != b2.appusername) { return false; }

        return true;
    }

    private boolean assertEquals(List<TransHistory> list1, List<TransHistoryEntity> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = TRANSACTION_SIZE - 1; i >= 0; i--) {
            if(!assertElementEquals(list1.get(i), list2.get(TRANSACTION_SIZE - i - 1))) {
                return false;
            }
        }

        return true;
    }
}