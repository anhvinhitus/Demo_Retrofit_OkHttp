package vn.com.vng.zalopay.ui.presenter;


import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseAppPresenter {

    protected final String TAG = this.getClass().getSimpleName();

    protected final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();

    protected final PassportRepository passportRepository = AndroidApplication.instance().getAppComponent().passportRepository();
    protected final UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();

    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    protected final Context applicationContext = AndroidApplication.instance();

    protected final ZPAnalytics zpAnalytics = AndroidApplication.instance().getAppComponent().zpAnalytics();

    protected void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                            subscriber.onCompleted();
                        } catch (Exception ex) {
                            try {
                                subscriber.onError(ex);
                            } catch (Exception ex2) {
                            }
                        }
                    }
                });
    }

    protected void clearAndLogout() {
        AndroidApplication.instance().getAppComponent().applicationSession().clearUserSession();
    }

    public void getZaloProfileInfo() {
        ZaloSDK.Instance.getProfile(applicationContext, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.w(ex, " Exception :");
                }
            }
        });
    }
}
