package vn.com.vng.zalopay.requestsupport;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

public class ChooseCategoryPresenter extends AbstractPresenter<IChooseCategoryView> {
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

    public List<AppResource> fetchAppResource() {
        final List<AppResource> appResources = new ArrayList<>();
        Subscription fetchSubscription = mAppResourceRepository.fetchAppResource()
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<AppResource>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<AppResource> appResourceList) {
                        appResources.addAll(appResourceList);
                    }
                });

        return appResources;
    }
}
