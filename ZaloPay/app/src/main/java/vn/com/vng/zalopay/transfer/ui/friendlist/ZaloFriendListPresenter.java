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
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

final class ZaloFriendListPresenter extends BaseUserPresenter implements IPresenter<IZaloFriendListView> {

    private IZaloFriendListView mZaloFriendListView;

    private FriendStore.Repository mFriendRepository;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Context mContext;

    private Navigator mNavigator;


    @Inject
    ZaloFriendListPresenter(Context context, Navigator navigator, FriendStore.Repository friendRepository) {
        this.mFriendRepository = friendRepository;
        this.mContext = context;
        this.mNavigator = navigator;
    }

    @Override
    public void setView(IZaloFriendListView iZaloFriendListView) {
        mZaloFriendListView = iZaloFriendListView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(mCompositeSubscription);
        mZaloFriendListView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    void refreshFriendList() {
        Subscription subscription = mFriendRepository.fetchZaloFriendList()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mCompositeSubscription.add(subscription);
    }

    void getFriendList() {
        Subscription subscription = mFriendRepository.zaloFriendList()
                .concatWith(retrieveZaloFriendsAsNeeded())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mCompositeSubscription.add(subscription);
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

    public void syncContact() {
        Subscription subscription = mFriendRepository.syncContact()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        mCompositeSubscription.add(subscription);
    }

    void doSearch(String s) {
        Subscription subscription = mFriendRepository.searchZaloFriend(s)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mCompositeSubscription.add(subscription);
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

    void showDialogNotUsingApp(ZaloFriend zaloFriend) {
        if (mZaloFriendListView != null) {
            String message = String.format(mContext.getString(R.string.account_not_use_zalopay), zaloFriend.displayName, zaloFriend.displayName);
            DialogManager.showSweetDialogCustom((Activity) mZaloFriendListView.getContext(),
                    message, mContext.getString(R.string.txt_close), mContext.getString(R.string.notification),
                    DialogManager.NORMAL_TYPE, null
            );
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
            if (cursor != null) {
                mZaloFriendListView.swapCursor(cursor);
                mZaloFriendListView.hideLoading();
                mZaloFriendListView.setRefreshing(false);
            }
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get friend zalo error");
            String message = ErrorMessageFactory.create(mContext, e);
            if (mZaloFriendListView != null) {
                mZaloFriendListView.showError(message);
                mZaloFriendListView.setRefreshing(false);
            }
        }
    }
}
