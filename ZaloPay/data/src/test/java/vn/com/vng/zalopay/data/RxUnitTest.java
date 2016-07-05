package vn.com.vng.zalopay.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by huuhoa on 7/5/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class RxUnitTest {
    @Test
    public void testJust() {
        // Loads 3 documents in parallel
        Observable
                .just("doc1", "doc2", "doc3")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String id) {
                        return Observable.just(id + "_1");
                    }
                }).subscribe(new Action1<String>() {
            @Override
            public void call(String document) {
                System.out.println("Got: " + document);
            }
        });
    }

    @Test
    public void testFilterNext() {
        // Loads 3 documents in parallel
        Observable<String> first = Observable
                .just("doc1", "doc2", "doc3", "doc4")
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.equalsIgnoreCase("doc4");
                    }
                });

        Observable<String> next = Observable.just("doc5");

        first.concatWith(next).subscribe(new Action1<String>() {
            @Override
            public void call(String document) {
                System.out.println("Got: " + document);
            }
        });

        first.mergeWith(next).subscribe(new Action1<String>() {
            @Override
            public void call(String document) {
                System.out.println("Got: " + document);
            }
        });

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                first.subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("Got inside: " + s);
                        next.subscribe(subscriber);
                    }
                });
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String document) {
                System.out.println("Got: " + document);
            }
        });
    }

    @Test
    public void testTimeout() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);

        Observable timeout = Observable.timer(10, TimeUnit.MILLISECONDS);
        Observable.just("doc1", "doc2")
                .ambWith(timeout)
                .take(1)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("Completed");
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("Errr: " + e);
                        latch.countDown();
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("Got: " + s);
                        latch.countDown();
                    }
                });

        latch.await();
    }
}
