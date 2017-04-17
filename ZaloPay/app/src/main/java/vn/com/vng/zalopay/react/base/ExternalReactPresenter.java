package vn.com.vng.zalopay.react.base;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

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

    public void sendActiveEvent(@Nullable ReactContext reactContext) {
        if (reactContext instanceof ReactApplicationContext) {
            sendEventToJs((ReactApplicationContext) reactContext, "zalopayShowShowActive", null);
        }
    }

    private void sendEventToJs(@Nullable ReactApplicationContext reactContext, String eventName, @Nullable Object data) {
        if (reactContext != null) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
        }
    }
}
