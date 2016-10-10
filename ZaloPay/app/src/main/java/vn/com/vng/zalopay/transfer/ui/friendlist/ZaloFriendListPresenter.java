package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.content.Context;
import android.database.Cursor;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

final class ZaloFriendListPresenter extends BaseUserPresenter implements IPresenter<IZaloFriendListView> {

    private IZaloFriendListView mZaloFriendListView;

    private FriendStore.Repository mFriendRepository;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Context mContext;

    @Inject
    public ZaloFriendListPresenter(Context context, FriendStore.Repository friendRepository) {
        this.mFriendRepository = friendRepository;
        this.mContext = context;
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

    void getFriendList() {
        Subscription subscription = mFriendRepository.zaloFriendList()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mCompositeSubscription.add(subscription);
    }

    void doSearch(String s) {
        Subscription subscription = mFriendRepository.searchZaloFriend(s)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mCompositeSubscription.add(subscription);
    }

    private class FriendListSubscriber extends DefaultSubscriber<Cursor> {

        @Override
        public void onNext(Cursor cursor) {

            Timber.d("onNext cursor %s", cursor);
            mZaloFriendListView.swapCursor(cursor);
            mZaloFriendListView.hideLoading();
            mZaloFriendListView.setRefreshing(false);
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
