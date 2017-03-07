package vn.com.vng.zalopay.data;

import org.junit.Assert;

import java.util.List;

import rx.Observer;

/**
 * Created by huuhoa on 12/16/16.
 * Default observer for getting list of result
 */

public class DefaultObserver<T> implements Observer<List<T>> {
    private List<T> mResultHolder;
    public DefaultObserver(List<T> resultHolder) {
        mResultHolder = resultHolder;
    }
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
    public void onNext(List<T> items) {
        mResultHolder.addAll(items);
    }
}
