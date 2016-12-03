package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.view.IBalanceManagementView;
import vn.com.vng.zalopay.withdraw.ui.presenter.AbsWithdrawConditionPresenter;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 11/08/2016.
 * *
 */
public class BalanceManagementPresenter extends AbsWithdrawConditionPresenter
        implements IPresenter<IBalanceManagementView> {

    private IBalanceManagementView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private User mUser;
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;
    private Navigator mNavigator;

    @Inject
    BalanceManagementPresenter(User user,
                               EventBus eventBus,
                               BalanceStore.Repository balanceRepository,
                               Navigator navigator,
                               UserConfig userConfig) {
        super(userConfig);

        this.mUser = user;
        this.mEventBus = eventBus;
        this.mBalanceRepository = balanceRepository;
        this.mNavigator = navigator;
    }

    private void updateUserInfo() {
        mView.updateUserInfo(mUser);
    }

    @Override
    public void setView(IBalanceManagementView iWithdrawView) {
        mView = iWithdrawView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        mView.updateBalance(mBalanceRepository.currentBalance());
        updateBalance();
        updateUserInfo();
    }

    @Override
    public void pause() {
        mEventBus.unregister(this);
    }

    @Override
    public void destroy() {
        CShareData.dispose();
        GlobalData.initApplication(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ChangeBalanceEvent event) {
        Timber.d("onEventMainThread ChangeBalanceEvent");
        if (mView != null) {
            mView.updateBalance(event.balance);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline) {
            updateBalance();
        }
    }

    private void updateBalance() {
        Subscription subscription = mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        compositeSubscription.add(subscription);
    }

    public void startWithdrawActivity() {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        if (!isValidProfile()) {
            mNavigator.startWithdrawConditionActivity(mView.getContext());
        } else {
            validLinkCard(new IListenerValid() {
                @Override
                public void onSuccess(List<BankConfig> list, boolean isValid) {
                    if (mView == null || mView.getContext() == null) {
                        return;
                    }
                    if (isValid) {
                        mNavigator.startWithdrawActivity(mView.getContext());
                    } else {
                        mNavigator.startWithdrawConditionActivity(mView.getContext());
                    }
                }

                @Override
                public void onError(String error) {
                    if (mView == null || mView.getContext() == null) {
                        return;
                    }
                    mView.showConfirmDialog(error,
                            mView.getContext().getString(R.string.txt_retry),
                            mView.getContext().getString(R.string.txt_close),
                            new ZPWOnEventConfirmDialogListener() {
                                @Override
                                public void onCancelEvent() {

                                }

                                @Override
                                public void onOKevent() {
                                    startWithdrawActivity();
                                }
                            });
                }
            });
        }
    }

    @Override
    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    private int getProfileLevel() {
        User user = mUserConfig.getCurrentUser();
        if (user == null) {
            return 0;
        } else if (mUserConfig.getCurrentUser() == null) {
            return 0;
        } else {
            return mUserConfig.getCurrentUser().profilelevel;
        }
    }

    public void updateZaloPayID() {
        if (!TextUtils.isEmpty(mUser.zalopayname)) {
            return;
        }
        if (getProfileLevel() < 2) {
            requireUpdateProfileLevel2();
        } else {
            mNavigator.startEditAccountActivity(mView.getContext());
        }
    }

    private void requireUpdateProfileLevel2() {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        mView.showConfirmDialog(mView.getContext().getString(R.string.alert_need_update_level_2),
                mView.getContext().getString(R.string.txt_update),
                mView.getContext().getString(R.string.txt_close),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        mNavigator.startUpdateProfileLevel2Activity(mView.getContext());
                    }
                });
    }
}
