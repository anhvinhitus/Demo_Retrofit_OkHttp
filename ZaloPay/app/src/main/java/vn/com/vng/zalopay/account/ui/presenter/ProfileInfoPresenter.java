package vn.com.vng.zalopay.account.ui.presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 19/05/2016.
 * *
 */
public class ProfileInfoPresenter extends BaseUserPresenter implements IPresenter<IProfileInfoView> {

    IProfileInfoView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private EventBus mEventBus;
    private UserConfig mUserConfig;


    @Inject
    public ProfileInfoPresenter(EventBus eventBus, UserConfig userConfig) {
        this.mEventBus = eventBus;
        this.mUserConfig = userConfig;
    }

    @Override
    public void attachView(IProfileInfoView iProfileInfoView) {
        mView = iProfileInfoView;
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        unsubscribeIfNotNull(compositeSubscription);
        mEventBus.unregister(this);
        mView = null;
    }

    @Override
    public void resume() {
        mView.updateUserInfo(mUserConfig.getCurrentUser());
    }


    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        detachView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        Timber.d("onEventMainThread event %s", event);
        //UPDATE USERINFO
        if (mView != null) {
            mView.updateUserInfo(mUserConfig.getCurrentUser());
        }

        mEventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayNameEventMainThread(ZaloPayNameEvent event) {
        if (mView != null) {
            mView.setZaloPayName(event.zaloPayName);
        }
    }
}
