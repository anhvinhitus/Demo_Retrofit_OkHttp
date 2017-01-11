package vn.com.vng.zalopay.requestsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

public class ChooseCategoryPresenter extends AbstractPresenter<ILoadDataView> {
    private final AppResourceStore.Repository mAppResourceRepository;

    @Inject
    ChooseCategoryPresenter(AppResourceStore.Repository appResourceRepository) {

        this.mAppResourceRepository = appResourceRepository;
    }

    @Override
    public void detachView() {
        hideLoadingView();
        super.detachView();
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    public List<AppResource> fetchAppResource() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<AppResource> appResources = new ArrayList<>();

        Subscription fetchSubscription = mAppResourceRepository.fetchAppResource()
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<AppResource>>() {
                    @Override
                    public void onCompleted() {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onNext(List<AppResource> appResourceList) {
                        countDownLatch.countDown();
                        appResources.addAll(appResourceList);
                    }
                });

        countDownLatch.await(2, TimeUnit.SECONDS);
        return appResources;
    }
}
