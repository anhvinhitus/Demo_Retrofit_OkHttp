package vn.com.zalopay.game.webview;

import android.app.Activity;
import android.webkit.ValueCallback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.ref.WeakReference;

import timber.log.Timber;


/**
 * Created by huuhoa on 9/11/16.
 * Callback for utils.getNav()
 */
class GetNavigationCallback implements ValueCallback<String> {
    private AppGameWebViewProcessor.IWebViewListener mWebViewListener;

    GetNavigationCallback(AppGameWebViewProcessor.IWebViewListener webViewListener) {
        mWebViewListener = webViewListener;
    }

    @Override
    public void onReceiveValue(String value) {
        Timber.d("result of utils.getNav(): %s", value);
        if (value == null) {
            return;
        }

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonObject = parser.parse(value);
            if (jsonObject == null || !jsonObject.isJsonObject()) {
                return;
            }

            JsonObject data = jsonObject.getAsJsonObject();
            String title = data.get("title").getAsString();
            String thumb = data.get("thumb").getAsString();

            if (mWebViewListener != null) {
                mWebViewListener.setTitleAndLogo(title, thumb);
            }


        } catch (Throwable t) {
            Timber.w(t, "Caught error while parsing navigation information");
        }
    }
}
