package vn.com.zalopay.wallet.repository;

import timber.log.Timber;

/**
 * Created by chucvv on 6/7/17.
 */

public class AbstractLocalStorage {
    protected SharedPreferencesManager mSharedPreferences;

    public AbstractLocalStorage(SharedPreferencesManager pSharedPreferencesManager) {
        this.mSharedPreferences = pSharedPreferencesManager;
        Timber.d("create LocalStorage");
    }

    public interface LocalStorage{
        SharedPreferencesManager sharePref();
    }
}
