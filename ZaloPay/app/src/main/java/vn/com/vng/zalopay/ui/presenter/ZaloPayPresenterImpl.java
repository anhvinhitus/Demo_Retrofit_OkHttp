package vn.com.vng.zalopay.ui.presenter;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
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
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
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


    public void listAppResouce() {

        Subscription subscription = appConfigRepository.listAppResource()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResouceSubscriber());

        compositeSubscription.add(subscription);

    }

    private final void onGetAppResourceSuccess(List<AppResource> resources) {
        mZaloPayView.insertApps(resources);
    }


    private final class AppResouceSubscriber extends DefaultSubscriber<List<AppResource>> {
        public AppResouceSubscriber() {
        }

        @Override
        public void onNext(List<AppResource> appResources) {
            ZaloPayPresenterImpl.this.onGetAppResourceSuccess(appResources);
        }
    }

}
