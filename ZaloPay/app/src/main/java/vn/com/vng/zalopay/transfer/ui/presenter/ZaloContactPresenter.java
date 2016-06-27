package vn.com.vng.zalopay.transfer.ui.presenter;

import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.ZaloFriendsFactory;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.view.IZaloContactView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 11/06/2016.
 */
public class ZaloContactPresenter extends BaseUserPresenter implements IPresenter<IZaloContactView> {
    private IZaloContactView mView;

    @Inject
    Navigator navigator;

    ZaloFriendsFactory zaloFriendsFactory;

    public interface IZaloFriendListener {
        void onGetZaloFriendSuccess(List<ZaloFriend> zaloFriends);
    }

    public ZaloContactPresenter(ZaloFriendsFactory zaloFriendsFactory) {
        this.zaloFriendsFactory = zaloFriendsFactory;
    }

    @Override
    public void setView(IZaloContactView zaloContactView) {
        mView = zaloContactView;
    }

    @Override
    public void destroyView() {
        zaloFriendsFactory = null;
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
    }

    public void getFriendList(final IZaloFriendListener listener) {
//        mView.showLoading();
        AndroidApplication.instance().getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (zaloFriendsFactory == null || mView == null) {
                    return;
                }
                zaloFriendsFactory.reloadZaloFriend(applicationContext, listener);
            }
        });
    }

    private void saveZaloFriends(List<ZaloFriend> zaloFriends) {
        zaloFriendsFactory.insertZaloFriends(zaloFriends);
    }
}
