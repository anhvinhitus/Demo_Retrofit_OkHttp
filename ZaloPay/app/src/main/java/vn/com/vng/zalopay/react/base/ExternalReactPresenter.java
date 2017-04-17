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
    private boolean mShowDialogWaitingDownloadApp = false;

    @Inject
    public ExternalReactPresenter(AppResourceStore.Repository appResourceRepository) {
        mAppResourceRepository = appResourceRepository;
    }

    void checkResourceReady(long appId) {
        Subscription subscription = mAppResourceRepository.existResource(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceReadySubscriber(appId));
        mSubscription.add(subscription);
    }

    void checkResourceReadyWithoutDownload(long appId) {
        Subscription subscription = mAppResourceRepository.existResource(appId, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceReadySubscriber(appId));
        mSubscription.add(subscription);
    }

    private class ResourceReadySubscriber extends DefaultSubscriber<Boolean> {

        private long appId;

        ResourceReadySubscriber(long appId) {
            this.appId = appId;
        }

        @Override
        public void onNext(Boolean isAppAvailable) {
            Timber.d("Check resource available : appid [%s] available [%s]", appId, isAppAvailable);
            if (mView == null || mView.getActivity() == null) {
                return;
            }

            if (isAppAvailable) {
                mView.startReactApplication();
            } else if (!mShowDialogWaitingDownloadApp) {
                mShowDialogWaitingDownloadApp = true;
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
            Timber.d("sendEventToJs : eventName [%s]", eventName);
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
        }
    }
}
