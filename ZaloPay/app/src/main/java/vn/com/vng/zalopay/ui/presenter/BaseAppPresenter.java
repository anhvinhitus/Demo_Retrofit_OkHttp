package vn.com.vng.zalopay.ui.presenter;


import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseAppPresenter {

    protected final String TAG = this.getClass().getSimpleName();

    @Inject
    protected EventBus eventBus;

    @Inject
    protected RxBus rxBus;

    @Inject
    protected PassportRepository passportRepository;

    @Inject
    protected UserConfig userConfig;

    @Inject
    protected Navigator mNavigator;

    protected final Context applicationContext = AndroidApplication.instance();

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
