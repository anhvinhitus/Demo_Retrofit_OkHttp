package vn.com.zalopay.wallet.repository;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;

/**
 * Created by chucvv on 6/7/17.
 */

public class AbstractLocalStorage {
    protected SharedPreferencesManager mSharedPreferences;

    public AbstractLocalStorage(SharedPreferencesManager pSharedPreferencesManager) {
        this.mSharedPreferences = pSharedPreferencesManager;
        Log.d(this, "create LocalStorage");
    }
}
