package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.DialogHelper;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

final class ZaloFriendListPresenter extends AbstractPresenter<IZaloFriendListView> {
    private FriendStore.Repository mFriendRepository;
    private Context mContext;
    private Navigator mNavigator;


    @Inject
    ZaloFriendListPresenter(Context context, Navigator navigator, FriendStore.Repository friendRepository) {
        this.mFriendRepository = friendRepository;
        this.mContext = context;
        this.mNavigator = navigator;
    }

    void refreshFriendList() {
        Subscription subscription = mFriendRepository.fetchZaloFriendList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    void getFriendList() {
        Subscription subscription = mFriendRepository.zaloFriendList()
                .concatWith(retrieveZaloFriendsAsNeeded())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    private Observable<Cursor> retrieveZaloFriendsAsNeeded() {
        return mFriendRepository.shouldUpdateFriendList()
                .flatMap(new Func1<Boolean, Observable<Cursor>>() {
                    @Override
                    public Observable<Cursor> call(Boolean aBoolean) {
                        if (aBoolean) {
                            return mFriendRepository.fetchZaloFriendList();
                        } else {
                            return Observable.empty();
                        }
                    }
                });
    }

    void syncContact() {
        Subscription subscription = mFriendRepository.syncContact()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        mSubscription.add(subscription);
    }

    void doSearch(String s) {
        Subscription subscription = mFriendRepository.searchZaloFriend(s)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    void startTransfer(Fragment fragment, Cursor cursor) {
        ZaloFriend zaloFriend = mFriendRepository.transform(cursor);
        if (zaloFriend == null) {
            return;
        }

        if (zaloFriend.usingApp) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.ARG_ZALO_FRIEND, zaloFriend);
            mNavigator.startTransferActivity(fragment, bundle);
        } else {
            showDialogNotUsingApp(zaloFriend);
        }
    }

    private void showDialogNotUsingApp(ZaloFriend zaloFriend) {
        if (mView != null) {
            String message = String.format(mContext.getString(R.string.account_not_use_zalopay), zaloFriend.displayName, zaloFriend.displayName);
            DialogHelper.showNotificationDialog((Activity) mView.getContext(),
                    message,
                    null);
        }
    }

    private class FriendListSubscriber extends DefaultSubscriber<Cursor> {

        int next;

        @Override
        public void onCompleted() {
            Timber.d("onCompleted");
        }

        @Override
        public void onNext(Cursor cursor) {
            Timber.d("onNext:  %s %s", next++, cursor);
            if (mView == null) {
                return;
            }

            if (cursor != null) {
                mView.swapCursor(cursor);
                mView.hideLoading();
                mView.setRefreshing(false);
                mView.checkIfEmpty();
            }
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get friend zalo error");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (mView != null) {
                mView.showError(ErrorMessageFactory.create(mContext, e));
                mView.setRefreshing(false);
            }
        }
    }
}
