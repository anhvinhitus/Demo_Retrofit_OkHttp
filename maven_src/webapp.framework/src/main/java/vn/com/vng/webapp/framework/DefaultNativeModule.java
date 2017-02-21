package vn.com.vng.webapp.framework;

import org.json.JSONObject;

/**
 * Created by huuhoa on 2/18/17.
 * Default native module handlers
 */

class DefaultNativeModule implements NativeModule {
    private IWebViewListener mWebViewListener;
    private static String MESSAGE_CLOSE_WINDOW = "closeWindow";

    DefaultNativeModule(IWebViewListener webViewListener) {
        this.mWebViewListener = webViewListener;
    }

    @Override
    public void processMessage(String messageName, String messageType, JSONObject data, Promise promise) {
        if (MESSAGE_CLOSE_WINDOW.equalsIgnoreCase(messageName)) {
            if (mWebViewListener != null) {
                mWebViewListener.finishActivity();
            }

            promise.resolve(null);
        }
    }

    @Override
    public String[] canProcessMessages() {
        return new String[] { MESSAGE_CLOSE_WINDOW, };
    }
}
