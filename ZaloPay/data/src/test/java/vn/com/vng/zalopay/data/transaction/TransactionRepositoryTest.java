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

    private final int TRANSACTION_SIZE = 20;

    private List<TransHistoryEntity> entities = new ArrayList<>();
    TransactionHistoryResponse transactionHistoryResponse;
    TransactionStore.LocalStorage mLocalStorage;
    TransactionStore.RequestService mRequestService = null;
    ZaloPayEntityDataMapper mMapper;
    User mUser;

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
        mUser = new User();
        mUser.accesstoken = "";

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new TransactionLocalStorage(daoSession);
        //mLocalStorage.put(entities);

        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage,
                mRequestService, EventBus.getDefault());
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
        //transactionHistoryResponse.data = entities;
    }

//    @Test
//    public void getTransactions() {
//        final List<TransHistory> result = new ArrayList<TransHistory>();
//        Subscription subscription;
//        int pageIndex, count;
//
//        pageIndex = 2;
//        count = 5;
//        subscription = mRepository.getTransactions(pageIndex, count).subscribe(
//                new Observer<List<TransHistory>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        System.out.print("getTransactions got error: " + e + "\n");
//                        return;
//                    }
//
//                    @Override
//                    public void onNext(List<TransHistory> transHistories) {
//                        result.addAll(transHistories);
//                    }
//                }
//        );
//        Assert.assertEquals("getTransactions: when not having data (both from local and cloud)", 0, result.size());
//
//        pageIndex = 0;
//        count = 0;
//        subscription = mRepository.getTransactions(pageIndex, count).subscribe(
//                new Observer<List<TransHistory>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        System.out.print("getTransactions: with count = 0 got error: " + e + "\n");
//                        return;
//                    }
//
//                    @Override
//                    public void onNext(List<TransHistory> transHistories) {
//                        result.clear();
//                        result.addAll(transHistories);
//                    }
//                }
//        );
//        Assert.assertEquals("getTransactions: with count = 0", 0, result.size());
//
//        pageIndex = 0;
//        count = -1;
//        subscription = mRepository.getTransactions(pageIndex, count).subscribe(
//                new Observer<List<TransHistory>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        System.out.print("getTransactions: with count = -1 got error: " + e + "\n");
//                        return;
//                    }
//
//                    @Override
//                    public void onNext(List<TransHistory> transHistories) {
//                        result.clear();
//                        result.addAll(transHistories);
//                    }
//                }
//        );
//        Assert.assertEquals("getTransactions: with count = -1", 0, result.size());
//
//        pageIndex = -1;
//        count = 10;
//        subscription = mRepository.getTransactions(pageIndex, count).subscribe(
//                new Observer<List<TransHistory>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        System.out.print("getTransactions: with pageIndex = -1 got error: " + e + "\n");
//                        return;
//                    }
//
//                    @Override
//                    public void onNext(List<TransHistory> transHistories) {
//                        result.clear();
//                        result.addAll(transHistories);
//                    }
//                }
//        );
//        Assert.assertEquals("getTransactions: with pageIndex = -1", 0, result.size());
//    }


    @Test
    public void getTransactionsFail() {
        final List<TransHistory> result = new ArrayList<TransHistory>();
        Subscription subscription;
        int pageIndex, count;

        pageIndex = 2;
        count = 5;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: not having datas got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: when not having data (both from local and cloud)", 0, result.size());

        //// Get datas from cloud
        transactionHistoryResponse.data = entities;
        mRequestService = new RequestServiceImpl();
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage,
                mRequestService, EventBus.getDefault());
        pageIndex = 2;
        count = 5;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: got datas from cloud got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.addAll(transHistories);
                    }
                }
        );
        assertEquals(result, transactionHistoryResponse.data.subList(
                TRANSACTION_SIZE - (pageIndex + 1) * count, TRANSACTION_SIZE - (pageIndex + 1) * count + count));

        pageIndex = 0;
        count = 0;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: got datas from cloud with count = 0 got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.clear();
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: test get datas from clound with count = 0", 0, result.size());

        pageIndex = 0;
        count = -1;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: test get datas from clound with count = -1 got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.clear();
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: test get datas from clound with count = -1", 0, result.size());

        pageIndex = -1;
        count = 10;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: test get datas from clound with pageIndex = -1 got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.clear();
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: test get datas from clound with pageIndex = -1", 0, result.size());

        //// Get datas from local storage
        mLocalStorage.put(entities);
        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage,
                null, EventBus.getDefault());
        pageIndex = 2;
        count = 5;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: got datas from local got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.addAll(transHistories);
                    }
                }
        );
        assertEquals(result, entities.subList(
                TRANSACTION_SIZE - (pageIndex + 1) * count, TRANSACTION_SIZE - (pageIndex + 1) * count + count));

        pageIndex = 0;
        count = 0;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: got datas from local with count = 0 got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.clear();
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: test get datas from local with count = 0", 0, result.size());

        pageIndex = 0;
        count = -1;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: test get datas from local with count = -1 got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.clear();
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: test get datas from local with count = -1", 0, result.size());

        pageIndex = -1;
        count = 10;
        subscription = mRepository.getTransactionsFail(pageIndex, count).subscribe(
                new Observer<List<TransHistory>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.print("getTransactionsFail: test get datas from local with pageIndex = -1 got error: " + e + "\n");
                        return;
                    }

                    @Override
                    public void onNext(List<TransHistory> transHistories) {
                        result.clear();
                        result.addAll(transHistories);
                    }
                }
        );
        Assert.assertEquals("getTransactionsFail: test get datas from local with pageIndex = -1", 0, result.size());

        //// Get datas from local and cloud
        //// ...
    }

//    @Test
//    public void getTransaction() {
//        final List<TransHistory> result = new ArrayList<TransHistory>();
//        long id = 1;
//
//        Subscription subscription = mRepository.getTransaction(id).subscribe(new Observer<TransHistory>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(TransHistory transHistory) {
//                result.add(transHistory);
//            }
//        });
//        Assert.assertEquals("getTransactionByID: when not having data", null, result.get(0));
//
//        mLocalStorage.put(entities);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage,
//                null, EventBus.getDefault());
//        subscription = mRepository.getTransaction(id).subscribe(new Observer<TransHistory>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(TransHistory transHistory) {
//                result.add(transHistory);
//            }
//        });
//        assertElementEquals(result.get(0), transactionHistoryResponse.data.get((int)id - 1));
//
//        transactionHistoryResponse.data = entities;
//        mRequestService = new RequestServiceImpl();
//        mRepository = new TransactionRepository(mMapper, mUser, null,
//                mRequestService, EventBus.getDefault());
//        subscription = mRepository.getTransaction(id).subscribe(new Observer<TransHistory>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(TransHistory transHistory) {
//                result.add(transHistory);
//            }
//        });
//        assertElementEquals(result.get(0), transactionHistoryResponse.data.get((int)id - 1));
//
//        id = -1;
//        result.clear();
//        subscription = mRepository.getTransaction(id).subscribe(new Observer<TransHistory>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(TransHistory transHistory) {
//                result.clear();
//                result.add(transHistory);
//            }
//        });
//        Assert.assertEquals("getTransactionByID: id = -1", null, result.get(0));
//
//        id = 21;
//        result.clear();
//        subscription = mRepository.getTransaction(id).subscribe(new Observer<TransHistory>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(TransHistory transHistory) {
//                result.clear();
//                result.add(transHistory);
//            }
//        });
//        Assert.assertEquals("getTransactionByID: id is over list size data", null, result.get(0));
//    }
//
//    @Test
//    public void updateTransactionStatusSuccess() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//        long id = 1;
//
//        Subscription subscription = mRepository.updateTransactionStatusSuccess(id).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("updateTransactionStatusSuccess", true, result.get(0));
//
//        id = 21;
//        subscription = mRepository.updateTransactionStatusSuccess(id).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("updateTransactionStatusSuccess: id is over list size data", false, result.get(0));
//
//        id = -1;
//        subscription = mRepository.updateTransactionStatusSuccess(id).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("updateTransactionStatusSuccess: id = -1", false, result.get(0));
//    }
//
//    @Test
//    public void isLoadedTransactionSuccess() {
//        boolean result;
//
//        result = mRepository.isLoadedTransactionSuccess();
//        Assert.assertEquals("isLoadedTransactionSuccess: test when haven't yet set loaded status", false, result);
//
//        mLocalStorage.setLoadedTransactionSuccess(true);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionSuccess();
//        Assert.assertEquals("isLoadedTransactionSuccess", true, result);
//
//        mLocalStorage.setLoadedTransactionSuccess(false);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionSuccess();
//        Assert.assertEquals("isLoadedTransactionSuccess: set loaded status true = false", false, result);
//
//        mLocalStorage.setLoadedTransactionFail(true);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionSuccess();
//        Assert.assertEquals("isLoadedTransactionSuccess: set loaded status fail = true", false, result);
//
//        mLocalStorage.setLoadedTransactionFail(false);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionSuccess();
//        Assert.assertEquals("isLoadedTransactionSuccess: set loaded status fail = false", false, result);
//    }
//
//    @Test
//    public void isLoadedTransactionFail() {
//        boolean result;
//
//        result = mRepository.isLoadedTransactionFail();
//        Assert.assertEquals("isLoadedTransactionFail: test when haven't yet set loaded status", false, result);
//
//        mLocalStorage.setLoadedTransactionFail(true);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionFail();
//        Assert.assertEquals("isLoadedTransactionFail", true, result);
//
//        mLocalStorage.setLoadedTransactionFail(false);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionFail();
//        Assert.assertEquals("isLoadedTransactionFail: set loaded status fail = false", false, result);
//
//        mLocalStorage.setLoadedTransactionSuccess(true);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionFail();
//        Assert.assertEquals("isLoadedTransactionFail: set loaded status true = true", false, result);
//
//        mLocalStorage.setLoadedTransactionSuccess(false);
//        mRepository = new TransactionRepository(mMapper, mUser, mLocalStorage, mRequestService, EventBus.getDefault());
//        result = mRepository.isLoadedTransactionFail();
//        Assert.assertEquals("isLoadedTransactionFail: set loaded status true = false", false, result);
//    }
//
//    @Test
//    public void fetchTransactionHistorySuccessLatest() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//
//        Subscription subscription = mRepository.fetchTransactionHistorySuccessLatest().subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("fetchTransactionHistorySuccessLatest", true, result.get(0));
//    }
//
//    @Test
//    public void fetchTransactionHistoryFailLatest() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//
//        Subscription subscription = mRepository.fetchTransactionHistoryFailLatest().subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("fetchTransactionHistoryFailLatest" ,true, result.get(0));
//    }
//
//    @Test
//    public void fetchTransactionHistoryLatest() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//
//        Subscription subscription = mRepository.fetchTransactionHistoryLatest().subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("fetchTransactionHistoryLatest" ,true, result.get(0));
//    }
//
//    @Test
//    public void fetchTransactionHistoryOldestWithThresholdTime() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//        long time = transactionHistoryResponse.data.get(3).reqdate;
//
//        Subscription subscription = mRepository.fetchTransactionHistoryOldest(time).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("fetchTransactionHistoryOldestWithThresholdTime" , true, result.get(0));
//    }
//
//    @Test
//    public void fetchTransactionHistoryLatestWithThresholdTime() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//        long time = transactionHistoryResponse.data.get(3).reqdate;
//
//        Subscription subscription = mRepository.fetchTransactionHistoryLatest(time).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("fetchTransactionHistoryLatestWithThresholdTime", true, result.get(0));
//    }
//
//    @Test
//    public void reloadTransactionHistoryWithTime() {
//        final List<Boolean> result = new ArrayList<Boolean>();
//        long time = transactionHistoryResponse.data.get(3).reqdate;
//
//        Subscription subscription = mRepository.reloadTransactionHistory(time).subscribe(new Observer<Boolean>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.print("Got error: " + e + "\n");
//                return;
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                result.add(aBoolean);
//            }
//        });
//        Assert.assertEquals("reloadTransactionHistoryWithTime", true, result.get(0));
//    }


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

    private void assertEquals(List<TransHistory> list1, List<TransHistoryEntity> list2) {
        if(list1.size() == 0) {
            Assert.fail("object is null");
            return;
        }

        for (int i = 0; i < list1.size(); i++) {
            assertElementEquals(list1.get(i), list2.get(list2.size() - i - 1));
        }
    }
}