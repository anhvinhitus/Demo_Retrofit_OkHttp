package vn.com.vng.zalopay.service;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.google.android.gms.iid.InstanceID;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.AbstractDao;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.event.SignOutEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 6/14/16.
 * Manage application session
 */
public class ApplicationSessionImpl implements ApplicationSession {

    private final Navigator navigator;
    private final Context applicationContext;
    private final DaoSession daoSession;
    private final EventBus eventBus;

    private String mLoginMessage;

    public ApplicationSessionImpl(Context applicationContext, DaoSession daoSession,
                                  Navigator navigator, EventBus eventBus) {
        this.applicationContext = applicationContext;
        this.navigator = navigator;
        this.daoSession = daoSession;
        this.eventBus = eventBus;
    }

    /**
     * Clear current user session and move to login state
     */
    @Override
    public void clearUserSession() {

        clearUserSessionWithoutSignOut();

        if (!TextUtils.isEmpty(mLoginMessage)) {
            AndroidApplication.instance().getAppComponent().globalEventService()
                    .enqueueMessageAtLogin(
                            SweetAlertDialog.NORMAL_TYPE,
                            applicationContext.getString(R.string.accept),
                            mLoginMessage);
        }
        
        navigator.startLoginActivity(applicationContext, true);
        mLoginMessage = null;
    }

    private void resetRecovery() {
        daoSession.getDataManifestDao().deleteByKey(Constants.MANIFEST_RECOVERY_NOTIFY);
        daoSession.getDataManifestDao().deleteByKey(Constants.MANIFEST_RECOVERY_TIME_NOTIFICATION);
    }

    public void clearMerchantSession() {
        daoSession.getMerchantUserDao().deleteAll();
    }

    private void deleteInstanceID() {
        try {
            InstanceID.getInstance(applicationContext).deleteInstanceID();
        } catch (IOException e) {
            Timber.d(e, "deleteInstanceID gcm exception");
        } catch (Exception ex) {
            Timber.d(ex, "deleteInstanceID error");
        }
    }

    private Observable<Boolean> taskLogout() {
        return ObservableHelper.makeObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                clearMerchantSession();
                deleteInstanceID();
                resetRecovery();

                navigator.setLastTimeCheckPin(0);

                return Boolean.TRUE;
            }
        });
    }

    @Override
    public void clearAllUserDB() {
        Timber.d("clearAllUserDB");
        try {
            daoSession.clear();
            Collection<AbstractDao<?, ?>> daoCollection = daoSession.getAllDaos();
            for (AbstractDao<?, ?> dao : daoCollection) {
                if (dao != null) {
                    dao.deleteAll();
                }
            }
        } catch (Exception e) {
            Timber.e(e, "exception");
        }
    }

    public void setMessageAtLogin(String message) {
        mLoginMessage = message;
    }

    @Override
    public void setMessageAtLogin(int message) {
        mLoginMessage = applicationContext.getString(message);
    }

    /**
     * New user session and move to main state
     */
    public void newUserSession() {

    }

    @Override
    public void cancelAllRequest() {
        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        OkHttpClient okHttpClient = applicationComponent.okHttpClient();
        okHttpClient.dispatcher().cancelAll();
    }

    @Override
    public void clearUserSessionWithoutSignOut() {
        eventBus.removeAllStickyEvents();
        eventBus.post(new SignOutEvent());
        //cancel notification
        NotificationManagerCompat nm = NotificationManagerCompat.from(applicationContext);
        nm.cancelAll();

        taskLogout().subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());

        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();

        // move to login
        ZaloSDK.Instance.unauthenticate();

        // clear current user DB
        UserConfig userConfig = applicationComponent.userConfig();
        userConfig.clearConfig();
        userConfig.setCurrentUser(null);

        AndroidApplication.instance().releaseUserComponent();

    }
}
