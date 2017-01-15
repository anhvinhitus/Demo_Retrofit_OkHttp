package vn.com.vng.zalopay.requestsupport;

import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

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

    void fetchAppResource() {
        Subscription subscription = mAppResourceRepository.fetchAppResource()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<List<AppResource>>() {

                    @Override
                    public void onStart() {
                        if (mView != null) {
                            mView.showLoading();
                        }
                    }

                    @Override
                    public void onNext(List<AppResource> appResourceList) {
                        if (mView == null) {
                            return;
                        }
                        mView.setData(appResourceList);
                        mView.hideLoading();
                    }
                });

        mSubscription.add(subscription);

    }
}
