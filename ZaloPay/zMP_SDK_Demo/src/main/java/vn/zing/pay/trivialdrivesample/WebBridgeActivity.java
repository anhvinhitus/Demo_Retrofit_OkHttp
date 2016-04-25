package vn.zing.pay.trivialdrivesample;

//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeReader;
//import com.google.zxing.qrcode.QRCodeWriter;
//import com.google.zxing.qrcode.encoder.QRCode;

import vn.zing.pay.zmpsdk.analysis.JavaInstanceTracker;
import vn.zing.pay.zmpsdk.business.atm.webview.WebPaymentBridge;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class WebBridgeActivity extends AppCompatActivity {

	WebPaymentBridge bridge = null;
	EditText editText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("onCreate", getIntent().toString());
		setContentView(R.layout.web_bridge);

		editText = (EditText) findViewById(R.id.txtJs);

		findViewById(R.id.btnUrl).setOnClickListener(mClickTestListener);
		findViewById(R.id.btnOpenBrowser).setOnClickListener(mClickOpenListener);
		findViewById(R.id.btnSendSMS).setOnClickListener(mClickSmsListener);
		findViewById(R.id.btnTrack).setOnClickListener(mClickTrack);
		bridge = (WebPaymentBridge) findViewById(R.id.webView);

		findViewById(R.id.btnRunJs).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				bridge.runScript(editText.getText().toString());
			}
		});

		findViewById(R.id.btnQR).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// this is a small sample use of the QRCodeEncoder class from
				// zxing
//				try {
//					// image_view.setImageBitmap(bm);
//					QRCodeWriter writer = new QRCodeWriter();
//					BitMatrix matrix = writer.encode(editText.getText().toString(), BarcodeFormat.QR_CODE, 800, 800);
//					((ImageView) findViewById(Resource.id.qrImageView)).setImageBitmap(toBitmap(matrix));
//				} catch (Exception e) { // eek
//
//				}
			}
		});

		bridge.setWebViewClient(new CWebclient(bridge));

		findViewById(R.id.btnAnother).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent sendIntent = new Intent("demo123pay.pay.zing.vn.receivedintentapp.ANOTHER_ACTIVITY");
				sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
				sendIntent.setType("text/plain");
				startActivityForResult(sendIntent, 12345);

				// Intent sendIntent2 = new
				// Intent("demo123pay.pay.zing.vn.receivedintentapp.ANOTHER_ACTIVITY");
				// sendIntent.putExtra(Intent.EXTRA_TEXT,
				// "This is my text to send.");
				// sendIntent.setType("text/plain");
				// startActivityForResult(sendIntent, 12345);
			}
		});
	}

//	/**
//	 * Writes the given Matrix on a new Bitmap object.
//	 *
//	 * @param matrix
//	 *            the matrix to write.
//	 * @return the new {@link Bitmap}-object.
//	 */
//	public static Bitmap toBitmap(BitMatrix matrix) {
//		int height = matrix.getHeight();
//		int width = matrix.getWidth();
//		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
//			}
//		}
//		return bmp;
//	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.i("onNewIntent", intent.toString());

		Uri uri = intent.getData();

		if (uri != null) {
			// Intent callback from browser (credit card)
			Toast.makeText(this, uri.getHost() + "|" + uri.getQuery(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("onActivityResult", (data == null) ? "null" : data.toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("onResume", getIntent().toString());
	}

	private OnClickListener mClickTestListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!TextUtils.isEmpty(editText.getText().toString())) {
				bridge.loadUrl(editText.getText().toString());
			} else {
				bridge.loadUrl("http://html5doctor.com/demos/forms/forms-example.html");
			}
		}
	};

	private OnClickListener mClickOpenListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = ConnectionUtil.getBrowserIntent(WebBridgeActivity.this, "https://jsfiddle.net/nsk4e00n/2/");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			WebBridgeActivity.this.startActivityForResult(intent, 12345);
		}
	};

	private OnClickListener mClickSmsListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			String defApp = Settings.Secure.getString(WebBridgeActivity.this.getContentResolver(),
					"sms_default_application");
			Uri smsUri = Uri.parse("smsto:6969");

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setPackage(defApp);
			intent.putExtra("sms_body", "Sau chin sau chin");
			intent.putExtra("address", "6969");
			intent.putExtra(Intent.EXTRA_TEXT, "Sau chin sau chin");
			intent.setData(smsUri);
			WebBridgeActivity.this.startActivityForResult(intent, 12345);
		}
	};

	private OnClickListener mClickTrack = new OnClickListener() {

		@Override
		public void onClick(View v) {
			JavaInstanceTracker.collectGarbage();
		}
	};

	public static class CWebclient extends WebViewClient {
		boolean loadingFinished = true;
		boolean redirect = false;
		WebView mWebPaymentBridge = null;

		public CWebclient(WebView pWebPaymentBridge) {
			mWebPaymentBridge = pWebPaymentBridge;
			mWebPaymentBridge.addJavascriptInterface(this, "zmpsdk_wv");
		}

		public void onLoadResource(WebView view, String url) {
			// Log.d(this, "///// onLoadResource: " + url);
		}

		@JavascriptInterface
		public void logDebug(String msg) {
			Log.d(this, "****** Debug webview: " + msg);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(this, "///// shouldOverrideUrlLoading: " + url);
			if (!loadingFinished) {
				redirect = true;
			}

			loadingFinished = false;
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap facIcon) {
			Log.i(this, "///// onPageStarted: " + url);
			loadingFinished = false;
			// SHOW LOADING IF IT ISNT ALREADY VISIBLE
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(this, "///// onPageFinished: " + url);
			if (!redirect) {
				loadingFinished = true;
			}

			if (loadingFinished && !redirect) {
				Log.e(this, "=========== ALREADY FINISHED ===========");
				Log.e(this, url);
			} else {
				redirect = false;
			}

		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			Log.e(this, "==== current error SSL on page: " + mWebPaymentBridge.getUrl() + " | " + mWebPaymentBridge.getOriginalUrl());
			handler.proceed(); // Ignore SSL certificate errors
		}

		@JavascriptInterface
		public void onJsPaymentResult(String pResult) {
			Log.d(this, "///// onJsPaymentResult: " + pResult);
		}
	}
}
