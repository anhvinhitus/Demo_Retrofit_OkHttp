package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
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
import vn.com.vng.zalopay.data.zfriend.FriendConfig;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.model.TransferObject;
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
        Subscription subscription = mFriendRepository.fetchZaloFriendCursorFullInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    boolean isEnableSyncContact() {
        return FriendConfig.sEnableSyncContact;
    }

    void getFriendList() {
        Subscription subscription = mFriendRepository.getZaloFriendsCursor()
                .concatWith(retrieveZaloFriendsAsNeeded())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    private Observable<Cursor> retrieveZaloFriendsAsNeeded() {
        return mFriendRepository.shouldUpdateFriendList()
                .filter(Boolean::booleanValue)
                .flatMap(new Func1<Boolean, Observable<Cursor>>() {
                    @Override
                    public Observable<Cursor> call(Boolean aBoolean) {
                        return mFriendRepository.fetchZaloFriendCursorFullInfo();
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    void startTransfer(Fragment fragment, Cursor cursor) {
        ZPProfile zaloProfile = mFriendRepository.transform(cursor);
        if (zaloProfile == null) {
            return;
        }

        if (zaloProfile.status == 1) {

            TransferObject object = new TransferObject(zaloProfile);
            object.transferMode = Constants.TransferMode.TransferToZaloFriend;
            object.activateSource = Constants.ActivateSource.FromTransferActivity;

            mNavigator.startTransferActivity(fragment, object, Constants.REQUEST_CODE_TRANSFER);
        } else {
            showDialogNotUsingApp(zaloProfile);
        }
    }

    private void showDialogNotUsingApp(ZPProfile zaloProfile) {
        if (mView != null) {
            String message = String.format(mContext.getString(R.string.account_not_use_zalopay), zaloProfile.displayName, zaloProfile.displayName);
            DialogHelper.showNotificationDialog((Activity) mView.getContext(),
                    message,
                    null);
        }
    }

    private class FriendListSubscriber extends DefaultSubscriber<Cursor> {

        @Override
        public void onNext(Cursor cursor) {
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
                mView.hideLoading();
            }
        }
    }
}
