package vn.com.zalopay.wallet.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

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
    public T getViewOrThrow() {
        final T view = getView();
        if (view == null) {
            throw new IllegalStateException("view not attached");
        }
        return view;
    }

    public void addSubscription(Subscription subscription) {
        mSubscription.add(subscription);
    }
}
