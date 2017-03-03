package vn.com.vng.zalopay;

import android.app.Application;

import com.facebook.stetho.Stetho;

import timber.log.Timber;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;

/**
 * Created by admin on 9/19/16.
 */
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //Timber.plant(new CrashReportingTree());
            Timber.plant(new Timber.DebugTree());
        }
        WalletSDKApplication.wrap(this);
        Stetho.initializeWithDefaults(this);
    }
}
