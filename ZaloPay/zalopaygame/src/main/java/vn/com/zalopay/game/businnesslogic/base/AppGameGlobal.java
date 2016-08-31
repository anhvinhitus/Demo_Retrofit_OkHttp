package vn.com.zalopay.game.businnesslogic.base;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.callback.IAppGameResultListener;
import vn.com.zalopay.game.businnesslogic.provider.config.IGetUrlConfig;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.game.businnesslogic.provider.networking.INetworking;
import vn.com.zalopay.game.controller.AppGameController;

public class AppGameGlobal
{
	private static Activity mApplication = null;
	/***
	 * callback to app
     */
	private static IAppGameResultListener mResultListener;
	/***
	 * payment info from app
	 */
	private static AppGamePayInfo mAppGamePayInfo;

	/***
	 * providers from main app.
     */
	private static IDialog mDialog;
	private static IGetUrlConfig mUrlConfig;
	private static INetworking mNetworking;

	public static void setApplication(Activity pActivity,AppGamePayInfo pAppGamePayInfo,IAppGameResultListener pListener,
									  IDialog pDialog, IGetUrlConfig pUrlConfig,INetworking pNetworking) throws Exception
	{
		AppGameGlobal.mApplication 		= pActivity;
		AppGameGlobal.mAppGamePayInfo	= pAppGamePayInfo;
		AppGameGlobal.mResultListener	= pListener;

		AppGameGlobal.mDialog			= pDialog;
		AppGameGlobal.mUrlConfig		= pUrlConfig;
		AppGameGlobal.mNetworking		= pNetworking;
	}

	public static Context getApplication() 
	{
		if(mApplication == null)
			return null;
		
		return mApplication.getBaseContext();
	}

	public static String getString(int pResourceID)
	{

		try
		{
			if (getApplication() == null)
				return null;

			return getApplication().getString(pResourceID);

		} catch (Exception e)
		{
			Log.e("getStringResource ",  e != null ? e.getMessage(): "error");

			return null;
		}
	}

	public static Activity getOwnerActivity() {
		return mApplication;
	}

	public static AppGamePayInfo getAppGamePayInfo()
	{
		return mAppGamePayInfo;
	}

	public static boolean isZingXuChannel()
	{
		if(mAppGamePayInfo != null && mAppGamePayInfo.getAppId() == Integer.parseInt(getString(R.string.appgame_zingxu_channel_id)))
			return true;
		return false;
	}

	public static boolean isPayForGameChannel()
	{
		if(mAppGamePayInfo != null && mAppGamePayInfo.getAppId() == Integer.parseInt(getString(R.string.appgame_payforgame_channel_id)))
			return true;
		return false;
	}

	public static boolean isResultChannel()
	{
		if(mAppGamePayInfo != null && ! TextUtils.isEmpty(mAppGamePayInfo.getApptransid()))
			return true;
		return false;
	}

	public static IAppGameResultListener getResultListener()
	{
		return mResultListener;
	}

	public static IDialog getDialog() {
		return mDialog;
	}

	public static void setDialog(IDialog mDialog) {
		AppGameGlobal.mDialog = mDialog;
	}

	public static IGetUrlConfig getUrlConfig() {
		return mUrlConfig;
	}

	public static void setUrlConfig(IGetUrlConfig mUrlConfig) {
		AppGameGlobal.mUrlConfig = mUrlConfig;
	}

	public static INetworking getNetworking() {
		return mNetworking;
	}

	public static void setNetworking(INetworking mNetworking) {
		AppGameGlobal.mNetworking = mNetworking;
	}
}
