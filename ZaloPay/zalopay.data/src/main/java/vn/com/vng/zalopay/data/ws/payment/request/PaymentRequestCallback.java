package vn.com.vng.zalopay.data.ws.payment.request;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by hieuvm on 3/8/17.
 */

public abstract class PaymentRequestCallback<T, R> {

    private static final int MESSAGE_POST_RESULT = 0x1;

    private static Gson mGson = new Gson();
    private static InternalHandler sHandler;

    private Class<T> tClass;

    public PaymentRequestCallback(Class<T> tClass) {
        this.tClass = tClass;
    }

    public void handlerResponse(String response) {
        R result = doBackground(convert(response));
        postResult(result);
    }

    private T convert(String response) {
        return mGson.fromJson(response, responseType());
    }

    private R postResult(R result) {
        @SuppressWarnings("unchecked")
        Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT,
                new PaymentRequestResult<>(this, result));
        message.sendToTarget();
        return result;
    }

    public abstract R doBackground(@NonNull T result);

    public abstract void onResult(R result);

    protected void onCancelled() {
    }

    private Type responseType() {
        return tClass;
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            PaymentRequestResult<?> result = (PaymentRequestResult<?>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    result.mTask.onResult(result.mData[0]);
                    break;
            }

        }
    }

    private static class PaymentRequestResult<Data> {
        final PaymentRequestCallback mTask;
        final Data[] mData;

        PaymentRequestResult(PaymentRequestCallback task, Data... data) {
            mTask = task;
            mData = data;
        }
    }

    private static Handler getHandler() {
        synchronized (PaymentRequestCallback.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler();
            }
            return sHandler;
        }
    }
}
