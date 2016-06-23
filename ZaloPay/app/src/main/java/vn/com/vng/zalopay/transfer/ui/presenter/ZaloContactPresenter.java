package vn.com.vng.zalopay.transfer.ui.presenter;

import android.os.CountDownTimer;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
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
        zaloFriendsFactory.reloadZaloFriend(mView.getContext(), listener);
    }

    private void saveZaloFriends(List<ZaloFriend> zaloFriends) {
        zaloFriendsFactory.insertZaloFriends(zaloFriends);
    }
}
