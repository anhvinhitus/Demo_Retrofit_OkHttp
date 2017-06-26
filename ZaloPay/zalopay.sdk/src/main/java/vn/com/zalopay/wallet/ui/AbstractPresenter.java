package vn.com.zalopay.wallet.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import vn.com.zalopay.wallet.exception.InvalidStateException;

/**
 * Created by chucvv on 6/12/17.
 */

public abstract class AbstractPresenter<T> implements IPresenter<T> {
    protected T mView;
    protected CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    public void onAttach(T pView) {
        mView = pView;
    }

    @CallSuper
    @Override
    public void onDetach() {
        mView = null;
        mSubscription.clear();
    }

    @Nullable
    public T getView() {
        return mView;
    }

    @NonNull
    public T getViewOrThrow() throws Exception {
        final T view = getView();
        if (view == null) {
            throw new InvalidStateException("view not attached");
        }
        return view;
    }

    public void addSubscription(Subscription subscription) {
        mSubscription.add(subscription);
    }
}
