package vn.com.vng.webapp.framework;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by huuhoa on 2/8/17.
 * handler for web-app communication
 */

class WebAppCommunicationHandler {
    private IWebView mWebView;
    private Map<String, NativeModule> mMessageHandlers = new HashMap<>();

    WebAppCommunicationHandler(IWebView webView) {
        mWebView = webView;
    }

    void registerNativeModule(@NonNull NativeModule nativeModule) {
        String[] messages = nativeModule.canProcessMessages();
        for (String message : messages) {
            if (mMessageHandlers.containsKey(message)) {
                continue;
            }

            mMessageHandlers.put(message, nativeModule);
        }
    }

    void unregisterNativeModule(@NonNull NativeModule nativeModule) {
        String[] messages = nativeModule.canProcessMessages();
        for (String message : messages) {
            if (mMessageHandlers.get(message) != nativeModule) {
                continue;
            }

            mMessageHandlers.remove(message);
        }
    }

    void cleanup() {
        mWebView = null;
        mMessageHandlers.clear();
    }

    void preHandleWebMessage(String messageData) {
        CallNativeModuleTask task = new CallNativeModuleTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messageData);
    }
//
//    private void callback(WebMessage originMessage, JSONObject data) {
//        WebMessage webMessage = new WebMessage();
//        webMessage.data = data;
//        webMessage.functionName = originMessage.functionName;
//        webMessage.messageId = originMessage.messageId;
//        webMessage.messageType = "callback";
//        webMessage.keepCallback = false;
//
//        if (mWebView != null) {
//            mWebView.invokeJS(webMessage);
//        }
//    }
//
//    private void processMessage(WebMessage message) throws Exception {
//        if ("showLoading".equalsIgnoreCase(message.functionName)) {
//            mWebViewListener.showLoading();
//        } else if ("hideLoading".equalsIgnoreCase(message.functionName)) {
//            mWebViewListener.hideLoading();
//        } else if ("showDialog".equalsIgnoreCase(message.functionName)) {
//            showDialog(message);
//        } else if ("closeWindow".equalsIgnoreCase(message.functionName)) {
//            finishActivity(message);
//        } else if ("vibrate".equalsIgnoreCase(message.functionName)) {
//            vibrator(message);
//        } else if ("payOrder".equalsIgnoreCase(message.functionName)) {
//            pay(message);
//        } else {
//            callback(message, failObject("Unknown function"));
//        }
//    }


    void callback(WebMessage webMessage) {
        if (mWebView != null) {
            mWebView.invokeJS(webMessage);
        }
    }

    private class CallNativeModuleTask extends AsyncTask<String, Void, WebMessage> {

        @Override
        protected WebMessage doInBackground(String ...messageData) {
            try {
                JSONObject object = new JSONObject(messageData[0]);

                String functionName = object.optString("func");
                JSONObject param = object.optJSONObject("param");
                String messageType = object.optString("msgType");
                String clientId = object.optString("clientId");

//            final WebMessage webMessage = new WebMessage();
//            webMessage.messageId = clientId;
//            webMessage.messageType = messageType;
//            webMessage.data = param;
//            webMessage.functionName = functionName;

                PromiseImpl promise;
                if (mMessageHandlers.containsKey(functionName)) {
                    promise = new PromiseImpl(WebAppCommunicationHandler.this, functionName, clientId);
                    NativeModule nativeModule = mMessageHandlers.get(functionName);
                    nativeModule.processMessage(functionName, messageType, param, promise);
                } else {
                    Timber.i("Unknown/Unsupported function: %s", functionName);
                    promise = new PromiseImpl(WebAppCommunicationHandler.this, functionName, clientId);
                    promise.reject(404, "Method Not Found");
                }

                return promise.mResult;
//            processMessage(webMessage);
            } catch (Exception e) {
                Timber.d(e, "Exception while parsing arguments");
            }

            return null;
        }

        @Override
        protected void onPostExecute(WebMessage webMessage) {
            super.onPostExecute(webMessage);

            if (webMessage == null) {
                return;
            }

            if (mWebView == null) {
                return;
            }

            mWebView.invokeJS(webMessage);
        }
    }
}

