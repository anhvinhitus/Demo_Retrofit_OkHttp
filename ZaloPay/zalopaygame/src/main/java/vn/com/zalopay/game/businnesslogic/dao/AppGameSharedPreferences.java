package vn.com.zalopay.game.businnesslogic.dao;

import android.content.SharedPreferences;

import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.base.AppGameBaseSingleton;

public class AppGameSharedPreferences extends AppGameBaseSingleton {

	private static AppGameSharedPreferences mSharePreferencesManager = null;

	public static synchronized AppGameSharedPreferences getInstance()
	{
		if (mSharePreferencesManager == null)
			mSharePreferencesManager = new AppGameSharedPreferences();

		return mSharePreferencesManager;
	}

	public static final String SHARE_PREFERENCES_NAME = "ZALOPAYGAME_CACHED";

	private SharedPreferences mCommonSharedPreferences = null;

	public AppGameSharedPreferences() {
		super();
	}

	public synchronized SharedPreferences getSharedPreferences()
	{
		if (AppGameGlobal.getApplication() == null) {
			return null;
		}

		if (mCommonSharedPreferences != null)
			return mCommonSharedPreferences;

		mCommonSharedPreferences = AppGameGlobal.getApplication().getSharedPreferences(SHARE_PREFERENCES_NAME, 0);
		return mCommonSharedPreferences;
	}
	private String getString(String pKey) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getString(pKey, null);

		return null;
	}

	public boolean setString(String pKey, String pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putString(pKey, pValue).commit();
		}

		return false;
	}

	private long getLong(String pKey) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getLong(pKey, 0);

		return 0;
	}

	public boolean setLong(String pKey, long pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putLong(pKey, pValue).commit();
		}

		return false;
	}

	/**
	 * Retrieve a int value from the preferences.
	 * 
	 * @param pKey
	 *            The name of the preference to retrieve.
	 * 
	 * @return Returns the preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a int.
	 */
	private int getInt(String pKey) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getInt(pKey, Integer.MIN_VALUE);

		return Integer.MIN_VALUE;
	}

	public boolean setInt(String pKey, int pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putInt(pKey, pValue).commit();
		}

		return false;
	}

	private boolean getBoolean(String pKey, boolean defaultValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null)
			return sharedPreferences.getBoolean(pKey, defaultValue);

		return defaultValue;
	}

	public boolean setBoolean(String pKey, boolean pValue) {
		SharedPreferences sharedPreferences = getSharedPreferences();

		if (sharedPreferences != null) {
			return sharedPreferences.edit().putBoolean(pKey, pValue).commit();
		}

		return false;
	}
}
