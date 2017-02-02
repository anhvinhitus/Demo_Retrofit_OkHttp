package vn.com.vng.zalopay.webapp;

import timber.log.Timber;

/**
 * Created by huuhoa on 2/2/17.
 * Javascript interface for bridge communication between JS and native
 */
public class JavascriptInterface {
    @android.webkit.JavascriptInterface
    void sayHello(String name) {
        Timber.d("Hello %s", name);
    }
}
