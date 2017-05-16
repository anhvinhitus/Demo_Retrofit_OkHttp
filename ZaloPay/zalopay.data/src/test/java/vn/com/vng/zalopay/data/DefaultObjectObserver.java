package vn.com.vng.zalopay.data;

import org.junit.Assert;

import rx.Observer;

/**
 * Created by longlv on 05/15/17.
 * Default observer for getting object of result
 */

public abstract class DefaultObjectObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        System.out.println("DefaultObserve failed: not having data. Got error: " + e);
        e.printStackTrace();
        Assert.fail(e.getMessage());
    }

    @Override
    public void onNext(T item) {
    }
}
