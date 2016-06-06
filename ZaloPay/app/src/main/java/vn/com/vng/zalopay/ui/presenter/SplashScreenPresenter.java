package vn.com.vng.zalopay.ui.presenter;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;

/**
 * Created by AnhHieu on 5/13/16.
 */
@Singleton
public class SplashScreenPresenter extends BaseAppPresenter implements IPresenter<ISplashScreenView> {


    @Inject
    public SplashScreenPresenter() {
    }

    private ISplashScreenView mView;

    private Subscription verifySubscription;

    @Override
    public void setView(ISplashScreenView iSplashScreenView) {
        mView = iSplashScreenView;
    }

    @Override
    public void destroyView() {
        mView = null;
        unsubscribeIfNotNull(verifySubscription);
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

    public void verifyUser() {

        Timber.tag(TAG);
        Timber.d("verifyUser");

        if (userConfig.isClientActivated()) {
            mView.showLoading();

            User user = userConfig.getCurrentUser();

            Timber.d("verifyUser");
            verifySubscription = passportRepository.verifyAccessToken(user.uid, user.accesstoken)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new VerifySubscriber());

        } else {
            verifySubscription = Observable.timer(1, TimeUnit.SECONDS)
                    .subscribe(new DefaultSubscriber<Long>() {

                        @Override
                        public void onCompleted() {
                            SplashScreenPresenter.this.onVerifyComplete(false, false);
                        }
                    });

            Timber.d("gotoLoginScreen");

        }
    }

    private void onVerifyComplete(boolean isVerifySuccess, boolean clearData) {

        Timber.d("onVerifyComplete %s %s", isVerifySuccess, clearData);

        mView.hideLoading();
        if (isVerifySuccess) {

            getZaloProfileInfo();

            mView.gotoHomeScreen();

        } else {
            if (clearData) {
                clearData();
            }
            mView.gotoLoginScreen();
        }
    }

    private void getZaloProfileInfo() {
        ZaloSDK.Instance.getProfile(applicationContext, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.tag(TAG).e(ex, " Exception :");
                }
            }
        });
    }


    private final class VerifySubscriber extends DefaultSubscriber<Boolean> {
        @Override
        public void onError(Throwable e) {
            SplashScreenPresenter.this.onVerifyComplete(false, true);
        }

        @Override
        public void onCompleted() {
            SplashScreenPresenter.this.onVerifyComplete(true, false);
        }
    }
}
