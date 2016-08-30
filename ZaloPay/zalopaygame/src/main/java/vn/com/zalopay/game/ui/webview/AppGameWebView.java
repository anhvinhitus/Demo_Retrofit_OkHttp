package vn.com.zalopay.game.ui.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;

public class AppGameWebView extends WebView {

	public AppGameWebView(Context context) {
		super(context);
		init(context);
	}

	public AppGameWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AppGameWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	private void init(Context context) {
		getSettings().setJavaScriptEnabled(true);
		getSettings().setDomStorageEnabled(true);
		getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		getSettings().setUserAgentString(
						"Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");

		getSettings().setBlockNetworkImage(false);
		getSettings().setLoadWithOverviewMode(true);
		getSettings().setLoadsImagesAutomatically(true);
		getSettings().setSavePassword(false);
		getSettings().setSaveFormData(false);
		setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				result.confirm();
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				return true;
			}
		});

	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void runScript(String scriptContent)
	{
		AppGameGlobal.getLog().d("AppGameWebView", "##### runScript: " + scriptContent);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			evaluateJavascript(scriptContent, null);
		}
		else
		{
			loadUrl("javascript:{" + scriptContent + "}");
		}
	}

}
