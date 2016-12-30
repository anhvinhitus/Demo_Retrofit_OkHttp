package vn.com.vng.zalopay.webview.widget;

import android.webkit.ValueCallback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import timber.log.Timber;


/**
 * Created by huuhoa on 9/11/16.
 * Callback for utils.getNav()
 */
class GetNavigationCallback implements ValueCallback<String> {
    private INavigationListener mNavigationListener;

    GetNavigationCallback(INavigationListener listener) {
        mNavigationListener = listener;
    }

    @Override
    public void onReceiveValue(String value) {
        Timber.d("result of utils.getNav(): %s", value);

        boolean appState = false;
        try {
            if (value == null) {
                return;
            }

            JsonParser parser = new JsonParser();
            JsonElement jsonObject = parser.parse(value);
            if (jsonObject == null || !jsonObject.isJsonObject()) {
                return;
            }

            JsonObject data = jsonObject.getAsJsonObject();
            int code = data.get("returnCode").getAsInt();
            if (code >= 1) {
                JsonObject nav = data.getAsJsonObject("nav");
                String title = nav.get("title").getAsString();
                String thumb = nav.get("thumb").getAsString();

                if (mNavigationListener != null) {
                    mNavigationListener.setTitleAndLogo(title, thumb);

                    if (code == 1) {
                        appState = true;
                    }
                }
            } else {
                Timber.d("WebApp is not valid");
            }
        } catch (Throwable t) {
            Timber.w(t, "Caught error while parsing navigation information");
        } finally {
            setWebAppState(appState);
        }
    }

    private void setWebAppState(boolean status) {
        if (mNavigationListener != null) {
            mNavigationListener.onWebAppStateChanged(status);
        }
    }

    interface INavigationListener {
        void setTitleAndLogo(String title, String thumb);

        void onWebAppStateChanged(boolean valid);
    }
}
