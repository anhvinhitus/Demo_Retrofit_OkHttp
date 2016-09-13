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
    private WeakReference<Activity> mActivityWeakReference;
    private AppGameWebViewProcessor.IWebViewListener mWebViewListener;

    GetNavigationCallback(Activity activity, AppGameWebViewProcessor.IWebViewListener webViewListener) {
        mActivityWeakReference = new WeakReference<>(activity);
        mWebViewListener = webViewListener;
    }

    @Override
    public void onReceiveValue(String value) {
        if (mActivityWeakReference.get() == null) {
            Timber.i("Activity reference has been nullified");
            return;
        }

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

            Activity activity = mActivityWeakReference.get();
            activity.setTitle(title);

            if (mWebViewListener != null) {
                mWebViewListener.setLogo(thumb);
            }


        } catch (Throwable t) {
            Timber.w(t, "Caught error while parsing navigation information");
        }
    }
}
