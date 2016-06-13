package vn.com.vng.zalopay.transfer.ui.presenter;

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
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.view.IZaloContactView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 11/06/2016.
 */
public class ZaloContactPresenter extends BaseUserPresenter implements IPresenter<IZaloContactView> {
    private final int OFFSET_GET_FRIEND = 50;
    private int mPageIndex = 0;
    private IZaloContactView mView;

    @Inject
    Navigator navigator;

    public interface IZaloFriendListener {
        void onGetZaloFriendSuccess(List<ZaloFriend> zaloFriends);
    }

    @Override
    public void setView(IZaloContactView zaloContactView) {
        mView = zaloContactView;
    }

    @Override
    public void destroyView() {

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
        mPageIndex = 0;
        getFriendList(mPageIndex, listener);
    }

    private void getFriendList(int pageIndex, final IZaloFriendListener listener) {
        Timber.d("getFriendList, pageIndex: %s", pageIndex);
        ZaloSDK.Instance.getFriendList(mView.getContext(), pageIndex, OFFSET_GET_FRIEND, new ZaloOpenAPICallback() {
            @Override
            public void onResult(final JSONObject arg0) {
                try {
                    JSONArray data = arg0.getJSONArray("result");
//                    Timber.d("getFriendList, data: %s", data.toString());
                    if (data != null && data.length() >= OFFSET_GET_FRIEND) {
                        mPageIndex++;
                        getFriendList(mPageIndex, listener);
                    }
                    List<ZaloFriend> zaloFriends = zaloFriends(data);
                    if (listener != null) {
                        listener.onGetZaloFriendSuccess(zaloFriends);
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private List<ZaloFriend> zaloFriends(final JSONArray jsonArray) {
        Timber.d("zaloFriends start........");
        List<ZaloFriend> zaloFriends = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() <= 0) {
            return zaloFriends;
        }
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Timber.d("zaloFriends index: %s", i);
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ZaloFriend zaloFriend = new ZaloFriend(jsonObject);
                if (zaloFriend.getUserId() > 0 && zaloFriend.isUsingApp()) {
                    zaloFriends.add(zaloFriend);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        return zaloFriends;
    }

}
