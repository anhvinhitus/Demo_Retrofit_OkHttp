package vn.com.zalopay.game.businnesslogic.base;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.callback.IAppGameResultListener;
import vn.com.zalopay.game.businnesslogic.interfaces.payment.IPaymentService;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.game.businnesslogic.provider.networking.INetworking;

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
	private static String mUrlConfig;
	private static INetworking mNetworking;
	private static IPaymentService mPayment;

	public static void setApplication(Activity pActivity, IPaymentService payment, AppGamePayInfo pAppGamePayInfo, IAppGameResultListener pListener,
									  IDialog pDialog, String webUrl, INetworking pNetworking) throws Exception
	{
		AppGameGlobal.mApplication 		= pActivity;
		AppGameGlobal.mAppGamePayInfo	= pAppGamePayInfo;
		AppGameGlobal.mResultListener	= pListener;

		AppGameGlobal.mPayment			= payment;
		AppGameGlobal.mDialog			= pDialog;
		AppGameGlobal.mUrlConfig		= webUrl;
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
		Timber.d("isResultChannel appTransId [%s]", mAppGamePayInfo.getApptransid());
		if(mAppGamePayInfo != null && ! TextUtils.isEmpty(mAppGamePayInfo.getApptransid()))
			return true;
		return false;
	}

	public static IPaymentService getPaymentService() {
		return mPayment;
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

	public static String getUrlConfig() {
		return mUrlConfig;
	}

	public static INetworking getNetworking() {
		return mNetworking;
	}

	public static void setNetworking(INetworking mNetworking) {
		AppGameGlobal.mNetworking = mNetworking;
	}
}
