package vn.com.vng.zalopay.transfer.ui.presenter;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.FriendRepository;
import vn.com.vng.zalopay.transfer.FriendStoreRepository;
import vn.com.vng.zalopay.transfer.ZaloFriendsFactory;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.view.IZaloContactView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 11/06/2016.
 */
public class ZaloContactPresenter extends BaseUserPresenter implements IPresenter<IZaloContactView>, FriendRepository.IZaloFriendListener {
    private final int TIMEOUT_GET_ZALO_FRIENDS = 10000; //10s
    private final int TIMEOUT_UPDATE_LISTVIEW = 1000; //1s
    private IZaloContactView mView;

    CountDownTimer mCountDownGetZaloFriends;
    CountDownTimer mCountDownUpdateListView;

    enum EGetZaloFriendListener {
        GetZaloFriendError(0), GetZaloFriendFinish(1), ZaloFriendUpdated(2), TimeOut(3);
        private final int value;

        EGetZaloFriendListener(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Inject
    Navigator navigator;

    FriendStoreRepository zaloFriendsFactory;

    public ZaloContactPresenter(FriendStoreRepository zaloFriendsFactory) {
        this.zaloFriendsFactory = zaloFriendsFactory;
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
        /*zaloFriendsFactory = null;*/
    }

    private void startCountDownGetZaloFriends() {
        if (mCountDownGetZaloFriends == null) {
            mCountDownGetZaloFriends = new CountDownTimer(TIMEOUT_GET_ZALO_FRIENDS, TIMEOUT_GET_ZALO_FRIENDS) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    onGetZaloFriendTimeout();
                }
            };
        } else {
            mCountDownGetZaloFriends.cancel();
        }
        mCountDownGetZaloFriends.start();
    }

    private void mCountDownUpdateListView() {
        if (mCountDownUpdateListView == null) {
            mCountDownUpdateListView = new CountDownTimer(TIMEOUT_UPDATE_LISTVIEW, TIMEOUT_UPDATE_LISTVIEW) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    onGetZaloFriendFinish();
                }
            };
        } else {
            mCountDownUpdateListView.cancel();
        }
        mCountDownUpdateListView.start();
    }

    public void retrieveZaloFriendsAsNeeded() {
//        mView.showLoading();
        if (zaloFriendsFactory == null) {
            onGetZaloFriendError();
            return;
        }
        zaloFriendsFactory.retrieveZaloFriendsAsNeeded(this);
        startCountDownGetZaloFriends();
    }


    public void getFriendListServer() {
//        mView.showLoading();
        Timber.d("getFriendListServer zaloFriendsFactory: %s", zaloFriendsFactory);
        if (zaloFriendsFactory == null) {
            onGetZaloFriendError();
            return;
        }
        zaloFriendsFactory.fetchListFromServer(this);
        startCountDownGetZaloFriends();
    }

    private void onGetZaloFriendTimeout() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.TimeOut.getValue();
        messageHandler.sendMessage(message);
    }

    @Override
    public void onGetZaloFriendSuccess(List<ZaloFriend> zaloFriends) {
        mCountDownUpdateListView();
    }

    private void cancelTimeout() {
        if (mCountDownGetZaloFriends != null) {
            mCountDownGetZaloFriends.cancel();
        }
        if (mCountDownUpdateListView != null) {
            mCountDownUpdateListView.cancel();
        }
    }

    @Override
    public void onGetZaloFriendError() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.GetZaloFriendError.getValue();
        messageHandler.sendMessage(message);
        cancelTimeout();
    }

    @Override
    public void onZaloFriendUpdated() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.ZaloFriendUpdated.getValue();
        messageHandler.sendMessage(message);
        cancelTimeout();
    }

    @Override
    public void onGetZaloFriendFinish() {
        Message message = new Message();
        message.what = EGetZaloFriendListener.GetZaloFriendFinish.getValue();
        messageHandler.sendMessage(message);
        cancelTimeout();
    }

    private final Handler messageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            if (msg.what == EGetZaloFriendListener.GetZaloFriendError.getValue()) {
                ZaloContactPresenter.this.mView.onGetZaloFriendError();
            } else if (msg.what == EGetZaloFriendListener.ZaloFriendUpdated.getValue()) {
                ZaloContactPresenter.this.mView.onZaloFriendUpdated();
            } else if (msg.what == EGetZaloFriendListener.GetZaloFriendFinish.getValue()) {
                ZaloContactPresenter.this.mView.onGetZaloFriendFinish();
            } else if (msg.what == EGetZaloFriendListener.TimeOut.getValue()) {
                ZaloContactPresenter.this.mView.onGetZaloFriendTimeout();
            }
        }
    };
}
