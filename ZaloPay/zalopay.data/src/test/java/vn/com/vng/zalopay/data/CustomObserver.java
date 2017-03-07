package vn.com.vng.zalopay.data;

import org.junit.Assert;

import java.util.List;

import rx.Observer;

/**
 * Created by cpu11759-local on 22/12/2016.
 */

public class CustomObserver<T> implements Observer<T> {
    private List<T> mResultHolder;
    public CustomObserver(List<T> resultHolder) {
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
    public void onNext(T items) {
        mResultHolder.add(items);
    }
}
