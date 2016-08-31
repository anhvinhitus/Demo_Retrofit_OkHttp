package vn.com.zalopay.game.ui.webview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;

public class AppGameWebViewProcessor extends WebViewClient
{

	private static final String JAVA_SCRIPT_INTERFACE_NAME = "zalopay_appgame";

	private AppGameWebView mWebView = null;

	public AppGameWebViewProcessor(AppGameWebView pWebView)
	{
		mWebView = pWebView;
		mWebView.setWebViewClient(this);
		mWebView.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
	}

	public void start(String pUrl, Activity pActivity, ITimeoutLoadingListener pTimeoutListener)
	{
		if(AppGameGlobal.getDialog() != null)
			AppGameGlobal.getDialog().showLoadingDialog(pActivity, pTimeoutListener);

		mWebView.loadUrl(pUrl);
	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
		if(AppGameGlobal.getDialog() != null)
			AppGameGlobal.getDialog().hideLoadingDialog();

		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon)
	{
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		Timber.d("===shouldOverrideUrlLoading===%s",url);

		//use case for url
		if( ! TextUtils.isEmpty(url) && url.equalsIgnoreCase(AppGameConfig.URL_TO_APP) && AppGameBaseActivity.getCurrentActivity() != null)
		{
			AppGameBaseActivity.getCurrentActivity().finish();
		}
		else if( ! TextUtils.isEmpty(url) && url.equalsIgnoreCase(AppGameConfig.URL_TO_LOGIN)
				&& AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity)
		{
			((AppGameActivity)AppGameBaseActivity.getCurrentActivity()).logout();
		}
		else
		{
			view.loadUrl(url);
		}

		return true;
	}

	public void onLoadResource(WebView view, String url)
	{
		Log.d("onLoadResource " , url);
	}

	@JavascriptInterface
	public void onJsCallBackResult(String pResult)
	{
	}
}
