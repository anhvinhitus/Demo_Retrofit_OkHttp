package vn.com.vng.zalopay.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by huuhoa on 7/5/16.
 */

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


    @Test
    public void testRecurseRx() {
        Observable<Long> observableJust = Observable.just(1L)
                .filter(aLong -> aLong > 0)
                .flatMap(aLong -> Observable.just(3L));
        observableJust.subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted Just");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("along just " + aLong);
            }
        });

        Observable<Long> observable = requestApi(0);
        //  final CountDownLatch latch = new CountDownLatch(1);

        Subscriber<Long> testSubscriber = new Subscriber<Long>() {
            @Override
            public void onError(Throwable e) {
                System.out.println("error: " + e);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("Recurse time " + aLong);
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted: Recurse");
            }
        };
        observable.subscribe(testSubscriber);
        //  testSubscriber.assertNoErrors();
        //Assert.assertTrue(false);
       /* try {
            latch.await();
        } catch (Exception ex) {
            //empty
        }*/
    }

    private Observable<Long> requestApi(long page) {
        return Observable.just(page)
                .filter(aLong -> {
                    System.out.println("along +: " + aLong);
                    return aLong < 30;
                })
                .flatMap(aLong -> requestApi(page + 1))
                ;
    }

    @Test
    public void testRecurseRx2() {
        int maxPageNum = 20;
        Subscription subscription = Observable.create(subscriber -> getIntegers(1, 5, maxPageNum, subscriber))
                .subscribe(System.out::println);
    }

    private void getIntegers(final int pageStart, int pageSize, int maxPageNumber, Subscriber<? super Integer> subscriber) {
        Subscription subscription = Observable.range(pageStart, pageSize)
                .doOnCompleted(() -> {
                    int newPageStart = pageStart + pageSize;
                    if (newPageStart >= maxPageNumber) {
                        subscriber.onCompleted();
                    } else {
                        getIntegers(newPageStart, pageSize, maxPageNumber, subscriber);
                    }
                })
                .subscribe(subscriber::onNext);
    }

}
