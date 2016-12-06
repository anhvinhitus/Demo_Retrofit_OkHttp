package vn.com.vng.zalopay.data.merchant;

import android.util.LongSparseArray;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.CustomRobolectricRunner;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.api.response.ListMUIResponse;
import vn.com.vng.zalopay.data.cache.model.MerchantUser;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by huuhoa on 9/23/16.
 * Unit tests for Merchant repository test
 */
@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16, manifest=Config.NONE)
public class MerchantRepositoryTest {
    @Test
    public void getMerchantUserInfoNotInLocalStorage() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        MerchantStore.LocalStorage localStorage = new MerchantStore.LocalStorage() {
            LongSparseArray<MerchantUser> db = new LongSparseArray<>();
            @Override
            public void put(MerchantUser entity) {
                db.put(entity.appid, entity);
            }

            @Override
            public void put(List<MerchantUser> entities) {
                for (MerchantUser e : entities) {
                    db.put(e.appid, e);
                }
            }

            @Override
            public MerchantUser get(long appId) {
                countDownLatch.countDown();
                return db.get(appId, null);
            }

            @Override
            public void removeAll() {

            }

            @Override
            public boolean existIn(Collection<Long> appIds) {
                return false;
            }

            @Override
            public List<Long> notExistInDb(List<Long> appIds) {
                return null;
            }
        };

        MerchantStore.RequestService requestService = new MerchantStore.RequestService() {
            @Override
            public Observable<GetMerchantUserInfoResponse> getmerchantuserinfo(@Query("appid") long appid, @Query("userid") String userid, @Query("accesstoken") String accesstoken) {
                countDownLatch.countDown();
                GetMerchantUserInfoResponse response = new GetMerchantUserInfoResponse();
                response.muid = "1" + String.valueOf(appid);
                return Observable.just(response);
            }

            @Override
            public Observable<ListMUIResponse> getlistmerchantuserinfo(@Query("appidlist") String appidlist, @Query("userid") String userid, @Query("accesstoken") String accesstoken) {
                String[] appids = appidlist.split(",");
                ListMUIResponse response = new ListMUIResponse();
                response.listmerchantuserinfo = new ArrayList<>();
                for (String a : appids) {
                    ListMUIResponse.MerchantUserSubInfo u = new ListMUIResponse().new MerchantUserSubInfo();
                    u.muid = "1" + String.valueOf(a);
                    response.listmerchantuserinfo.add(u);
                }
                return Observable.just(response);
            }
        };

        User user = new User();
        user.zaloPayId = "1";

        MerchantRepository repository = new MerchantRepository(localStorage, requestService, user);
        repository.getMerchantUserInfo(10).subscribe(new Subscriber<MerchantUserInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(MerchantUserInfo merchantUserInfo) {
                Assert.assertEquals("110", merchantUserInfo.muid);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));

    }

    @Test
    public void getMerchantUserInfoInLocalStorage() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        MerchantStore.LocalStorage localStorage = new MerchantStore.LocalStorage() {
            LongSparseArray<MerchantUser> db = new LongSparseArray<>();
            @Override
            public void put(MerchantUser entity) {
                db.put(entity.appid, entity);
            }

            @Override
            public void put(List<MerchantUser> entities) {
                for (MerchantUser e : entities) {
                    db.put(e.appid, e);
                }
            }

            @Override
            public MerchantUser get(long appId) {
                System.out.println("Query for app in local: " + String.valueOf(appId));
                countDownLatch.countDown();
                MerchantUser user =  db.get(appId, null);
                System.out.println("Query for app in local result: " + user);

                return user;
            }

            @Override
            public void removeAll() {

            }

            @Override
            public boolean existIn(Collection<Long> appIds) {
                return false;
            }

            @Override
            public List<Long> notExistInDb(List<Long> appIds) {
                return null;
            }
        };

        MerchantStore.RequestService requestService = new MerchantStore.RequestService() {
            @Override
            public Observable<GetMerchantUserInfoResponse> getmerchantuserinfo(@Query("appid") long appid, @Query("userid") String userid, @Query("accesstoken") String accesstoken) {
                Assert.fail("Should not call here");
                return Observable.empty();
            }

            @Override
            public Observable<ListMUIResponse> getlistmerchantuserinfo(@Query("appidlist") String appidlist, @Query("userid") String userid, @Query("accesstoken") String accesstoken) {
                String[] appids = appidlist.split(",");
                ListMUIResponse response = new ListMUIResponse();
                response.listmerchantuserinfo = new ArrayList<>();
                for (String a : appids) {
                    ListMUIResponse.MerchantUserSubInfo u = new ListMUIResponse().new MerchantUserSubInfo();
                    u.muid = "1" + String.valueOf(a);
                    response.listmerchantuserinfo.add(u);
                }
                return Observable.just(response);
            }
        };

        User user = new User();
        user.zaloPayId = "1";
        MerchantUser merchantUser = new MerchantUser();
        merchantUser.appid = (10);
        merchantUser.mUid = ("110");
        merchantUser.mAccessToken = ("accesstoken");
        merchantUser.avatar = ("avatar");
        merchantUser.displayName = ("display");
        merchantUser.gender = (1);

        localStorage.put(merchantUser);

        MerchantRepository repository = new MerchantRepository(localStorage, requestService, user);
        repository.getMerchantUserInfo(10).subscribe(new Subscriber<MerchantUserInfo>() {
            @Override
            public void onCompleted() {
                System.out.println("Completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Error: " + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(MerchantUserInfo merchantUserInfo) {
                System.out.println("Got result");
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));
    }


    @Test
    public void getMerchantUserInfoInLocalStorage2() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(7);

        MerchantStore.LocalStorage localStorage = new MerchantStore.LocalStorage() {
            LongSparseArray<MerchantUser> db = new LongSparseArray<>();
            @Override
            public void put(MerchantUser entity) {
                countDownLatch.countDown();
                System.out.println("Put app in local: " + String.valueOf(entity.appid));
                db.put(entity.appid, entity);
            }

            @Override
            public void put(List<MerchantUser> entities) {
                for (MerchantUser e : entities) {
                    db.put(e.appid, e);
                }
            }

            @Override
            public MerchantUser get(long appId) {
                System.out.println("Query for app in local: " + String.valueOf(appId));
                countDownLatch.countDown();
                return db.get(appId, null);
            }

            @Override
            public void removeAll() {

            }

            @Override
            public boolean existIn(Collection<Long> appIds) {
                return false;
            }

            @Override
            public List<Long> notExistInDb(List<Long> appIds) {
                return null;
            }
        };

        MerchantStore.RequestService requestService = new MerchantStore.RequestService() {
            @Override
            public Observable<GetMerchantUserInfoResponse> getmerchantuserinfo(@Query("appid") long appid, @Query("userid") String userid, @Query("accesstoken") String accesstoken) {
                countDownLatch.countDown();

                GetMerchantUserInfoResponse response = new GetMerchantUserInfoResponse();
                response.muid = "1";
                response.usergender = 1;
                return Observable.just(response);
            }

            @Override
            public Observable<ListMUIResponse> getlistmerchantuserinfo(@Query("appidlist") String appidlist, @Query("userid") String userid, @Query("accesstoken") String accesstoken) {
                String[] appids = appidlist.split(",");
                ListMUIResponse response = new ListMUIResponse();
                response.listmerchantuserinfo = new ArrayList<>();
                for (String a : appids) {
                    ListMUIResponse.MerchantUserSubInfo u = new ListMUIResponse().new MerchantUserSubInfo();
                    u.muid = "1" + String.valueOf(a);
                    response.listmerchantuserinfo.add(u);
                }
                return Observable.just(response);
            }
        };

        User user = new User();
        user.zaloPayId = "1";

        MerchantRepository repository = new MerchantRepository(localStorage, requestService, user);
        Subscriber<MerchantUserInfo> subscriber = new Subscriber<MerchantUserInfo>() {
            @Override
            public void onCompleted() {
                System.out.println("Completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Error: " + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(MerchantUserInfo merchantUserInfo) {
                System.out.println("Got result");
                countDownLatch.countDown();
            }
        };

        System.out.println("Query for not in local");
        repository.getMerchantUserInfo(10).subscribe(subscriber);

        System.out.println("Query for in local");
        repository.getMerchantUserInfo(10).subscribe(subscriber);

        Assert.assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void getListMerchantUserInfo() throws Exception {

    }

}