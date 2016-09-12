package vn.com.zalopay.game.ui.webview;

import android.app.Activity;
import android.webkit.ValueCallback;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by huuhoa on 9/11/16.
 * Callback for utils.getNav()
 */
class GetNavigationCallback implements ValueCallback<String> {
    private WeakReference<Activity> mActivityWeakReference;

    GetNavigationCallback(Activity activity) {
        mActivityWeakReference = new WeakReference<>(activity);
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

            String title = jsonObject.getAsJsonObject().get("title").getAsString();
            mActivityWeakReference.get().setTitle(title);
        } catch (Throwable t) {
            Timber.w(t, "Caught error while parsing navigation information");
        }
    }
}
