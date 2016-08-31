package vn.com.zalopay.game.ui.webview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.parceler.Parcels;

import timber.log.Timber;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.config.AppGameConfig;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;

public class AppGameWebViewProcessor extends WebViewClient {

    private static final String JAVA_SCRIPT_INTERFACE_NAME = "zalopay_appgame";

    private AppGameWebView mWebView = null;

    public AppGameWebViewProcessor(AppGameWebView pWebView) {
        mWebView = pWebView;
        mWebView.setWebViewClient(this);
        mWebView.addJavascriptInterface(this, JAVA_SCRIPT_INTERFACE_NAME);
    }

    public void start(String pUrl, Activity pActivity, ITimeoutLoadingListener pTimeoutListener) {
        if (AppGameGlobal.getDialog() != null)
            AppGameGlobal.getDialog().showLoadingDialog(pActivity, pTimeoutListener);

        mWebView.loadUrl(pUrl);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (AppGameGlobal.getDialog() != null)
            AppGameGlobal.getDialog().hideLoadingDialog();

        super.onPageFinished(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("===shouldOverrideUrlLoading===%s", url);

        //use case for url
        if (!TextUtils.isEmpty(url) && url.equalsIgnoreCase(AppGameConfig.URL_TO_APP) &&
                AppGameBaseActivity.getCurrentActivity() != null) {
            AppGameBaseActivity.getCurrentActivity().setResult(Activity.RESULT_CANCELED);
            AppGameBaseActivity.getCurrentActivity().finish();
        } else if (!TextUtils.isEmpty(url) && url.equalsIgnoreCase(AppGameConfig.URL_TO_LOGIN)
                && AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity) {
            ((AppGameActivity) AppGameBaseActivity.getCurrentActivity()).logout();
        } else if (url.startsWith("zalopay-1://post")) {
            payOrder(url);
        } else {
            view.loadUrl(url);
        }

        return true;
    }

    private void payOrder(String url) {
        //Check param valid
        Uri data = Uri.parse(url);
        String muid = data.getQueryParameter("muid");
        String  accesstoken = data.getQueryParameter("maccesstoken");
        String appid = data.getQueryParameter("appid");
        String apptransid = data.getQueryParameter("apptransid");
        String appuser = data.getQueryParameter("appuser");
        String apptime = data.getQueryParameter("apptime");
        String item = data.getQueryParameter("item");
        String description = data.getQueryParameter("description");
        String embeddata = data.getQueryParameter("embeddata");
        String amount = data.getQueryParameter("amount");
        String mac = data.getQueryParameter("mac");

        if (TextUtils.isEmpty(muid) ||
                TextUtils.isEmpty(apptransid) ||
                TextUtils.isEmpty(appuser) ||
                TextUtils.isEmpty(amount) ||
                TextUtils.isEmpty(mac)) {
            Toast.makeText(AppGameBaseActivity.getCurrentActivity(), "Dữ liệu thanh toán không hợp lệ.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("muid", muid);
        bundle.putString("accesstoken", accesstoken);
        bundle.putString("appid", appid);
        bundle.putString("apptransid", apptransid);
        bundle.putString("appuser", appuser);
        bundle.putString("apptime", apptime);
        bundle.putString("item", item);
        bundle.putString("description", description);
        bundle.putString("embeddata", embeddata);
        bundle.putString("amount", amount);
        bundle.putString("mac", mac);
        bundle.putParcelable("AppGamePayInfo", Parcels.wrap(AppGameGlobal.getAppGamePayInfo()));

        Timber.d("onResponseSuccess appId [%s]", AppGameGlobal.getAppGamePayInfo().getAppId());
        Timber.d("onResponseSuccess getApptransid [%s]", AppGameGlobal.getAppGamePayInfo().getApptransid());
        Timber.d("onResponseSuccess getUid [%s]", AppGameGlobal.getAppGamePayInfo().getUid());
        Timber.d("onResponseSuccess getAccessToken [%s]", AppGameGlobal.getAppGamePayInfo().getAccessToken());

        intent.putExtras(bundle);
        AppGameBaseActivity.getCurrentActivity().setResult(Activity.RESULT_OK, intent);
        AppGameBaseActivity.getCurrentActivity().finish();
    }

    public void onLoadResource(WebView view, String url) {
        Log.d("onLoadResource ", url);
    }

    @JavascriptInterface
    public void onJsCallBackResult(String pResult) {
    }
}
