package vn.com.vng.zalopay.react.base;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by longlv on 3/13/17.
 * *
 */

class ExternalReactPresenter extends AbstractPresenter<IExternalReactView> {

    @Inject
    User mUser;

    @Inject
    Navigator mNavigator;

    private AppResourceStore.Repository mAppResourceRepository;
    private boolean mShowDialogWatingDownloadApp = false;

    @Inject
    public ExternalReactPresenter(AppResourceStore.Repository appResourceRepository) {
        mAppResourceRepository = appResourceRepository;
    }

    void checkResourceReady(long appId) {
        Timber.d("checkResourceReady appid[%s]", appId);
        Subscription subscription = mAppResourceRepository.existResource(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceReadySubscriber());
        mSubscription.add(subscription);
    }

    void checkResourceReadyWithoutDownload(long appId) {
        Timber.d("checkResourceReady appid[%s]", appId);
        Subscription subscription = mAppResourceRepository.existResource(appId, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceReadySubscriber());
        mSubscription.add(subscription);
    }

    private class ResourceReadySubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onNext(Boolean isAppAvailable) {
            Timber.d("onNext isAppAvailable[%s]", isAppAvailable);
            if (mView == null || mView.getActivity() == null) {
                return;
            }

            if (isAppAvailable) {
                mView.startReactApplication();
            } else if (!mShowDialogWatingDownloadApp) {
                mShowDialogWatingDownloadApp = true;
                mView.showWaitingDownloadApp();
            }
        }

    }

    @Override
    public void resume() {
        super.resume();
    }
}
