package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/11/16.
 * *
 */
public class LeftMenuPresenter extends AbstractPresenter<ILeftMenuView> {
    private User mUser;

    private EventBus mEventBus;

    private UserConfig mUserConfig;
    private Context context;

    @Inject
    ZaloSdkApi mZaloSdkApi;

    @Inject
    LeftMenuPresenter(User user, EventBus mEventBus,
                      UserConfig userConfig,
                      Context context) {
        this.mEventBus = mEventBus;
        this.mUserConfig = userConfig;
        this.context = context;
        this.mUser = user;
        Timber.d("accessToken[%s]", userConfig.getCurrentUser().accesstoken);
    }

    @Override
    public void attachView(ILeftMenuView iLeftMenuView) {
        super.attachView(iLeftMenuView);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        super.detachView();
    }

    public void initialize() {
        listMenuItem();
        mView.setUserInfo(mUser);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        //UPDATE USERINFO
        mUser.avatar = event.avatar;
        mUser.displayName = event.displayName;

        if (mView != null) {
            mView.setAvatar(event.avatar);
            mView.setDisplayName(event.displayName);
        }

        mEventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (!event.isOnline) {
            return;
        }

        if (TextUtils.isEmpty(mUser.displayName) ||
                TextUtils.isEmpty(mUser.avatar)) {
            mZaloSdkApi.getProfile();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayNameEventMainThread(ZaloPayNameEvent event) {
        if (mView != null) {
            mView.setZaloPayName(event.zaloPayName);
        }
    }

    private void listMenuItem() {
        if (mView == null) {
            return;
        }

        List<MenuItem> listItem = MenuItemUtil.getMenuItems();
        try {
            boolean isEnableDeposit = CShareData.getInstance().isEnableDeposite();
            if (!isEnableDeposit) {
                listItem.remove(new MenuItem(MenuItemUtil.DEPOSIT_ID));
            }
        } catch (Exception e) {
            //empty
        }

        if (mView != null) {
            mView.setMenuItem(listItem);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent event) {
        Timber.d("onRefreshPlatformInfoEvent");
        listMenuItem();
    }
}
