package vn.com.vng.zalopay.transfer.ui.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import de.greenrobot.dao.query.LazyList;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.ui.view.IZaloContactView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 11/06/2016.
 */
public class ZaloContactPresenter extends BaseUserPresenter implements IPresenter<IZaloContactView> {
    private final int TIMEOUT_GET_ZALO_FRIENDS = 10000; //10s
    private IZaloContactView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    enum EGetZaloFriendListener {
        GetZaloFriendError(0), GetZaloFriendFinish(1), TimeOut(2);
        private final int value;

        EGetZaloFriendListener(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    Navigator navigator;
    FriendStore.Repository mRepository;
    Subscription mSubscription;

    @Inject
    public ZaloContactPresenter(Navigator navigator, FriendStore.Repository repository) {
        this.navigator = navigator;
        this.mRepository = repository;
    }

    @Override
    public void setView(IZaloContactView zaloContactView) {
        mView = zaloContactView;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        unsubscribeIfNotNull(compositeSubscription);
    }

    public void retrieveZaloFriendsAsNeeded() {
        if (mRepository == null) {
            onGetZaloFriendError();
            return;
        }
        Observable<List<ZaloFriend>> timeout = Observable.timer(TIMEOUT_GET_ZALO_FRIENDS, TimeUnit.MILLISECONDS).map(new Func1<Long, List<ZaloFriend>>() {
            @Override
            public List<ZaloFriend> call(Long o) {
                return null;
            }
        });

        Observable.amb(timeout, mRepository.retrieveZaloFriendsAsNeeded())
                .subscribe(new GetFriendSubscriber());
    }

    public void getFriendListServer() {
        Timber.d("getFriendListServer mRepository: %s", mRepository);
        if (mRepository == null) {
            onGetZaloFriendError();
            return;
        }

        Observable<List<ZaloFriend>> timeout = Observable.timer(TIMEOUT_GET_ZALO_FRIENDS, TimeUnit.MILLISECONDS).map(new Func1<Long, List<ZaloFriend>>() {
            @Override
            public List<ZaloFriend> call(Long o) {
                return null;
            }
        });

        Observable.amb(timeout, mRepository.fetchListFromServer())
                .subscribe(new GetFriendSubscriber());
    }

    public void getFriedListFromDB(String textSearch) {
        Timber.d("getFriedListFromDB  textSearch:%s", textSearch);
        Subscription subscription = mRepository.listZaloFriendFromDb(textSearch)
                .subscribe(new FriendLazyListSubscriber());
        compositeSubscription.add(subscription);
    }

    private void onGetZaloFriendTimeout() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.TimeOut.getValue();
        messageHandler.sendMessage(message);
    }

    public void onGetZaloFriendError() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.GetZaloFriendError.getValue();
        messageHandler.sendMessage(message);
    }

    public void onGetZaloFriendFinish() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.GetZaloFriendFinish.getValue();
        messageHandler.sendMessage(message);
    }

    private final Handler messageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            if (msg.what == EGetZaloFriendListener.GetZaloFriendError.getValue()) {
                ZaloContactPresenter.this.mView.onGetZaloFriendError();
            } else if (msg.what == EGetZaloFriendListener.GetZaloFriendFinish.getValue()) {
                ZaloContactPresenter.this.mView.onGetZaloFriendFinish();
            } else if (msg.what == EGetZaloFriendListener.TimeOut.getValue()) {
                ZaloContactPresenter.this.mView.onGetZaloFriendTimeout();
            }
        }
    };

    private class GetFriendSubscriber extends Subscriber<List<ZaloFriend>> {
        @Override
        public void onCompleted() {
            unsubscribeIfNotNull(mSubscription);
            mSubscription = null;

            onGetZaloFriendFinish();
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof TimeoutException ||
                    e.getCause() instanceof TimeoutException) {
                onGetZaloFriendTimeout();
            } else {
                onGetZaloFriendError();
            }
        }

        @Override
        public void onNext(List<ZaloFriend> zaloFriends) {
        }
    }

    private class FriendLazyListSubscriber extends DefaultSubscriber<LazyList<ZaloFriendGD>> {
        @Override
        public void onCompleted() {
            Timber.d("onCompleted ");
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("onError e %s", e);
            super.onError(e);
        }

        @Override
        public void onNext(LazyList<ZaloFriendGD> zaloFriendGDs) {
            Timber.d("onNext zaloFriendGDS %s", zaloFriendGDs);
            mView.onGetZaloFriendFinish(zaloFriendGDs);
        }
    }
}
