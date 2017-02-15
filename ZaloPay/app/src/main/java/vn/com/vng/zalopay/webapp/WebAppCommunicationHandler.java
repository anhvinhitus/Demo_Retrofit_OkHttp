package vn.com.vng.zalopay.webapp;

import android.content.Context;
import android.os.Vibrator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 2/8/17.
 * handler for web-app communication
 */

class WebAppCommunicationHandler {
    private IWebView mWebView;
    private IWebViewListener mWebViewListener;

    WebAppCommunicationHandler(IWebView webView, IWebViewListener webViewListener) {
        mWebViewListener = webViewListener;
        mWebView = webView;
    }

    void cleanup() {
        mWebView = null;
        mWebViewListener = null;
    }

    void preHandleWebMessage(String messageData) {
        try {
            JSONObject object = new JSONObject(messageData);

            String functionName = object.optString("func");
            JSONObject param = object.optJSONObject("param");
            String messageType = object.optString("msgType");
            String clientId = object.optString("clientId");

            final WebMessage webMessage = new WebMessage();
            webMessage.messageId = clientId;
            webMessage.messageType = messageType;
            webMessage.data = param;
            webMessage.functionName = functionName;

            processMessage(webMessage);
        } catch (Exception e) {
            Timber.d(e, "Exception while parsing arguments");
        }
    }

    private void callback(WebMessage originMessage, JSONObject data) {
        WebMessage webMessage = new WebMessage();
        webMessage.data = data;
        webMessage.functionName = originMessage.functionName;
        webMessage.messageId = originMessage.messageId;
        webMessage.messageType = "callback";
        webMessage.keepCallback = false;

        if (mWebView != null) {
            mWebView.invokeJS(webMessage);
        }
    }

    private void processMessage(WebMessage message) throws Exception {
        if ("showLoading".equalsIgnoreCase(message.functionName)) {
            mWebViewListener.showLoading();
        } else if ("hideLoading".equalsIgnoreCase(message.functionName)) {
            mWebViewListener.hideLoading();
        } else if ("showDialog".equalsIgnoreCase(message.functionName)) {
            showDialog(message);
        } else if ("closeWindow".equalsIgnoreCase(message.functionName)) {
            finishActivity(message);
        } else if ("vibrate".equalsIgnoreCase(message.functionName)) {
            vibrator(message);
        } else if ("payOrder".equalsIgnoreCase(message.functionName)) {
            pay(message);
        } else {
            callback(message, failObject("Unknown function"));
        }
    }

    private void pay(final WebMessage message) {
        if (message == null) {
            return;
        }
        if (mWebViewListener == null) {
            callback(message, failObject("Missing webview listener."));
        } else {
            mWebViewListener.pay(message.data, new IPaymentListener() {
                @Override
                public void onPayError(String param) {
                    callback(message, failObject(param));
                }

                @Override
                public void onPaySuccess() {
                    callback(message, successObject());
                }
            });
        }
    }

    private void finishActivity(final WebMessage message) {
        if (mWebViewListener != null) {
            mWebViewListener.finishActivity();
        }
        callback(message, successObject());
    }

    private void showDialog(final WebMessage message) {
        // {"title":"Hello","message":"ABC 123","button":"OK"}
        String title = message.data.optString("title");
        String content = message.data.optString("message");
        String buttonLabel = message.data.optString("button");

        if (mWebViewListener != null) {
            mWebViewListener.showDialog(SweetAlertDialog.NORMAL_TYPE, title, content, buttonLabel);
        }
        callback(message, successObject());
    }

    private void vibrator(final WebMessage message) {
        ObservableHelper.makeObservable(new Callable<JSONObject>() {
            @Override
            public JSONObject call() throws Exception {
                if (message.data != null) {
                    int duration = message.data.optInt("duration", 0);
                    if (duration == 0) {
                        duration = 500;
                    }

                    Vibrator v = (Vibrator) mWebViewListener.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(duration);
                }
                return successObject();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<JSONObject>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        JSONObject result = new JSONObject();

                        try {
                            result.put("error", 1);
                            result.put("errorMessage", e.getMessage());
                        } catch (JSONException ez) {

                        }

                        callback(message, result);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        callback(message, jsonObject);
                    }
                });
    }

    private JSONObject failObject(String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("error", 1);
            result.put("errorMessage", message);
        } catch (JSONException ez) {

        }

        return result;
    }

    private JSONObject successObject() {
        JSONObject result = new JSONObject();

        try {
            result.put("error", 0);
        } catch (JSONException ez) {

        }

        return result;
    }
}
