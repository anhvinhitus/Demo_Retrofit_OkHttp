package vn.com.vng.webapp.framework;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by huuhoa on 2/18/17.
 * Default implementation for promise
 */

class PromiseImpl implements Promise {
    private WeakReference<WebAppCommunicationHandler> mWebAppCommunicationHandlerWeakReference;
    private String mMessageName;
    private String mMessageId;

    WebMessage mResult = null;

    PromiseImpl(WebAppCommunicationHandler webAppCommunicationHandler, String messageName, String messageId) {
        this.mWebAppCommunicationHandlerWeakReference = new WeakReference<>(webAppCommunicationHandler);
        this.mMessageName = messageName;
        this.mMessageId = messageId;
    }

    @Override
    public void resolve(@Nullable Object value) {
        callback(successObject(value));
    }

    @Override
    public void reject(int code, String message) {
        callback(failObject(code, message));
    }

    @Override
    public void reject(int code, Throwable e) {
        callback(failObject(code, e.getMessage()));
    }

    @Override
    public void reject(int code, String message, Throwable e) {
        callback(failObject(code, message));
    }

    @Override
    public void reject(Throwable reason) {
        callback(failObject(reason.getMessage()));
    }

    private void callback(JSONObject data) {
        mResult = new WebMessage();
        mResult.data = data;
        mResult.functionName = mMessageName;
        mResult.messageId = mMessageId;
        mResult.messageType = "callback";
        mResult.keepCallback = true;

        if (mWebAppCommunicationHandlerWeakReference.get() != null) {
            mWebAppCommunicationHandlerWeakReference.get().callback(mResult);
        }
    }

    private JSONObject failObject(String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("error", 1);
            result.put("errorMessage", message);
        } catch (JSONException ez) {
            Timber.w(ez);
        }

        return result;
    }

    private JSONObject failObject(int code, String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("error", code);
            result.put("errorMessage", message);
        } catch (JSONException ez) {
            Timber.w(ez);
        }

        return result;
    }

    private JSONObject successObject(@Nullable Object value) {
        JSONObject result = new JSONObject();

        try {
            result.put("error", 0);
            result.putOpt("data", value);
        } catch (JSONException ez) {
            Timber.w(ez);
        }

        return result;
    }
}
