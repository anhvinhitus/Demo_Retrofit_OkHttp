package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
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
    private User user;

    private EventBus mEventBus;
    
    private UserConfig mUserConfig;
    private Context context;

    @Inject
    LeftMenuPresenter(User user, EventBus mEventBus,
                      UserConfig userConfig,
                      Context context) {
        this.mEventBus = mEventBus;
        this.mUserConfig = userConfig;
        this.context = context;
        this.user = user;
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
        mView.setUserInfo(user);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        //UPDATE USERINFO
        user.avatar = event.avatar;
        user.displayName = event.displayName;

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

        if (TextUtils.isEmpty(mUserConfig.getCurrentUser().displayName) ||
                TextUtils.isEmpty(mUserConfig.getCurrentUser().avatar)) {
            ZaloSDK.Instance.getProfile(context, new ZaloOpenAPICallback() {
                @Override
                public void onResult(JSONObject profile) {
                    try {
                        mUserConfig.saveZaloUserInfo(profile);
                    } catch (Exception ex) {
                        Timber.w(ex, " Exception :");
                    }
                }
            });
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
