package vn.zing.pay.zmpsdk.business.atm.webview;

import vn.zing.pay.zmpsdk.utils.Log;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WebPaymentBridge extends WebView {

	public WebPaymentBridge(Context context) {
		super(context);
		init(context);
	}

	public WebPaymentBridge(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WebPaymentBridge(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	private void init(Context context) {
		Log.i("Zmp", "WebPaymentBridge.init");
		getSettings().setJavaScriptEnabled(true);
		getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		getSettings()
				.setUserAgentString(
						"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36");

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
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
					JsPromptResult result) {
				return true;
			}
		});

	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void runScript(String scriptContent) {
		Log.w(this, "##### runScript: " + scriptContent);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			evaluateJavascript(scriptContent, null);
		} else {
			loadUrl("javascript:{" + scriptContent + "}");
		}
	}

}
