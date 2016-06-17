package vn.com.vng.zalopay.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.ui.view.IZaloPayView;

/**
 * Created by AnhHieu on 5/9/16.
 */
public class ZaloPayPresenterImpl extends BaseUserPresenter implements ZaloPayPresenter<IZaloPayView> {

    private IZaloPayView mZaloPayView;

    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(IZaloPayView o) {
        this.mZaloPayView = o;
        eventBus.register(this);
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        eventBus.unregister(this);
        this.mZaloPayView = null;
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

    @Override
    public void initialize() {
    }

    public void listAppResource() {

        Subscription subscription = mAppResourceRepository.listAppResource()
                .delaySubscription(3, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());

        compositeSubscription.add(subscription);

    }

    private final void onGetAppResourceSuccess(List<AppResource> resources) {
        // mZaloPayView.insertApps(resources);
    }

    private final class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {
        public AppResourceSubscriber() {
        }

        @Override
        public void onNext(List<AppResource> appResources) {
            ZaloPayPresenterImpl.this.onGetAppResourceSuccess(appResources);

            Timber.d(" AppResource %s", appResources.size());
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, " Throwable AppResourceSubscriber ");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (!event.isOnline && mZaloPayView != null) {
            mZaloPayView.showNetworkError();
        }
    }

}
