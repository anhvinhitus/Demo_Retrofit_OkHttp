package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

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
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;

/**
 * Created by AnhHieu on 5/11/16.
 *
 */
public class LeftMenuPresenter extends BaseUserPresenter implements IPresenter<ILeftMenuView> {
    private ILeftMenuView menuView;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    User user;
    private EventBus eventBus;
    private TransactionStore.Repository transactionRepository;
    private UserConfig userConfig;
    private BalanceStore.Repository balanceRepository;
    private Context context;

    private boolean isInitiated;

    @Inject
    public LeftMenuPresenter(EventBus eventBus,
                             TransactionStore.Repository transactionRepository,
                             UserConfig userConfig,
                             BalanceStore.Repository balanceRepository,
                             Context context) {
        this.eventBus = eventBus;
        this.transactionRepository = transactionRepository;
        this.userConfig = userConfig;
        this.balanceRepository = balanceRepository;
        this.context = context;
    }

    @Override
    public void setView(ILeftMenuView iLeftMenuView) {
        menuView = iLeftMenuView;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    public void destroyView() {
        eventBus.unregister(this);
        unsubscribeIfNotNull(compositeSubscription);
        menuView = null;
    }

    public void initialize() {
        menuView.setUserInfo(user);
        this.initializeZaloPay();
    }

    private void initializeZaloPay() {
        Timber.d("initializeZaloPay transactionRepository [%s]", transactionRepository);
        Subscription subscription = transactionRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        isInitiated = true;
                    }
                });
        compositeSubscription.add(subscription);
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

    private void getBalance() {
        Subscription subscription = balanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Long>());

        compositeSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        //UPDATE USERINFO
        user.avatar = event.avatar;
        user.displayName = event.displayName;

        if (menuView != null) {
            menuView.setAvatar(event.avatar);
            menuView.setDisplayName(event.displayName);
        }

        eventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (!event.isOnline) {
            return;
        }
        if (!isInitiated) {
            this.getBalance();
            this.initializeZaloPay();
        }
        if (TextUtils.isEmpty(userConfig.getCurrentUser().displayName) ||
                TextUtils.isEmpty(userConfig.getCurrentUser().avatar)) {
            ZaloSDK.Instance.getProfile(context, new ZaloOpenAPICallback() {
                @Override
                public void onResult(JSONObject profile) {
                    try {
                        userConfig.saveZaloUserInfo(profile);
                    } catch (Exception ex) {
                        Timber.w(ex, " Exception :");
                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayNameEventMainThread(ZaloPayNameEvent event) {
        if (menuView != null) {
            menuView.setZaloPayName(event.zaloPayName);
        }
    }

}
