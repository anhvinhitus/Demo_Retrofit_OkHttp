package vn.com.vng.zalopay.data;

import org.greenrobot.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Observer;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;
import vn.com.vng.zalopay.data.balance.BalanceRepositoryImpl;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by huuhoa on 7/13/16.
 * Unit Tests for BalanceStore
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class BalanceRepositoryTest {
    BalanceStore.Repository mRepository;
    BalanceStore.RequestService mRequestService;
    BalanceStore.LocalStorage mLocalStorage;

    long returnBalance = 10000;

    class RequestService implements BalanceStore.RequestService {

        private final long mBalance;

        public RequestService(long balance) {

            mBalance = balance;
        }

        @Override
        public Observable<BalanceResponse> balance(String uid, String accesstoken) {
            BalanceResponse response = new BalanceResponse();
            response.userid = Long.parseLong(uid);
            response.zpwbalance = mBalance;
            return Observable.just(response);
        }
    }

    class LocalStorage implements BalanceStore.LocalStorage {
        long mValue;

        @Override
        public void putBalance(long value) {
            mValue = value;
        }

        @Override
        public Observable<Long> getBalance() {
            return Observable.just(mValue);
        }
    }

    ;

    @Before
    public void setUp() throws Exception {
        mRequestService = new RequestService(returnBalance);
        mLocalStorage = new LocalStorage();
    }

    @Test
    public void testGetBalance() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        final List<Long> balanceValue = new ArrayList<>();
        final Long[] balanceValueExpected = {10L, returnBalance};

        mLocalStorage.putBalance(10);
        mRepository = new BalanceRepositoryImpl(mLocalStorage, mRequestService, new User("1"), new EventBus());
        mRepository.balance().subscribe(new Observer<Long>() {
            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("Got value: " + String.valueOf(aLong));
                balanceValue.add(aLong);
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        Assert.assertArrayEquals(balanceValue.toArray(), balanceValueExpected);
    }

    @Test
    public void testUpdateBalance() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        final List<Long> balanceValue = new ArrayList<>();
        final List<Long> balancePutValue = new ArrayList<>();
        final Long[] balanceValueExpected = {returnBalance};

        mLocalStorage = new BalanceStore.LocalStorage() {
            long mValue;

            @Override
            public void putBalance(long value) {
                mValue = value;
                balancePutValue.add(value);
                countDownLatch.countDown();
            }

            @Override
            public Observable<Long> getBalance() {
                return Observable.just(mValue);
            }
        };


        mRepository = new BalanceRepositoryImpl(mLocalStorage, mRequestService, new User("1"), new EventBus());
        mRepository.updateBalance().subscribe(new Observer<Long>() {
            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("Got value: " + String.valueOf(aLong));
                balanceValue.add(aLong);
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        Assert.assertArrayEquals(balanceValue.toArray(), balanceValueExpected);
        Assert.assertArrayEquals(balancePutValue.toArray(), balanceValueExpected);
    }

    @Test
    public void testCurrentBalance() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        mRepository = new BalanceRepositoryImpl(mLocalStorage, mRequestService, new User("1"), new EventBus());
        Assert.assertEquals(Long.valueOf(0), mRepository.currentBalance());
        mRepository.balance().subscribe(new Observer<Long>() {
            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Long aLong) {
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        Assert.assertEquals(Long.valueOf(returnBalance), mRepository.currentBalance());
    }
}
