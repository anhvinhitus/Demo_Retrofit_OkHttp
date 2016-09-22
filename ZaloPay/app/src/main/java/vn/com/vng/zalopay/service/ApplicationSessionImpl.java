package vn.com.vng.zalopay.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.google.android.gms.iid.InstanceID;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import java.io.IOException;
import java.util.Collection;

import de.greenrobot.dao.AbstractDao;
import okhttp3.OkHttpClient;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.notification.ZPNotificationService;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 6/14/16.
 * Manage application session
 */
public class ApplicationSessionImpl implements ApplicationSession {

    private final Navigator navigator;
    private final Context applicationContext;
    private final DaoSession daoSession;

    private String mLoginMessage;


    public ApplicationSessionImpl(Context applicationContext, DaoSession daoSession, Navigator navigator) {
        this.applicationContext = applicationContext;
        this.navigator = navigator;
        this.daoSession = daoSession;
    }

    /**
     * Clear current user session and move to login state
     */
    public void clearUserSession() {
        //cancel notification
        NotificationManagerCompat nm = NotificationManagerCompat.from(applicationContext);
        nm.cancelAll();

        try {
            InstanceID.getInstance(applicationContext).deleteInstanceID();
        } catch (IOException e) {
            Timber.d("unsubscriber gcm exception %s", e);
        }

        clearMerchant();

        navigator.setLastTimeCheckPin(0);

        applicationContext.stopService(new Intent(applicationContext, ZPNotificationService.class));

        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();

        // move to login
        ZaloSDK.Instance.unauthenticate();

        // clear current user DB
        UserConfig userConfig = applicationComponent.userConfig();
        userConfig.clearConfig();
        userConfig.setCurrentUser(null);

        AndroidApplication.instance().releaseUserComponent();

        if (TextUtils.isEmpty(mLoginMessage)) {
            navigator.startLoginActivity(applicationContext, true);
        } else {
            AndroidApplication.instance().getAppComponent().globalEventService()
                    .enqueueMessageAtLogin(
                            SweetAlertDialog.NORMAL_TYPE,
                            applicationContext.getString(R.string.accept),
                            mLoginMessage);
            navigator.startLoginActivity(applicationContext, true);
            mLoginMessage = null;
        }
    }

    private void clearMerchant() {
        daoSession.getMerchantUserDao().deleteAll();
    }

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
}
